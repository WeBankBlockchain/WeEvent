package com.webank.weevent.sdk;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.webank.weevent.sdk.jms.WeEventBytesMessage;
import com.webank.weevent.sdk.jms.WeEventConnectionFactory;
import com.webank.weevent.sdk.jms.WeEventTopic;
import com.webank.weevent.sdk.jms.WeEventTopicPublisher;
import com.webank.weevent.sdk.jms.WeEventTopicSubscriber;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class WeEventClient implements IWeEventClient {
    private final static String defaultJsonRpcUrl = "http://127.0.0.1:8080/weevent/jsonrpc";
    // json rpc proxy
    private IBrokerRpc brokerRpc;

    // groupId
    private String groupId;

    // default STOMP url, ws://localhost:8080/weevent/stomp
    private WeEventConnectionFactory connectionFactory;

    // stomp connection
    private TopicConnection connection;
    // (subscriptionId <-> TopicSession)
    private Map<String, TopicSession> sessionMap;

    WeEventClient() throws BrokerException {
        buildRpc(defaultJsonRpcUrl);
        buildJms(WeEventConnectionFactory.defaultBrokerUrl, "", "");
        this.groupId = WeEvent.DEFAULT_GROUP_ID;
    }

    WeEventClient(String brokerUrl) throws BrokerException {
        validateParam(brokerUrl);
        buildRpc(brokerUrl + "/jsonrpc");
        buildJms(getStompUrl(brokerUrl), "", "");
        this.groupId = WeEvent.DEFAULT_GROUP_ID;
    }

    WeEventClient(String brokerUrl, String groupId) throws BrokerException {
        validateParam(brokerUrl);
        buildRpc(brokerUrl + "/jsonrpc");
        buildJms(getStompUrl(brokerUrl), "", "");
        initGroupId(groupId);
    }

    WeEventClient(String brokerUrl, String groupId, String userName, String password) throws BrokerException {
        validateParam(brokerUrl);
        validateUser(userName, password);
        buildRpc(brokerUrl + "/jsonrpc");
        buildJms(getStompUrl(brokerUrl), userName, password);
        initGroupId(groupId);
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
        SendResult sendResult = new SendResult();
        try {
            TopicSession session = this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            // create topic
            WeEventTopic weEventTopic = (WeEventTopic) session.createTopic(weEvent.getTopic());
            weEventTopic.setGroupId(this.groupId);
            //create bytesMessage
            WeEventBytesMessage bytesMessage = new WeEventBytesMessage();
            bytesMessage.writeObject(weEvent);
            //publish
            WeEventTopicPublisher publisher = (WeEventTopicPublisher) session.createPublisher(weEventTopic);
            publisher.publish(bytesMessage);
            //return
            sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
            sendResult.setEventId(bytesMessage.getJMSMessageID());
            sendResult.setTopic(weEvent.getTopic());
        } catch (Exception e) {
            log.error("publish fail,error message: {}", e.getMessage());
            sendResult.setStatus(SendResult.SendResultStatus.ERROR);
            sendResult.setTopic(weEvent.getTopic());
        }
        return sendResult;
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

        String topic = DataTypeTools.topicArrayToString(topics);
        return dealSubscribe(topic, offset, null, listener);
    }

    @Override
    public String subscribe(String[] topics, String offset, String subscriptionId,
                            @NonNull EventListener listener) throws BrokerException {
        String topic = DataTypeTools.topicArrayToString(topics);
        return dealSubscribe(topic, offset, subscriptionId, listener);
    }

    private String dealSubscribe(String topic,String offset, String subscriptionId,EventListener listener) throws BrokerException {
        try {
            validateParam(topic);
            validateParam(offset);
            TopicSession session = this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            // create topic
            Topic destination = session.createTopic(topic);

            // create subscriber
            ((WeEventTopic) destination).setOffset(offset);
            ((WeEventTopic) destination).setGroupId(this.groupId);
            if (!StringUtils.isBlank(subscriptionId)) {
                ((WeEventTopic) destination).setContinueSubscriptionId(subscriptionId);
            }
            WeEventTopicSubscriber subscriber = (WeEventTopicSubscriber) session.createSubscriber(destination);

            // create listener
            subscriber.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    if (message instanceof BytesMessage) {
                        try {
                            BytesMessage bytesMessage = (BytesMessage) message;
                            ObjectMapper mapper = new ObjectMapper();
                            byte[] body = new byte[(int) bytesMessage.getBodyLength()];
                            bytesMessage.readBytes(body);
                            WeEvent event = mapper.readValue(body, WeEvent.class);
                            listener.onEvent(event);
                        } catch (IOException | JMSException e) {
                            log.error("onMessage exception", e);
                            listener.onException(e);
                        }
                    }
                }
            });

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
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

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

    private void buildRpc(String jsonRpcUrl) throws BrokerException {
        log.info("broker's json rpc url: {}", jsonRpcUrl);

        URL url;
        try {
            url = new URL(jsonRpcUrl);
        } catch (MalformedURLException e) {
            log.error("invalid url format", e);
            throw new BrokerException(ErrorCode.PARAM_ISBLANK);
        }

        JsonRpcHttpClient client = new JsonRpcHttpClient(url);
        if (jsonRpcUrl.contains("https://")) {
            SSLContext sslContext = getSSLContext();
            client.setSslContext(sslContext);
            // dot not verify HostName
            client.setHostNameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname,
                                      SSLSession sslsession) {
                    return true;
                }
            });
        }

        this.brokerRpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
    }

    private void buildJms(String stompUrl, String userName, String password) throws BrokerException {
        log.info("broker's stomp url: {}", stompUrl);

        try {
            if (connectionFactory == null) {
                connectionFactory = new WeEventConnectionFactory(userName, password, stompUrl);
            }
            this.sessionMap = new ConcurrentHashMap<>();
            this.connection = connectionFactory.createTopicConnection();
            this.connection.start();
        } catch (JMSException e) {
            log.error("init jms connection factory failed", e);
            throw jms2BrokerException(e);
        }
    }

    private static void validateWeEvent(WeEvent weEvent) throws BrokerException {
        validateParam(weEvent.getTopic());
        validateArrayParam(weEvent.getContent());
        validateExtensions(weEvent.getExtensions());

    }

    private static void validateExtensions(Map<String, String> extensions) throws BrokerException {
        if (extensions == null || extensions.isEmpty()) {
            throw new BrokerException(ErrorCode.PARAM_ISNULL);
        }

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

    private static void validateUser(String userName, String password) throws BrokerException {
        if (StringUtils.isBlank(userName)) {
            throw new BrokerException(ErrorCode.PARAM_ISBLANK);
        }
        if (StringUtils.isBlank(password)) {
            throw new BrokerException(ErrorCode.PARAM_ISBLANK);
        }
    }

    private static BrokerException jms2BrokerException(JMSException e) {
        return new BrokerException(Integer.parseInt(e.getErrorCode()), e.getMessage());
    }

    private void initGroupId(String groupId){
        if (StringUtils.isBlank(groupId)){
            this.groupId = WeEvent.DEFAULT_GROUP_ID;
        } else {
            this.groupId = groupId;
        }
    }
}
