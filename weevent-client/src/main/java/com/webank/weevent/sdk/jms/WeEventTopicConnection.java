package com.webank.weevent.sdk.jms;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

/**
 * WeEvent JMS TopicConnection.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
public class WeEventTopicConnection implements TopicConnection, CommandDispatcher {
    private String userName = "";
    private String password = "";
    private String clientID = "";
    private WebSocketTransport transport;

    private List<WeEventTopicSession> sessions;
    private Map<String, WeEventTopicSubscriber> subscribers;

    public WeEventTopicConnection(WebSocketTransport transport) {
        this.transport = transport;
        this.sessions = new ArrayList<>();
        this.subscribers = new HashMap<>();
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addSession(WeEventTopicSession session) {
        this.sessions.add(session);
    }

    public void removeSession(WeEventTopicSession session) {
        this.sessions.remove(session);
    }

    public void checkConnected() throws JMSException {
        if (!this.transport.isConnected()) {
            this.transport.stompConnect(this.userName, this.password);
        }
    }

    public void doStop() throws JMSException {
        if (this.transport.isConnected()) {
            this.transport.stompDisconnect();
        }
    }

    @Override
    public void dispatch(WeEventStompCommand command) {
        if (this.subscribers.containsKey(command.getSubscriptionId())) {
            this.subscribers.get(command.getSubscriptionId()).dispatch(command);
        }
    }

    public void publish(WeEventTopic topic, BytesMessage bytesMessage) throws JMSException {
        checkConnected();

        this.transport.stompSend(topic, bytesMessage);
    }

    public void createSubscriber(WeEventTopicSubscriber subscriber) throws JMSException {
        WeEventTopic topic = (WeEventTopic) subscriber.getTopic();
        String subscriptionId = this.transport.stompSubscribe(topic, topic.getOffset());
        subscriber.setSubscriptionId(subscriptionId);

        this.subscribers.put(subscriber.getSubscriptionId(), subscriber);
    }

    public void destroySubscriber(String subscriptionId) throws JMSException {
        if (this.subscribers.containsKey(subscriptionId)) {
            this.transport.stompUnsubscribe(subscriptionId);
            this.subscribers.remove(subscriptionId);
        }
    }

    // TopicConnection override methods

    @Override
    public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException {
        if (transacted || acknowledgeMode != Session.AUTO_ACKNOWLEDGE) {
            throw new JMSException(WeEventConnectionFactory.NotSupportTips);
        }

        WeEventTopicSession session = new WeEventTopicSession(this, WeEventConnectionFactory.genUniqueID());
        return session;
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Topic topic, String s, ServerSessionPool serverSessionPool, int i) throws JMSException {
        return null;
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String s, String s1, ServerSessionPool serverSessionPool, int i) throws JMSException {
        return null;
    }


    // Connection override methods

    @Override
    public Session createSession(boolean b, int i) throws JMSException {
        return null;
    }

    @Override
    public String getClientID() throws JMSException {
        return this.clientID;
    }

    @Override
    public void setClientID(String clientID) throws JMSException {
        this.clientID = clientID;
    }

    @Override
    public ConnectionMetaData getMetaData() throws JMSException {
        return null;
    }

    @Override
    public ExceptionListener getExceptionListener() throws JMSException {
        return null;
    }

    @Override
    public void setExceptionListener(ExceptionListener exceptionListener) throws JMSException {

    }

    @Override
    public void start() throws JMSException {
        checkConnected();

        for (WeEventTopicSession s : this.sessions) {
            s.start();
        }
    }

    @Override
    public void stop() throws JMSException {
        doStop();

        for (WeEventTopicSession s : this.sessions) {
            s.stop();
        }
    }

    @Override
    public void close() throws JMSException {
        this.stop();

        this.sessions.clear();
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String s, ServerSessionPool serverSessionPool, int i) throws JMSException {
        return null;
    }
}
