package com.webank.weevent.broker.sample;

import java.nio.charset.StandardCharsets;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.jms.WeEventConnectionFactory;
import com.webank.weevent.sdk.jms.WeEventTopic;


/**
 * Samples of JMS interface.
 *
 * @author matthewliu
 * @since 2019/04/08
 */
public class JMS {
    private final static String topicName = "com.weevent.test";

    private static void publish() throws JMSException {
        // get topic connection
        TopicConnectionFactory connectionFactory = new WeEventConnectionFactory(WeEventConnectionFactory.defaultBrokerUrl);
        TopicConnection connection = connectionFactory.createTopicConnection();

        // start connection
        connection.start();
        // create session
        TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

        // create topic
        Topic topic = session.createTopic(topicName);

        // create publisher
        TopicPublisher publisher = session.createPublisher(topic);
        // send message
        BytesMessage msg = session.createBytesMessage();
        msg.writeBytes(("hello WeEvent").getBytes(StandardCharsets.UTF_8));
        publisher.publish(msg);

        System.out.print("send done.");
        connection.close();
    }

    private static void subscribe() throws JMSException {
        // get topic connection
        TopicConnectionFactory connectionFactory = new WeEventConnectionFactory(WeEventConnectionFactory.defaultBrokerUrl);
        TopicConnection connection = connectionFactory.createTopicConnection();

        // start connection
        connection.start();
        // create session
        TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

        // create topic
        Topic topic = session.createTopic(topicName);
        // optional, default is OFFSET_LAST
        ((WeEventTopic) topic).setOffset(WeEvent.OFFSET_LAST);
        ((WeEventTopic) topic).setGroupId(WeEvent.DEFAULT_GROUP_ID);//if not set default 1

        // create subscriber
        TopicSubscriber subscriber = session.createSubscriber(topic);

        // create listener
        subscriber.setMessageListener(message -> {
            BytesMessage msg = (BytesMessage) message;
            try {
                byte[] data = new byte[(int) msg.getBodyLength()];
                msg.readBytes(data);
                System.out.println("received: " + new String(data, StandardCharsets.UTF_8));
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        connection.close();
    }

    public static void main(String[] args) {
        System.out.println("This is WeEvent JMS sample.");

        try {
            subscribe();

            publish();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
