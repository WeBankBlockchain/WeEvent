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

import com.webank.weevent.sdk.jms.WeEventConnectionFactory;
import com.webank.weevent.sdk.jms.WeEventTopic;
import com.webank.weevent.sdk.jms.WeEventTopicSubscriber;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Java Client for WeEvent.
 *
 * @author matthewliu
 * @since 2019/04/07
 */
@Slf4j
public class WeEventClient {
    /**
     * Interface for notify callback
     */
    public interface EventListener {
        /**
         * Called while new event arrived.
         *
         * @param event the event
         */
        void onEvent(WeEvent event);

        /**
         * Called while raise exception.
         *
         * @param e the e
         */
        void onException(Throwable e);
    }

    private final static String defaultJsonRpcUrl = "http://localhost:8080/weevent/jsonrpc";
    private IBrokerRpc brokerRpc;

    // default STOMP url, ws://localhost:8080/weevent/stomp
    private static WeEventConnectionFactory connectionFactory;
    private TopicConnection connection;
    // (subscriptionId <-> TopicSession)
    private Map<String, TopicSession> sessionMap;

    /**
     * Get the client handler of weevent's broker with default url, http://localhost:8080/weevent.
     *
     * @throws BrokerException broker exception
     */
    public WeEventClient() throws BrokerException {
        buildRpc(defaultJsonRpcUrl);
        buildJms(WeEventConnectionFactory.defaultBrokerUrl, "", "");
    }

    /**
     * Get the client handler of weevent's broker with custom url.
     *
     * @param brokerUrl weevent's broker url, like http://localhost:8080/weevent
     * @throws BrokerException broker exception
     */
    public WeEventClient(String brokerUrl) throws BrokerException {
        buildRpc(brokerUrl + "/jsonrpc");
        buildJms(getStompUrl(brokerUrl), "", "");
    }

    /**
     * Get the client handler of weevent's broker custom url and account authorization.
     *
     * @param brokerUrl weevent's broker url, like http://localhost:8080/weevent
     * @param userName account name
     * @param password password
     * @throws BrokerException broker exception
     */
    public WeEventClient(String brokerUrl, String userName, String password) throws BrokerException {
        buildRpc(brokerUrl);
        buildJms(getStompUrl(brokerUrl), userName, password);
    }

    /**
     * Publish an event to topic.
     *
     * @param topic topic name
     * @param content topic data
     * @return send result, SendResult.SUCCESS if success, and SendResult.eventId
     * @throws BrokerException broker exception
     */
    public SendResult publish(String topic, byte[] content) throws BrokerException {
        return this.brokerRpc.publish(topic, content);
    }

    /**
     * Subscribe events from topic.
     *
     * @param topic topic name
     * @param offset, from next event after this offset(an event id), WeEvent.OFFSET_FIRST if from head of queue, WeEvent.OFFSET_LAST if from tail of queue
     * @param listener callback
     * @return subscription Id
     * @throws BrokerException invalid input param
     */
    public String subscribe(String topic, String offset, EventListener listener) throws BrokerException {
        try {
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
            throw new BrokerException(ErrorCode.SDK_JMS_EXCEPTION);
        }
    }


    /**
     * Unsubscribe an exist subscription subscribed by subscribe interface.
     * The consumer will no longer receive messages from broker after this.
     *
     * @param subscriptionId invalid input
     * @return success if true
     * @throws BrokerException broker exception
     */
    public boolean unSubscribe(String subscriptionId) throws BrokerException {
        if (this.sessionMap.containsKey(subscriptionId)) {
            TopicSession session = this.sessionMap.get(subscriptionId);
            try {
                session.unsubscribe(subscriptionId);
            } catch (JMSException e) {
                log.error("jms exception", e);
                throw new BrokerException(ErrorCode.SDK_JMS_EXCEPTION);
            }

            this.sessionMap.remove(subscriptionId);
            return true;
        }

        return false;
    }

    // The following is interface for IEventTopic.

    /**
     * Open a topic.
     *
     * @param topic topic name
     * @return true if success
     * @throws BrokerException broker exception
     */
    public boolean open(String topic) throws BrokerException {
        return this.brokerRpc.open(topic);
    }

    /**
     * Close a topic.
     *
     * @param topic topic name
     * @return true if success
     * @throws BrokerException broker exception
     */
    public boolean close(String topic) throws BrokerException {
        return this.brokerRpc.close(topic);
    }

    /**
     * Check a topic is exist or not.
     *
     * @param topic topic name
     * @return true if exist
     * @throws BrokerException broker exception
     */
    public boolean exist(String topic) throws BrokerException {
        return this.brokerRpc.exist(topic);
    }

    /**
     * List all topics in weevent's broker.
     *
     * @param pageIndex page index, from 0
     * @param pageSize page size, [10, 100)
     * @return topic list
     * @throws BrokerException broker exception
     */
    public TopicPage list(Integer pageIndex, Integer pageSize) throws BrokerException {
        return this.brokerRpc.list(pageIndex, pageSize);
    }

    /**
     * Get a topic information.
     *
     * @param topic topic name
     * @return topic information
     * @throws BrokerException broker exception
     */
    public TopicInfo state(String topic) throws BrokerException {
        return this.brokerRpc.state(topic);
    }

    /**
     * Get an event information.
     *
     * @param eventId event id
     * @return weevent
     * @throws BrokerException broker exception
     */
    public WeEvent getEvent(String eventId) throws BrokerException {
        return this.brokerRpc.getEvent(eventId);
    }

    // implements

    private String getStompUrl(String brokerUrl) throws BrokerException {
        String stompUrl;
        if (brokerUrl.contains("http://")) {
            stompUrl = brokerUrl.replace("http://", "ws://");
        } else if (brokerUrl.contains("https://")) {
            stompUrl = brokerUrl.replace("https://", "wss://");
        } else {
            throw new BrokerException(ErrorCode.URL_INVALID_FORMAT);
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

    /**
     * Get a handler of IBrokerRpc.
     *
     * @param jsonRpcUrl the broker's json rpc url
     * @throws BrokerException broker exception
     */
    private void buildRpc(String jsonRpcUrl) throws BrokerException {
        log.info("broker's json rpc url: {}", jsonRpcUrl);

        URL url;
        try {
            url = new URL(jsonRpcUrl);
        } catch (MalformedURLException e) {
            log.error("invalid url format", e);
            throw new BrokerException(ErrorCode.URL_INVALID_FORMAT);
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

    /**
     * Get a handler of JMS connection.
     *
     * @param stompUrl the broker's stomp url
     * @param userName account
     * @param password password
     * @throws BrokerException broker exception
     */
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

    public static BrokerException jms2BrokerException(JMSException e) {
        return new BrokerException(Integer.valueOf(e.getErrorCode()), e.getMessage());
    }
}
