package com.webank.weevent.jms;


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

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.IWeEventClient;
import com.webank.weevent.client.WeEvent;

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

    public final static String defaultBrokerUrl = "ws://localhost:7000/weevent-broker/stomp";

    // JMS provider
    private URI brokerUri;

    private String brokerHttpUrl;

    private String groupId;

    // JMS provider authorization
    private String userName = "";
    private String password = "";

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
        this.setBrokerUri(createUri(defaultBrokerUrl));
        this.groupId = WeEvent.DEFAULT_GROUP_ID;
        this.brokerHttpUrl = getHttpUrl(defaultBrokerUrl);
    }

    public WeEventConnectionFactory(String brokerUrl, String groupId) throws JMSException {
        this.setBrokerUri(createUri(brokerUrl));
        this.groupId = groupId;
        this.brokerHttpUrl = getHttpUrl(brokerUrl);
    }

    public WeEventConnectionFactory(String userName, String password, String groupId) throws JMSException {
        this.setUserName(userName);
        this.setPassword(password);
        this.groupId = groupId;
        this.setBrokerUri(createUri(defaultBrokerUrl));
        this.brokerHttpUrl = getHttpUrl(defaultBrokerUrl);
    }

    public WeEventConnectionFactory(String userName, String password, String brokerUrl, String groupId) throws JMSException {
        this.setUserName(userName);
        this.setPassword(password);
        this.groupId = groupId;
        this.setBrokerUri(createUri(brokerUrl));
        this.brokerHttpUrl = getHttpUrl(brokerUrl);
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

        IWeEventClient client;
        try {
            client = IWeEventClient.builder().brokerUrl(this.brokerHttpUrl).groupId(this.groupId).userName(userName).password(password).build();
        } catch (BrokerException e) {
            log.error("build IWeEvent error", e);
            throw exp2JMSException(e);
        }
        WeEventTopicConnection connection = new WeEventTopicConnection(client);
        if (this.clientID != null) {
            connection.setClientID(this.clientID);
        }
        if (userName != null && password != null) {
            connection.setUserName(userName);
            connection.setPassword(password);
        }

        return connection;
    }

    public String getHttpUrl(String brokerUrl) throws JMSException {
        String httpUrl;
        if (brokerUrl.contains("ws://") && brokerUrl.contains("/stomp")) {
            httpUrl = brokerUrl.replace("ws://", "http://").replace("/stomp", "");
        } else if (brokerUrl.contains("wss://")) {
            httpUrl = brokerUrl.replace("wss://", "https://").replace("/stomp", "");
        } else {
            throw error2JMSException(ErrorCode.PARAM_ISBLANK);
        }
        return httpUrl;
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
