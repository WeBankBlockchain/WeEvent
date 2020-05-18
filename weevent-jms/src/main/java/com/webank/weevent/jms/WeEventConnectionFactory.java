package com.webank.weevent.jms;


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

    private String brokerUrl;

    private String groupId;

    // JMS provider authorization
    private String userName = "";
    private String password = "";

    // Client id
    private String clientID;

    // stomp timeout
    private int timeout = 10;

    public static JMSException error2JMSException(ErrorCode errorCode) {
        return new JMSException(errorCode.getCodeDesc(), String.valueOf(errorCode.getCode()));
    }

    public static JMSException exp2JMSException(BrokerException e) {
        return new JMSException(e.getMessage(), String.valueOf(e.getCode()));
    }

    public static String genUniqueID() {
        return UUID.randomUUID().toString();
    }

    public WeEventConnectionFactory(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        this.groupId = WeEvent.DEFAULT_GROUP_ID;
    }

    public WeEventConnectionFactory(String brokerUrl, String groupId) {
        this.brokerUrl = brokerUrl;
        this.groupId = groupId;
    }

    public WeEventTopicConnection createWeEventTopicConnection() throws JMSException {

        IWeEventClient client;
        try {
            client = IWeEventClient.builder().brokerUrl(this.brokerUrl).groupId(this.groupId).userName(this.userName).password(this.password).timeout(this.timeout).build();
        } catch (BrokerException e) {
            log.error("build IWeEvent error", e);
            throw exp2JMSException(e);
        }
        WeEventTopicConnection connection = new WeEventTopicConnection(client);
        if (this.clientID != null) {
            connection.setClientID(this.clientID);
        }

        return connection;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    // TopicConnectionFactory override methods

    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        return createWeEventTopicConnection();
    }

    @Override
    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        this.userName = userName;
        this.password = password;
        return createWeEventTopicConnection();
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
