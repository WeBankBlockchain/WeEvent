package com.webank.weevent.jms;


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

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.IWeEventClient;
import com.webank.weevent.client.WeEvent;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * WeEvent JMS TopicConnection.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Slf4j
public class WeEventTopicConnection implements TopicConnection {
    private String userName = "";
    private String password = "";
    private String clientID = "";
    private IWeEventClient client;

    private List<WeEventTopicSession> sessions;
    private Map<String, WeEventTopicSubscriber> subscribers;

    public WeEventTopicConnection(IWeEventClient client) {
        this.sessions = new ArrayList<>();
        this.subscribers = new HashMap<>();
        this.client = client;
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

    public void createTopic(String topicName) throws JMSException {
        if (StringUtils.isBlank(topicName)) {
            throw WeEventConnectionFactory.error2JMSException(ErrorCode.TOPIC_IS_BLANK);
        }
        try {
            this.client.open(topicName);
        } catch (BrokerException e) {
            log.info("create topic error.", e);
            throw WeEventConnectionFactory.exp2JMSException(e);
        }
    }

    public void publish(WeEventTopic topic, BytesMessage bytesMessage) throws JMSException {
        if (bytesMessage instanceof WeEventBytesMessage) {
            WeEventBytesMessage message = (WeEventBytesMessage) bytesMessage;
            byte[] content = new byte[(int) message.getBodyLength()];
            message.readBytes(content);
            WeEvent weEvent = new WeEvent(topic.getTopicName(), content, message.getExtensions());
            if (message.getExtensions() == null) {
                weEvent.setExtensions(new HashMap<>());
            }
            try {
                this.client.publish(weEvent);
            } catch (BrokerException e) {
                log.error("publish failed.", e);
                throw WeEventConnectionFactory.exp2JMSException(e);
            }
        } else {
            throw new JMSException(WeEventConnectionFactory.NotSupportTips);
        }
    }

    public void createSubscriber(WeEventTopicSubscriber subscriber) throws JMSException {
        try {
            WeEventTopic topic = (WeEventTopic) subscriber.getTopic();
            if (StringUtils.isBlank(topic.getOffset())) {
                topic.setOffset(WeEvent.OFFSET_LAST);
            }
            if (StringUtils.isBlank(topic.getContinueSubscriptionId())) {
                topic.setContinueSubscriptionId("");
            }
            String subscriptionId = this.client.subscribe(topic.getTopicName(), topic.getOffset(), topic.getContinueSubscriptionId(), new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    log.info("onEvent: {}", event.toString());
                    subscriber.dispatch(event);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException:", e);
                }
            });
            subscriber.setSubscriptionId(subscriptionId);
            this.subscribers.put(subscriber.getSubscriptionId(), subscriber);
        } catch (BrokerException e) {
            log.error("subscribe failed.", e);
            throw WeEventConnectionFactory.exp2JMSException(e);
        }
    }

    public void destroySubscriber(String subscriptionId) throws JMSException {
        try {
            if (this.subscribers.containsKey(subscriptionId)) {
                this.client.unSubscribe(subscriptionId);
                this.subscribers.remove(subscriptionId);
            }
        } catch (BrokerException e) {
            log.error("unSubscribe failed.", e);
            throw WeEventConnectionFactory.exp2JMSException(e);
        }
    }

    // TopicConnection override methods

    @Override
    public TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws JMSException {
        if (transacted || acknowledgeMode != Session.AUTO_ACKNOWLEDGE) {
            throw new JMSException(WeEventConnectionFactory.NotSupportTips);
        }
        this.clientID = WeEventConnectionFactory.genUniqueID();
        return new WeEventTopicSession(this);
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Topic topic, String s, ServerSessionPool serverSessionPool, int i) {
        return null;
    }

    @Override
    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String s, String s1, ServerSessionPool serverSessionPool, int i) {
        return null;
    }


    // Connection override methods

    @Override
    public Session createSession(boolean b, int i) {
        return null;
    }

    @Override
    public String getClientID() {
        return this.clientID;
    }

    @Override
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    @Override
    public ConnectionMetaData getMetaData() {
        return null;
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return null;
    }

    @Override
    public void setExceptionListener(ExceptionListener exceptionListener) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void start() {
        for (WeEventTopicSession s : this.sessions) {
            s.start();
        }
    }

    @Override
    public void stop() {
        for (WeEventTopicSession s : this.sessions) {
            s.stop();
        }
    }

    @Override
    public void close() {
        this.stop();

        this.sessions.clear();
    }

    @Override
    public ConnectionConsumer createConnectionConsumer(Destination destination, String s, ServerSessionPool serverSessionPool, int i) {
        return null;
    }

}
