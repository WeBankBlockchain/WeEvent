package com.webank.weevent.jms;

import java.nio.charset.StandardCharsets;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

@Slf4j
public class JMSTest {

    private final String topicName = "com.weevent.test";
    private final String defaultBrokerUrl = "ws://localhost:7000/weevent-broker/stomp";
    private TopicConnectionFactory connectionFactory;
    private TopicConnection connection;
    private WeEventTopicSession session;
    private WeEventTopic topic;
    private final long wait3s = 3000L;


    @Rule
    public TestName testName = new TestName();

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.connectionFactory = new WeEventConnectionFactory();
        this.connection = this.connectionFactory.createTopicConnection();
        this.session = (WeEventTopicSession) this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topic = (WeEventTopic) this.session.createTopic(this.topicName);
    }

    @After
    public void after() throws Exception {
        this.connection.close();
    }

    /**
     * test connect
     */
    @Test
    public void testConnection() throws JMSException {
        Assert.assertNotNull(this.connection.getClientID());
    }

    /**
     * test groupId is not exist
     */
    @Test
    public void testGroupIdIsNotExist() {
        try {
            this.connectionFactory = new WeEventConnectionFactory(this.defaultBrokerUrl, "0");
            this.connection = this.connectionFactory.createTopicConnection();
            this.session = (WeEventTopicSession) this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            this.topic = (WeEventTopic) this.session.createTopic(this.topicName);
            Assert.fail();
        } catch (JMSException e) {
            Assert.assertEquals(String.valueOf(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode()), e.getErrorCode());
        }
    }

    /**
     * test create connection with userName and password
     */
    @Test
    public void testConnectionWithUserNamePassword() throws JMSException {
        this.connectionFactory = new WeEventConnectionFactory("", "", this.defaultBrokerUrl, WeEvent.DEFAULT_GROUP_ID);
        this.connection = this.connectionFactory.createTopicConnection();
        this.session = (WeEventTopicSession) this.connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        this.topic = (WeEventTopic) this.session.createTopic(this.topicName);
        Assert.assertTrue(true);
    }

    /**
     * test create topic
     */
    @Test
    public void testCreateTopic() throws JMSException {
        WeEventTopic topic = (WeEventTopic) this.session.createTopic("aa");
        Assert.assertNotNull(topic.getTopicName());
    }

    /**
     * topic is blank
     */
    @Test
    public void testCreateTopicIsBlank() {
        try {
            this.session.createTopic("");
            Assert.fail();
        } catch (JMSException e) {
            Assert.assertEquals(String.valueOf(ErrorCode.TOPIC_IS_BLANK.getCode()), e.getErrorCode());
        }
    }

    /**
     * topic length > 64
     */
    @Test
    public void testCreateTopicOverMaxLen() {
        try {
            this.session.createTopic("topiclengthlonger64asdfghjklpoiuytrewqazxswcdevfrbg-" + System.currentTimeMillis());
            Assert.fail();
        } catch (JMSException e) {
            Assert.assertEquals(String.valueOf(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode()), e.getErrorCode());
        }
    }

    /**
     * test subscriber
     */
    @Test
    public void testSubscriber() throws JMSException {
        WeEventTopicSubscriber subscriber = (WeEventTopicSubscriber) this.session.createSubscriber(this.topic);
        subscriber.setMessageListener(message -> {
            BytesMessage msg = (BytesMessage) message;
            try {
                byte[] data = new byte[(int) msg.getBodyLength()];
                msg.readBytes(data);
                System.out.println("received: " + new String(data, StandardCharsets.UTF_8));
                Assert.assertTrue(true);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
        Assert.assertNotNull(subscriber.getSubscriptionId());
    }

    /**
     * test publish
     */
    @Test
    public void testPublish() throws JMSException {
        WeEventTopicPublisher publisher = (WeEventTopicPublisher) this.session.createPublisher(this.topic);
        BytesMessage msg = session.createBytesMessage();
        msg.writeBytes(("hello WeEvent").getBytes(StandardCharsets.UTF_8));
        publisher.publish(msg);
        Assert.assertTrue(true);
    }

    /**
     * test both subscribe and publish
     */
    @Test
    public void testBothSubscribePublish() throws JMSException, InterruptedException {
        WeEventTopicSubscriber subscriber = (WeEventTopicSubscriber) this.session.createSubscriber(this.topic);
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
        Assert.assertNotNull(subscriber.getSubscriptionId());

        WeEventTopicPublisher publisher = (WeEventTopicPublisher) this.session.createPublisher(this.topic);
        BytesMessage msg = session.createBytesMessage();
        msg.writeBytes(("hello WeEvent").getBytes(StandardCharsets.UTF_8));
        publisher.publish(msg);

        Thread.sleep(this.wait3s);
        Assert.assertTrue(true);
    }

}
