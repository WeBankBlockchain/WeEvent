package com.webank.weevent.client.jms;


import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicPublisher;

import lombok.extern.slf4j.Slf4j;

/**
 * WeEvent JMS TopicPublisher.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Slf4j
public class WeEventTopicPublisher implements TopicPublisher {
    private WeEventTopicSession topicSession;
    private WeEventTopic topic;

    WeEventTopicPublisher(WeEventTopicSession topicSession, WeEventTopic topic) {
        this.topicSession = topicSession;
        this.topic = topic;
    }

    // TopicPublisher override methods
    @Override
    public Topic getTopic() throws JMSException {
        return this.topic;
    }

    @Override
    public void publish(Message message) throws JMSException {
        if (message instanceof BytesMessage) {
            this.topicSession.publish(this.topic, (BytesMessage) message);
        } else {
            throw new JMSException(WeEventConnectionFactory.NotSupportTips);
        }
    }

    @Override
    public void publish(Message message, int i, int i1, long l) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void publish(Topic topic, Message message) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void publish(Topic topic, Message message, int i, int i1, long l) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    // MessageProducer override methods

    @Override
    public void setDisableMessageID(boolean b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public boolean getDisableMessageID() throws JMSException {
        return false;
    }

    @Override
    public void setDisableMessageTimestamp(boolean b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public boolean getDisableMessageTimestamp() throws JMSException {
        return false;
    }

    @Override
    public void setDeliveryMode(int i) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public int getDeliveryMode() throws JMSException {
        return 0;
    }

    @Override
    public void setPriority(int i) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public int getPriority() throws JMSException {
        return 0;
    }

    @Override
    public void setTimeToLive(long l) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public long getTimeToLive() throws JMSException {
        return 0;
    }

    @Override
    public Destination getDestination() throws JMSException {
        return this.topic;
    }

    @Override
    public void close() throws JMSException {
        log.info("close WeEventTopicPublisher.");
    }

    @Override
    public void send(Message message) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void send(Message message, int i, int i1, long l) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void send(Destination destination, Message message) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void send(Destination destination, Message message, int i, int i1, long l) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }
}
