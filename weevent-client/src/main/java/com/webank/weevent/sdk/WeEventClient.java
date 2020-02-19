package com.webank.weevent.sdk;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.webank.weevent.sdk.jms.WeEventBytesMessage;
import com.webank.weevent.sdk.jms.WeEventConnectionFactory;
import com.webank.weevent.sdk.jms.WeEventTopic;
import com.webank.weevent.sdk.jms.WeEventTopicPublisher;
import com.webank.weevent.sdk.jms.WeEventTopicSubscriber;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class WeEventClient implements IWeEventClient {
    private final String brokerUrl;
    private final String groupId;
    private final String userName;
    private final String password;
    private final int timeout;

    // json rpc proxy
    private IBrokerRpc brokerRpc;
    // default STOMP url, ws://localhost:8080/weevent-broker/stomp
    private WeEventConnectionFactory connectionFactory;
    // stomp connection
    private TopicConnection connection;
    // (subscriptionId <-> TopicSession)
    private Map<String, TopicSession> sessionMap;

    WeEventClient(String brokerUrl, String groupId, String userName, String password, int timeout) throws BrokerException {
        validateParam(brokerUrl);

        this.brokerUrl = brokerUrl;
        this.groupId = groupId;
        this.userName = userName;
        this.password = password;
        this.timeout = timeout;

        buildRpc();
        buildJms();
    }

    @Override
    public boolean unSubscribe(String subscriptionId) throws BrokerException {
        validateParam(subscriptionId);

        if (this.sessionMap.containsKey(subscriptionId)) {
            TopicSession session = this.sessionMap.get(subscriptionId);
            try {
                session.unsubscribe(subscriptionId);
            } catch (JMSException e) {
                log.error("jms exception", e);
                throw jms2BrokerException(e);
            }

            this.sessionMap.remove(subscriptionId);
            return true;
        }

        return false;
    }

    @Override
    public boolean open(String topic) throws BrokerException {
        validateParam(topic);
        return this.brokerRpc.open(topic, this.groupId);
    }

    @Override
    public boolean close(String topic) throws BrokerException {
        validateParam(topic);
        return this.brokerRpc.close(topic, this.groupId);
    }

    @Override
    public boolean exist(String topic) throws BrokerException {
        validateParam(topic);
        return this.brokerRpc.exist(topic, this.groupId);
    }

    @Override
    public TopicPage list(Integer pageIndex, Integer pageSize) throws BrokerException {
        return this.brokerRpc.list(pageIndex, pageSize, this.groupId);
    }

    @Override
    public TopicInfo state(String topic) throws BrokerException {
        validateParam(topic);
        return this.brokerRpc.state(topic, this.groupId);
    }

    @Override
    public WeEvent getEvent(String eventId) throws BrokerException {
        validateParam(eventId);
        return this.brokerRpc.getEvent(eventId, this.groupId);
    }

    @Override
    public SendResult publish(WeEvent weEvent) throws BrokerException {
        validateWeEvent(weEvent);
        log.info("start to publish: {}", weEvent);

        SendResult sendResult = new SendResult();
        sendResult.setTopic(weEvent.getTopic());
        try {
            TopicSession session = this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            // create topic
            WeEventTopic weEventTopic = (WeEventTopic) session.createTopic(weEvent.getTopic());
            weEventTopic.setGroupId(this.groupId);

            // create bytesMessage
            WeEventBytesMessage weEventBytesMessage = new WeEventBytesMessage();
            weEventBytesMessage.writeBytes(weEvent.getContent());
            weEventBytesMessage.setExtensions(weEvent.getExtensions());
            // publish
            WeEventTopicPublisher publisher = (WeEventTopicPublisher) session.createPublisher(weEventTopic);
            publisher.publish(weEventBytesMessage);

            // return
            sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
            sendResult.setEventId(weEventBytesMessage.getJMSMessageID());

            log.info("publish success, eventID: {}", weEventBytesMessage.getJMSMessageID());
        } catch (JMSException e) {
            log.error("jms exception", e);
            throw jms2BrokerException(e);
        } catch (Exception e) {
            log.error("publish failed", e);
            sendResult.setStatus(SendResult.SendResultStatus.ERROR);
        }

        return sendResult;
    }

    @Override
    public String subscribe(String topic, String offset, @NonNull EventListener listener) throws BrokerException {

        return dealSubscribe(topic, offset, null, false, listener);
    }

    @Override
    public String subscribe(String topic, String offset, String subscriptionId,
                            @NonNull EventListener listener) throws BrokerException {

        return dealSubscribe(topic, offset, subscriptionId, false, listener);
    }

    @Override
    public String subscribe(String[] topics, String offset, @NonNull EventListener listener) throws BrokerException {

        String topic = StringUtils.join(topics, WeEvent.MULTIPLE_TOPIC_SEPARATOR);
        return dealSubscribe(topic, offset, "", false, listener);
    }

    @Override
    public String subscribe(String[] topics, String offset, String subscriptionId,
                            @NonNull EventListener listener) throws BrokerException {
        String topic = StringUtils.join(topics, WeEvent.MULTIPLE_TOPIC_SEPARATOR);
        return dealSubscribe(topic, offset, subscriptionId, false, listener);
    }

    private String dealSubscribe(String topic, String offset, String subscriptionId, boolean isFile, EventListener listener) throws BrokerException {
        try {
            validateParam(topic);
            validateParam(offset);
            TopicSession session = this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            // create topic
            Topic destination = session.createTopic(topic);

            // extend param
            WeEventTopic weEventTopic = (WeEventTopic) destination;
            weEventTopic.setOffset(offset);
            weEventTopic.setGroupId(this.groupId);
            if (!StringUtils.isBlank(subscriptionId)) {
                weEventTopic.setContinueSubscriptionId(subscriptionId);
            }
            weEventTopic.setFile(isFile);

            // create subscriber
            WeEventTopicSubscriber subscriber = (WeEventTopicSubscriber) session.createSubscriber(destination);

            // create listener
            subscriber.setMessageListener(bytesMessage -> {
                        if (bytesMessage instanceof WeEventBytesMessage) {
                            try {
                                WeEventBytesMessage message = (WeEventBytesMessage) bytesMessage;
                                byte[] body = new byte[(int) message.getBodyLength()];
                                message.readBytes(body);

                                Topic jmsDestination = (Topic) message.getJMSDestination();
                                WeEvent event = new WeEvent(jmsDestination.getTopicName(), body, message.getExtensions());
                                event.setEventId(message.getJMSMessageID());
                                listener.onEvent(event);
                            } catch (JMSException e) {
                                log.error("onMessage exception", e);
                                listener.onException(jms2BrokerException(e));
                            }
                        }
                    }
            );

            this.sessionMap.put(subscriber.getSubscriptionId(), session);
            return subscriber.getSubscriptionId();
        } catch (JMSException e) {
            log.error("jms exception", e);
            throw jms2BrokerException(e);
        }
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

    private void buildRpc() throws BrokerException {
        String jsonRpcUrl = this.brokerUrl + "/jsonrpc";
        log.info("broker's json rpc url: {}", jsonRpcUrl);

        URL url;
        try {
            url = new URL(jsonRpcUrl);
        } catch (MalformedURLException e) {
            log.error("invalid url format", e);
            throw new BrokerException(ErrorCode.PARAM_ISBLANK);
        }

        JsonRpcHttpClient client = new JsonRpcHttpClient(url);
        client.setConnectionTimeoutMillis(this.timeout);
        client.setReadTimeoutMillis(this.timeout);

        // ssl
        if (jsonRpcUrl.contains("https://")) {
            SSLContext sslContext = getSSLContext();
            client.setSslContext(sslContext);
            // dot not verify HostName
            client.setHostNameVerifier((hostname, sslsession) -> true);
        }

        // custom Exception
        // {"jsonrpc":"2.0","id":"1","error":{"code":100106,"message":"topic name contain invalid char, ascii must be in[32, 128] except wildcard(+,#)"}}
        client.setExceptionResolver(response -> {
            log.error("Exception in json rpc invoke, {}", response.toString());
            JsonNode error = response.get("error");
            return new BrokerException(error.get("code").intValue(), error.get("message").textValue());
        });

        this.brokerRpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
    }

    private void buildJms() throws BrokerException {
        String stompUrl = getStompUrl(this.brokerUrl);
        log.info("broker's stomp url: {}", stompUrl);

        try {
            if (this.connectionFactory == null) {
                this.connectionFactory = new WeEventConnectionFactory(this.userName, this.password, stompUrl);
            }
            this.sessionMap = new ConcurrentHashMap<>();
            this.connection = this.connectionFactory.createTopicConnection();
            this.connection.start();
        } catch (JMSException e) {
            log.error("init jms connection factory failed", e);
            throw jms2BrokerException(e);
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

    private static BrokerException jms2BrokerException(JMSException e) {
        if (StringUtils.isBlank(e.getErrorCode())) {
            return new BrokerException(e.getMessage());
        } else {
            return new BrokerException(Integer.parseInt(e.getErrorCode()), e.getMessage());
        }
    }

    @Override
    public SendResult publishFile(String topic, String localFile) throws BrokerException, IOException {
        // upload file
        FileChunksTransport fileChunksTransport = new FileChunksTransport(this.brokerUrl + "/file");
        SendResult sendResult = fileChunksTransport.upload(localFile, topic, this.groupId);

        log.info("publish file result: {}", sendResult);
        return sendResult;

    }

    static class FileEventListener implements EventListener {
        private final FileChunksTransport fileChunksTransport;
        private final FileListener fileListener;

        private String subscriptionId;

        public FileEventListener(FileChunksTransport fileChunksTransport, FileListener fileListener) {
            this.fileChunksTransport = fileChunksTransport;
            this.fileListener = fileListener;
        }

        public void setSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        @Override
        public void onEvent(WeEvent event) {
            // download file
            String fileId = new String(event.getContent(), StandardCharsets.UTF_8);
            String host = event.getExtensions().get("host");
            String localFile = null;
            try {
                localFile = this.fileChunksTransport.download(host, fileId);
            } catch (BrokerException | IOException e) {
                log.error("detect exception", e);
                this.onException(e);
            }
            this.fileListener.onFile(this.subscriptionId, localFile);
        }

        @Override
        public void onException(Throwable e) {
            this.fileListener.onException(e);
        }
    }

    @Override
    public String subscribeFile(String topic, String filePath, FileListener fileListener) throws BrokerException {
        // subscribe file event
        FileChunksTransport fileChunksTransport = new FileChunksTransport(this.brokerUrl + "/file", filePath);
        FileEventListener fileEventListener = new FileEventListener(fileChunksTransport, fileListener);
        String subscriptionId = this.dealSubscribe(topic, WeEvent.OFFSET_LAST, "", true, fileEventListener);
        fileEventListener.setSubscriptionId(subscriptionId);

        return subscriptionId;
    }
}
