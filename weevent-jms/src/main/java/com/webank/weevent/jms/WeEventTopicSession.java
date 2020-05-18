package com.webank.weevent.jms;


import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.webank.weevent.client.ErrorCode;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * WeEvent JMS TopicSession.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Slf4j
@Getter
@Setter
public class WeEventTopicSession implements TopicSession {
    private WeEventTopicConnection topicConnection;


    public WeEventTopicSession(WeEventTopicConnection connection) {
        this.topicConnection = connection;
        connection.addSession(this);
    }

    public void start() {
        log.info("start topic session.");
    }

    public void stop() {
        log.info("stop topic session");
    }

    public void publish(WeEventTopic topic, BytesMessage bytesMessage) throws JMSException {
        this.topicConnection.publish(topic, bytesMessage);
    }

    // TopicSession override methods

    @Override
    public Topic createTopic(String topicName) throws JMSException {
        this.topicConnection.createTopic(topicName);
        return new WeEventTopic(topicName);
    }

    @Override
    public TopicSubscriber createSubscriber(Topic topic) throws JMSException {
        if (topic instanceof WeEventTopic) {
            WeEventTopicSubscriber subscriber = new WeEventTopicSubscriber((WeEventTopic) topic);
            this.topicConnection.createSubscriber(subscriber);
            return subscriber;
        }
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public TopicSubscriber createSubscriber(Topic topic, String s, boolean b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String s, String s1, boolean b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public TopicPublisher createPublisher(Topic topic) throws JMSException {
        if (StringUtils.isBlank(topic.getTopicName())) {
            throw WeEventConnectionFactory.error2JMSException(ErrorCode.TOPIC_IS_BLANK);
        }
        if (topic instanceof WeEventTopic) {
            return new WeEventTopicPublisher(this, (WeEventTopic) topic);
        }

        return null;
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void unsubscribe(String subscriptionId) throws JMSException {
        this.topicConnection.destroySubscriber(subscriptionId);
    }

    // Session override methods

    @Override
    public BytesMessage createBytesMessage() {
        return new WeEventBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public Message createMessage() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable serializable) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public TextMessage createTextMessage(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public boolean getTransacted() {
        return false;
    }

    @Override
    public int getAcknowledgeMode() {
        return Session.AUTO_ACKNOWLEDGE;
    }

    @Override
    public void commit() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void rollback() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void close() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void recover() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setMessageListener(MessageListener messageListener) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void run() {
        log.info("run task.");
    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String s, boolean b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public Queue createQueue(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }
}
