package com.webank.weevent.sdk.jms;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * WeEvent JMS TopicConnectionFactory.
 * Publish Subscribe is over WebSocket + Stomp.
 * Warning!
 * TopicConnectionFactory is a beta version currently, DO NOT USE it in product environment.
 * Use spring's websocket-stomp-client instead.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Slf4j
public class WeEventConnectionFactory implements ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory {
    public final static String NotSupportTips = "not support, only support pub/sub model in bytes message, no ack and transaction";

    public final static String defaultBrokerUrl = "ws://localhost:7000/weevent/stomp";

    // JMS provider
    private URI brokerUri;

    // JMS provider authorization
    private String userName;
    private String password;

    // Client id
    private String clientID;

    // stomp timeout
    private int timeout;

    public static JMSException error2JMSException(ErrorCode errorCode) {
        return new JMSException(errorCode.getCodeDesc(), String.valueOf(errorCode.getCode()));
    }

    public static JMSException exp2JMSException(BrokerException e) {
        return new JMSException(e.getMessage(), String.valueOf(e.getCode()));
    }

    public static String genUniqueID() {
        return UUID.randomUUID().toString();
    }

    public WeEventConnectionFactory() throws JMSException {
        this(defaultBrokerUrl);
    }

    public WeEventConnectionFactory(String BrokerUrl) throws JMSException {
        this.setBrokerUri(createUri(BrokerUrl));
    }

    public WeEventConnectionFactory(String userName, String password) throws JMSException {
        this.setUserName(userName);
        this.setPassword(password);
        this.setBrokerUri(createUri(defaultBrokerUrl));
    }

    public WeEventConnectionFactory(String userName, String password, String brokerUrl) throws JMSException {
        this.setUserName(userName);
        this.setPassword(password);
        this.setBrokerUri(createUri(brokerUrl));
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    private void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private void setBrokerUri(URI uri) {
        this.brokerUri = uri;
        this.timeout = 10;
    }

    private URI createUri(String url) throws JMSException {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new JMSException("invalid url format, eg: " + defaultBrokerUrl);
        }
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public WeEventTopicConnection createWeEventTopicConnection(String userName, String password) throws JMSException {
        if (this.brokerUri == null) {
            throw new JMSException("unknown broker url");
        }

        WebSocketTransport transport = WebSocketTransportFactory.create(this.brokerUri, this.timeout);
        WeEventTopicConnection connection = new WeEventTopicConnection(transport);
        if (this.clientID != null) {
            connection.setClientID(this.clientID);
        }
        if (userName != null && password != null) {
            connection.setUserName(userName);
            connection.setPassword(password);
        }

        return connection;
    }

    // TopicConnectionFactory override methods

    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        return createWeEventTopicConnection(this.userName, this.password);
    }

    @Override
    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        return createWeEventTopicConnection(userName, password);
    }

    // QueueConnectionFactory override methods

    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        throw new JMSException(NotSupportTips);
    }

    @Override
    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        throw new JMSException(NotSupportTips);
    }

    // ConnectionFactory override methods

    @Override
    public Connection createConnection() throws JMSException {
        throw new JMSException(NotSupportTips);
    }

    @Override
    public Connection createConnection(String s, String s1) throws JMSException {
        throw new JMSException(NotSupportTips);
    }

}
