package com.webank.weevent.client;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * WeEventClient Tester.
 *
 * @author <cristic>
 * @version 1.0
 * @since <pre>05/10/2019</pre>
 */
@Slf4j
public class WeEventClientGroupIdTest {
    private Map<String, String> extensions = new HashMap<>();
    @Rule
    public TestName testName = new TestName();

    private String topicName = "com.weevent.test";
    private String topicName2 = "com.weevent.test2";

    private IWeEventClient weEventClient;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.extensions.put(WeEvent.WeEvent_TAG, "test");
        this.weEventClient = IWeEventClient.builder().brokerUrl("http://localhost:7000/weevent-broker").groupId(WeEvent.DEFAULT_GROUP_ID).build();
        this.weEventClient.open(this.topicName);
        this.weEventClient.open(this.topicName2);
    }

    @After
    public void after() throws Exception {
        this.weEventClient.close(this.topicName);
        this.weEventClient.close(this.topicName2);
    }

    /**
     * test build WeEventClient groupId not exist
     */
    @Test
    public void testBuildWeEventClientGroupIdNotExist() {
        try {
            IWeEventClient.builder().brokerUrl("http://localhost:7000/weevent-broker").groupId("notExist").build();
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * Method: publish(WeEvent weEvent)
     */
    @Test
    public void testPublish() throws Exception {
        // test publish
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
        Assert.assertFalse(sendResult.getEventId().isEmpty());
    }

    /**
     * test eventId
     */
    @Test
    public void testPublishEventId() throws Exception {
        // test eventId
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);

        WeEvent weEventResponse = this.weEventClient.getEvent(sendResult.getEventId());
        Assert.assertEquals(this.topicName, weEventResponse.getTopic());
    }

    /**
     * test extensions
     */
    @Test
    public void testPublishExtensions() throws Exception {
        // test extensions
        this.extensions.put(WeEvent.WeEvent_FORMAT, "json");
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);

        WeEvent weEventResponse = this.weEventClient.getEvent(sendResult.getEventId());

        Assert.assertNotNull(weEventResponse.getExtensions());
        Assert.assertEquals("test", weEventResponse.getExtensions().get(WeEvent.WeEvent_TAG));
        Assert.assertEquals("json", weEventResponse.getExtensions().get(WeEvent.WeEvent_FORMAT));

    }

    /**
     * test empty content and extensions
     */
    @Test(expected = BrokerException.class)
    public void testPublish001() throws Exception {
        WeEvent weEvent = new WeEvent(this.topicName, null, null);
        this.weEventClient.publish(weEvent);
    }

    /**
     * test empty topic
     */
    @Test(expected = BrokerException.class)
    public void testPublish002() throws Exception {
        WeEvent weEvent = new WeEvent(null, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        this.weEventClient.publish(weEvent);
    }

    /**
     * test empty content
     */
    @Test(expected = BrokerException.class)
    public void testPublish003() throws Exception {
        WeEvent weEvent = new WeEvent("this topic is not exist", null, extensions);
        this.weEventClient.publish(weEvent);
    }

    /**
     * test  extensions
     */
    @Test
    public void testPublish005() throws Exception {
        // test extensions
        this.extensions = new HashMap<>();
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), extensions);
        SendResult result = this.weEventClient.publish(weEvent);
        Assert.assertNotNull(result);
    }

    /**
     * Method: subscribe(String topic, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribe() throws Exception {
        // create subscriber
        String subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, extensions, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info("onEvent: {}", event);

                Assert.assertFalse(event.getTopic().isEmpty());
                Assert.assertFalse(event.getEventId().isEmpty());
            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
                Assert.fail();
            }
        });
        Assert.assertNotNull(subscriptionId);
    }

    /**
     * Method: subscribe(String topic, groupId, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testContinueSubscribe() throws Exception {
        // create subscriber
        String subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, extensions, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
        Assert.assertNotNull(subscriptionId);
        boolean unSubscribeResult = this.weEventClient.unSubscribe(subscriptionId);
        Assert.assertTrue(unSubscribeResult);

        // continue subscribe
        extensions.put(WeEvent.WeEvent_SubscriptionId, subscriptionId);
        subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, extensions, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });

        Assert.assertNotNull(subscriptionId);
    }

    /**
     * Method: subscribe(String[] topic, groupId, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testMultipleTopicSubscribe() throws Exception {
        String[] topics = {this.topicName, this.topicName2};
        // create subscriber
        String subscriptionId = this.weEventClient.subscribe(topics, WeEvent.OFFSET_LAST, extensions, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
        Assert.assertNotNull(subscriptionId);
    }


    /**
     * Method: subscribe(String topic, groupId, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribeWildCard() throws Exception {
        // create subscriber
        String subscriptionId = this.weEventClient.subscribe("com.weevent.test/#", WeEvent.OFFSET_LAST, extensions, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });

        Assert.assertNotNull(subscriptionId);
    }

    /**
     * Method: unSubscribe(String subscriptionId)
     */
    @Test
    public void testUnSubscribe() throws Exception {
        String subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, extensions, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });

        Assert.assertFalse(subscriptionId.isEmpty());
        boolean result = this.weEventClient.unSubscribe(subscriptionId);
        Assert.assertTrue(result);
    }

    /**
     * Method: open(String topic)
     */
    @Test
    public void testOpen() throws Exception {
        boolean result = this.weEventClient.open(this.topicName);
        Assert.assertTrue(result);
    }


    /**
     * Method: open(String topic,String groupId)
     */
    @Test
    public void testOpenGroupId() throws Exception {
        boolean result = this.weEventClient.open(this.topicName);
        Assert.assertTrue(result);
    }

    /**
     * Method: close(String topic)
     */
    @Test
    public void testClose() throws Exception {
        boolean result = this.weEventClient.close(this.topicName);
        Assert.assertTrue(result);
    }


    /**
     * Method: testCloseGroupId(String topic,String groupId)
     */
    @Test
    public void testCloseGroupId() throws Exception {
        boolean result = this.weEventClient.close(topicName);
        Assert.assertTrue(result);
    }


    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExistGroupId() throws Exception {
        boolean result = this.weEventClient.exist(this.topicName);
        Assert.assertTrue(result);
    }

    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExist() throws Exception {
        boolean result = this.weEventClient.exist(this.topicName);
        Assert.assertTrue(result);
    }

    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExist001() throws Exception {
        boolean result = this.weEventClient.exist("not exist");
        Assert.assertFalse(result);
    }

    /**
     * Method: testListGroupId(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testListGroupId() throws Exception {
        TopicPage topicPage = this.weEventClient.list(0, 10);
        Assert.assertTrue(topicPage.getTotal() > 0);
    }


    /**
     * Method: list(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testList() throws Exception {
        TopicPage topicPage = this.weEventClient.list(0, 10);
        Assert.assertTrue(topicPage.getTotal() > 0);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testStateGroupId() throws Exception {
        TopicInfo topicInfo = this.weEventClient.state(this.topicName);
        Assert.assertEquals(topicInfo.getTopicName(), this.topicName);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testState() throws Exception {
        TopicInfo topicInfo = this.weEventClient.state(this.topicName);
        Assert.assertEquals(topicInfo.getTopicName(), this.topicName);
    }

    /**
     * Method: getEvent(String eventId)
     */
    @Test
    public void testGetEvent() throws Exception {
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), extensions);
        SendResult result = this.weEventClient.publish(weEvent);
        Assert.assertNotNull(result.getStatus());
        WeEvent event = this.weEventClient.getEvent(result.getEventId());
        Assert.assertNotNull(event);
        Assert.assertEquals("hello world", new String(event.getContent(), StandardCharsets.UTF_8));
    }

    /**
     * Method: getEvent(String eventId)
     */
    @Test(expected = BrokerException.class)
    public void testGetEventIdNotExist() throws Exception {
        this.weEventClient.getEvent("not exist");
    }

    /**
     * Method: publish(WeEvent weEvent)
     */
    @Test
    public void testPublishRepeat() throws Exception {
        // test publish
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);

        for (int i = 0; i <5 ; i++) {
            SendResult sendResult = this.weEventClient.publish(weEvent);
            Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
            Assert.assertFalse(sendResult.getEventId().isEmpty());
        }
    }
}
