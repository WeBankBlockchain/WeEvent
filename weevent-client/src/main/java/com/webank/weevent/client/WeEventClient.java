package com.webank.weevent.client;


import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.webank.weevent.client.stomp.TopicContent;
import com.webank.weevent.client.stomp.WebSocketTransport;
import com.webank.weevent.client.stomp.WebSocketTransportFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;

@Slf4j
public class WeEventClient implements IWeEventClient {
    private final String brokerUrl;
    private final String brokerRestfulUrl;
    private final String groupId;
    private final String userName;
    private final String password;

    // default STOMP url, ws://localhost:8080/weevent-broker/stomp
    private final String brokerStompUrl = "ws://localhost:7000/weevent-broker/stomp";
    private List<String> subscribeIdList;
    private HttpClientHelper httpClientHelper;
    private WebSocketTransport transport;

    WeEventClient(String brokerUrl, String groupId, String userName, String password, int timeout) throws BrokerException {
        validateParam(brokerUrl);

        this.brokerUrl = brokerUrl;
        this.brokerRestfulUrl = brokerUrl.concat("/rest");
        this.groupId = groupId;
        this.userName = userName;
        this.password = password;
        this.httpClientHelper = new HttpClientHelper(timeout);
        this.subscribeIdList = Collections.synchronizedList(new ArrayList<>());

        buildStomp();
    }

    @Override
    public boolean unSubscribe(String subscriptionId) throws BrokerException {
        validateParam(subscriptionId);

        if (this.subscribeIdList.contains(subscriptionId) && this.transport.isConnected()) {
            this.transport.stompUnSubscribe(subscriptionId);
            this.subscribeIdList.remove(subscriptionId);
            return true;
        }

        return false;
    }

    @Override
    public boolean open(String topic) throws BrokerException {
        validateParam(topic);
        HttpGet httpGet = new HttpGet(String.format("%s/open?topic=%s&groupId=%s",
                this.brokerRestfulUrl,
                encodeTopic(topic),
                this.groupId));

        return this.httpClientHelper.invokeCGI(httpGet, new TypeReference<BaseResponse<Boolean>>() {
        }).getData();
    }

    @Override
    public boolean close(String topic) throws BrokerException {
        validateParam(topic);
        HttpGet httpGet = new HttpGet(String.format("%s/close?topic=%s&groupId=%s",
                this.brokerRestfulUrl,
                encodeTopic(topic),
                this.groupId));

        return this.httpClientHelper.invokeCGI(httpGet, new TypeReference<BaseResponse<Boolean>>() {
        }).getData();
    }

    @Override
    public boolean exist(String topic) throws BrokerException {
        validateParam(topic);
        HttpGet httpGet = new HttpGet(String.format("%s/exist?topic=%s&groupId=%s",
                this.brokerRestfulUrl,
                encodeTopic(topic),
                this.groupId));


        return this.httpClientHelper.invokeCGI(httpGet, new TypeReference<BaseResponse<Boolean>>() {
        }).getData();
    }

    @Override
    public TopicPage list(Integer pageIndex, Integer pageSize) throws BrokerException {
        HttpGet httpGet = new HttpGet(String.format("%s/list?pageIndex=%s&pageSize=%s&groupId=%s", this.brokerRestfulUrl, pageIndex, pageSize, this.groupId));

        return this.httpClientHelper.invokeCGI(httpGet, new TypeReference<BaseResponse<TopicPage>>() {
        }).getData();
    }

    @Override
    public TopicInfo state(String topic) throws BrokerException {
        validateParam(topic);
        HttpGet httpGet = new HttpGet(String.format("%s/state?topic=%s&groupId=%s",
                this.brokerRestfulUrl,
                encodeTopic(topic),
                this.groupId));

        return this.httpClientHelper.invokeCGI(httpGet, new TypeReference<BaseResponse<TopicInfo>>() {
        }).getData();
    }

    @Override
    public WeEvent getEvent(String eventId) throws BrokerException {
        validateParam(eventId);
        HttpGet httpGet;
        try {
            httpGet = new HttpGet(String.format("%s/getEvent?eventId=%s&groupId=%s",
                    this.brokerRestfulUrl,
                    URLEncoder.encode(eventId, StandardCharsets.UTF_8.toString()),
                    this.groupId));
        } catch (UnsupportedEncodingException e) {
            log.error("Encode eventId error", e);
            throw new BrokerException(ErrorCode.ENCODE_EVENT_ID_ERROR);
        }

        return this.httpClientHelper.invokeCGI(httpGet, new TypeReference<BaseResponse<WeEvent>>() {
        }).getData();
    }

    @Override
    public SendResult publish(WeEvent weEvent) throws BrokerException {
        validateWeEvent(weEvent);
        log.info("start to publish: {}", weEvent);

        TopicContent weEventTopic = new TopicContent(weEvent.getTopic());
        weEventTopic.setGroupId(this.groupId);

        checkConnected();
        // publish
        return this.transport.stompSend(weEventTopic, weEvent);
    }

    @Override
    public CompletableFuture<SendResult> publishAsync(WeEvent weEvent) throws BrokerException {
        validateWeEvent(weEvent);
        log.info("start to publish event by async: {}", weEvent);

        TopicContent weEventTopic = new TopicContent(weEvent.getTopic());
        weEventTopic.setGroupId(this.groupId);

        checkConnected();
        return this.transport.stompSendAsync(weEventTopic, weEvent);
    }

    @Override
    public String subscribe(String topic, String offset, @NonNull EventListener listener) throws BrokerException {

        return dealSubscribe(topic, offset, null, listener);
    }

    @Override
    public String subscribe(String topic, String offset, String subscriptionId,
                            @NonNull EventListener listener) throws BrokerException {

        return dealSubscribe(topic, offset, subscriptionId, listener);
    }

    @Override
    public String subscribe(String[] topics, String offset, @NonNull EventListener listener) throws BrokerException {

        String topic = StringUtils.join(topics, WeEvent.MULTIPLE_TOPIC_SEPARATOR);
        return dealSubscribe(topic, offset, "", listener);
    }

    @Override
    public String subscribe(String[] topics, String offset, String subscriptionId,
                            @NonNull EventListener listener) throws BrokerException {
        String topic = StringUtils.join(topics, WeEvent.MULTIPLE_TOPIC_SEPARATOR);
        return dealSubscribe(topic, offset, subscriptionId, listener);
    }


    private String dealSubscribe(String topic, String offset, String subscriptionId, EventListener
            listener) throws BrokerException {
        validateParam(topic);
        validateParam(offset);

        // extend param
        TopicContent weEventTopic = new TopicContent(topic);
        weEventTopic.setOffset(offset);
        weEventTopic.setGroupId(this.groupId);
        if (StringUtils.isNotBlank(subscriptionId)) {
            weEventTopic.setContinueSubscriptionId(subscriptionId);
        }

        // create subscriber
        String subscribeId = this.transport.stompSubscribe(weEventTopic, listener);
        this.subscribeIdList.add(subscribeId);
        return subscribeId;
    }

    private String getStompUrl(String brokerUrl) throws BrokerException {
        String stompUrl;
        if (brokerUrl.contains("http://")) {
            stompUrl = brokerUrl.replace("http://", "ws://");
        } else if (brokerUrl.contains("https://")) {
            stompUrl = brokerUrl.replace("https://", "wss://");
        } else {
            throw new BrokerException(ErrorCode.PARAM_ISBLANK);
        }
        stompUrl += "/stomp";
        return stompUrl;
    }


    public static SSLContext getSSLContext() throws BrokerException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // impl X509TrustManager interface，not verify certificate
            X509TrustManager x509TrustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error("init tls ca failed", e);
            throw new BrokerException(ErrorCode.SDK_TLS_INIT_FAILED);
        }
    }

    private void buildStomp() throws BrokerException {
        String stompUrl = getStompUrl(this.brokerUrl);
        log.info("broker's stomp url: {}", stompUrl);
        this.transport = WebSocketTransportFactory.create(createUri(stompUrl), 10);
        if (!this.transport.isConnected()) {
            this.transport.stompConnect(this.userName, this.password);
        }
    }

    private static void validateWeEvent(WeEvent weEvent) throws BrokerException {
        validateParam(weEvent.getTopic());
        validateArrayParam(weEvent.getContent());
    }

    private static void validateParam(String param) throws BrokerException {
        if (StringUtils.isBlank(param)) {
            throw new BrokerException(ErrorCode.PARAM_ISBLANK);
        }
    }

    private static void validateArrayParam(byte[] param) throws BrokerException {
        if (param == null || param.length == 0) {
            throw new BrokerException(ErrorCode.PARAM_ISEMPTY);
        }
    }


    private String encodeTopic(String topic) throws BrokerException {
        try {
            return URLEncoder.encode(topic, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("encode topic error", e);
            throw new BrokerException(ErrorCode.ENCODE_TOPIC_ERROR);
        }
    }

    private void checkConnected() throws BrokerException {
        if (!this.transport.isConnected()) {
            throw new BrokerException(ErrorCode.SDK_STOMP_CONNECTION_BREAKDOWN);
        }
    }

    private URI createUri(String url) throws BrokerException {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new BrokerException("invalid url format, eg: " + brokerStompUrl);
        }
    }

}
