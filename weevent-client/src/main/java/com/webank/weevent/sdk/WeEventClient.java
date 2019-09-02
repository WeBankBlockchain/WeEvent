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

    // default STOMP url, ws://localhost:8080/weevent/stomp
    private static WeEventConnectionFactory connectionFactory;

    // stomp connection
    private TopicConnection connection;
    // (subscriptionId <-> TopicSession)
    private Map<String, TopicSession> sessionMap;


    WeEventClient() throws BrokerException {
        buildRpc(defaultJsonRpcUrl);
        buildJms(WeEventConnectionFactory.defaultBrokerUrl, "", "");
    }

    WeEventClient(String brokerUrl) throws BrokerException {
        validateParam(brokerUrl);
        buildRpc(brokerUrl + "/jsonrpc");
        buildJms(getStompUrl(brokerUrl), "", "");
    }

    WeEventClient(String brokerUrl, String userName, String password) throws BrokerException {
        validateParam(brokerUrl);
        validateUser(userName, password);
        buildRpc(brokerUrl + "/jsonrpc");
        buildJms(getStompUrl(brokerUrl), userName, password);
    }


    public String subscribe(String topic, String offset, @NonNull EventListener listener) throws BrokerException {
        try {
            validateParam(topic);
            validateParam(offset);
            TopicSession session = this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            // create topic
            Topic destination = session.createTopic(topic);

            // create subscriber
            ((WeEventTopic) destination).setOffset(offset);
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


    public boolean open(String topic) throws BrokerException {
        validateParam(topic);
        return this.brokerRpc.open(topic);
    }


    public boolean close(String topic) throws BrokerException {
        validateParam(topic);
        return this.brokerRpc.close(topic);
    }

    public boolean exist(String topic) throws BrokerException {
        validateParam(topic);
        return this.brokerRpc.exist(topic);
    }

    public TopicPage list(Integer pageIndex, Integer pageSize) throws BrokerException {
        return this.brokerRpc.list(pageIndex, pageSize);
    }

    public TopicInfo state(String topic) throws BrokerException {
        validateParam(topic);
        return this.brokerRpc.state(topic);
    }

    public WeEvent getEvent(String eventId) throws BrokerException {
        validateParam(eventId);
        return this.brokerRpc.getEvent(eventId);
    }

    public SendResult publish(String topic, byte[] content) throws BrokerException {
        return this.publish(topic, content, null);
    }

    public SendResult publish(String topic, byte[] content, Map<String, String> extensions) throws BrokerException {
        return this.publish(topic, WeEvent.DEFAULT_GROUP_ID, content, extensions);
    }

    public SendResult publish(String topic, String groupId, byte[] content, Map<String, String> extensions) throws BrokerException {
        WeEvent weEvent = new WeEvent();
        weEvent.setTopic(topic);
        weEvent.setContent(content);
        weEvent.setExtensions(extensions);
        return this.publish(weEvent, groupId);
    }

    public SendResult publish(WeEvent weEvent, String groupId) throws BrokerException {
        validateParam(weEvent.getTopic());
        validateParam(groupId);
        validateArrayParam(weEvent.getContent());
        SendResult sendResult = new SendResult();
        try {
            TopicSession session = this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            // create topic
            WeEventTopic weEventTopic = (WeEventTopic) session.createTopic(weEvent.getTopic());
            weEventTopic.setGroupId(groupId);
            //create bytesMessage
            WeEventBytesMessage bytesMessage = new WeEventBytesMessage();
            bytesMessage.writeObject(weEvent);
            //publish
            WeEventTopicPublisher publisher = (WeEventTopicPublisher) session.createPublisher(weEventTopic);
            publisher.publish(bytesMessage);
            //get WeEvent
            byte[] body = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(body);
            ObjectMapper mapper = new ObjectMapper();
            WeEvent event = mapper.readValue(body, WeEvent.class);
            log.info("topic [{}] publish success. weevent:{}", weEvent.getTopic(), this.getEvent(event.getEventId(),groupId));
            //return
            sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
            sendResult.setEventId(event.getEventId());
            sendResult.setTopic(event.getTopic());
        } catch (Exception e) {
            log.error("publish fail,error message: {}", e.getMessage());
            sendResult.setStatus(SendResult.SendResultStatus.ERROR);
            sendResult.setTopic(weEvent.getTopic());
            throw new BrokerException("publish fail,error message", e);
        }
        return sendResult;
    }

    public boolean close(String topic, String groupId) throws BrokerException {
        validateParam(topic);
        validateParam(groupId);
        return this.brokerRpc.close(topic, groupId);
    }


    public String subscribe(String topic, String groupId, String offset,
                            @NonNull EventListener listener) throws BrokerException {
        try {
            validateParam(topic);
            validateParam(groupId);
            validateParam(offset);
            TopicSession session = this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            // create topic
            Topic destination = session.createTopic(topic);

            // create subscriber
            ((WeEventTopic) destination).setOffset(offset);
            ((WeEventTopic) destination).setGroupId(groupId);//if not set default 1
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
                            log.info("event :{}", event);
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

    public String subscribe(String topic, String groupId, String offset, String subscriptionId,
                            @NonNull EventListener listener) throws BrokerException {
        try {
            validateParam(topic);
            validateParam(groupId);
            validateParam(offset);
            TopicSession session = this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            // create topic
            Topic destination = session.createTopic(topic);

            // create subscriber
            ((WeEventTopic) destination).setOffset(offset);
            ((WeEventTopic) destination).setGroupId(groupId);//if not set default 1
            ((WeEventTopic) destination).setContinueSubscriptionId(subscriptionId);
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


    public boolean exist(String topic, String groupId) throws BrokerException {
        validateParam(topic);
        validateParam(groupId);
        return this.brokerRpc.exist(topic, groupId);
    }

    public boolean open(String topic, String groupId) throws BrokerException {
        validateParam(topic);
        validateParam(groupId);
        return this.brokerRpc.open(topic, groupId);
    }

    public TopicPage list(Integer pageIndex, Integer pageSize, String groupId) throws BrokerException {
        validateParam(groupId);
        return this.brokerRpc.list(pageIndex, pageSize, groupId);
    }

    public TopicInfo state(String topic, String groupId) throws BrokerException {
        validateParam(topic);
        validateParam(groupId);
        return this.brokerRpc.state(topic, groupId);
    }

    public WeEvent getEvent(String eventId, String groupId) throws BrokerException {
        validateParam(groupId);
        validateParam(eventId);
        return this.brokerRpc.getEvent(eventId, groupId);
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

    private static void validateObject(Object object) throws BrokerException {
        if (object == null) {
            throw new BrokerException(ErrorCode.PARAM_ISEMPTY);
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

}
