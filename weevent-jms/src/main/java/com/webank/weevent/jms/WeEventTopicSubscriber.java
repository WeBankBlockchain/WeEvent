package com.webank.weevent.jms;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import com.webank.weevent.client.WeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * WeEvent JMS TopicSubscriber.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Slf4j
public class WeEventTopicSubscriber implements TopicSubscriber, CommandDispatcher {
    private WeEventTopic topic;
    private String subscriptionId;
    private String headerId;
    private MessageListener messageListener;

    public WeEventTopicSubscriber(WeEventTopic topic) {
        this.topic = topic;
    }

    public String getHeaderId() {
        return headerId;
    }

    public void setHeaderId(String headerId) {
        this.headerId = headerId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @Override
    public void dispatch(WeEvent event) {
        WeEventBytesMessage message = new WeEventBytesMessage();
        try {
            message.setJMSMessageID(event.getEventId());
            message.writeBytes(event.getContent());
            message.setExtensions(event.getExtensions());
            message.setJMSDestination(new WeEventTopic(event.getTopic()));
            this.messageListener.onMessage(message);
        } catch (JMSException e) {
            log.error("write WeEvent into BytesMessage failed", e);
        }
    }

    // TopicSubscriber override methods

    @Override
    public Topic getTopic() {
        return this.topic;
    }

    @Override
    public boolean getNoLocal() {
        return false;
    }

    // MessageConsumer override methods

    @Override
    public String getMessageSelector() {
        return null;
    }

    @Override
    public MessageListener getMessageListener() {
        return this.messageListener;
    }

    @Override
    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public Message receive() {
        return null;
    }

    @Override
    public Message receive(long l) {
        return null;
    }

    @Override
    public Message receiveNoWait() {
        return null;
    }

    @Override
    public void close() {

    }

}
