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

    private IWeEventClient weEventClient;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.extensions.put(WeEvent.WeEvent_TAG, "test");
        this.weEventClient = new IWeEventClient.Builder().brokerUrl("http://localhost:7000/weevent-broker").groupId(WeEvent.DEFAULT_GROUP_ID).build();
        this.weEventClient.open(this.topicName);
    }

    @After
    public void after() throws Exception {
        this.weEventClient.close(this.topicName);
    }

    /**
     * Method: publish(WeEvent weEvent, String groupId)
     */
    @Test
    public void testPublishGroupId() throws Exception {
        // test groupId
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent);
        log.info("sendResult {}", sendResult);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }


    /**
     * Method: publish(WeEvent weEvent)
     */
    @Test
    public void testPublish() throws Exception {
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

        BaseResponse<WeEvent> response = this.weEventClient.getEvent(sendResult.getEventId());
        Assert.assertEquals(0, response.getCode());
        Assert.assertEquals(this.topicName, response.getData().getTopic());
    }

    /**
     * test extensions
     */
    @Test
    public void testPublishExtensions() throws Exception {
        // test extensions
        this.extensions.put(WeEvent.WeEvent_FORMAT, "json");
        this.extensions.put(WeEvent.WeEvent_TAG, "test");
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);

        BaseResponse<WeEvent> response = this.weEventClient.getEvent(sendResult.getEventId());
        Assert.assertEquals(0, response.getCode());

        Assert.assertNotNull(response.getData().getExtensions());
        Assert.assertEquals("test", response.getData().getExtensions().get(WeEvent.WeEvent_TAG));
        Assert.assertEquals("json", response.getData().getExtensions().get(WeEvent.WeEvent_FORMAT));

    }

    /**
     * test empty content、extensions
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
        String subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
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
        Assert.assertFalse(subscriptionId.isEmpty());
        Thread.sleep(5000);
    }

    /**
     * Method: subscribe(String topic, groupId, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribe01() throws Exception {
        // create subscriber
        String subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
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
        Thread.sleep(5000);
    }


    /**
     * Method: subscribe(String topic, groupId, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribeWildCard() throws Exception {
        // create subscriber
        String subscriptionId = this.weEventClient.subscribe("com.weevent.test/#", WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
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
        Thread.sleep(5000);
    }

    /**
     * Method: unSubscribe(String subscriptionId)
     */
    @Test
    public void testUnSubscribe() throws Exception {
        String subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
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
        BaseResponse<Boolean> response = this.weEventClient.open(this.topicName);
        Assert.assertEquals(0, response.getCode());
        Assert.assertTrue(response.getData());
    }


    /**
     * Method: open(String topic,String groupId)
     */
    @Test
    public void testOpenGroupId() throws Exception {
        BaseResponse<Boolean> response = this.weEventClient.open(this.topicName);
        Assert.assertEquals(0, response.getCode());
        Assert.assertTrue(response.getData());
    }

    /**
     * Method: close(String topic)
     */
    @Test
    public void testClose() throws Exception {
        BaseResponse<Boolean> response = this.weEventClient.close(this.topicName);
        Assert.assertEquals(0, response.getCode());
        Assert.assertTrue(response.getData());
    }


    /**
     * Method: testCloseGroupId(String topic,String groupId)
     */
    @Test
    public void testCloseGroupId() throws Exception {
        BaseResponse<Boolean> response = this.weEventClient.close(topicName);
        Assert.assertEquals(0, response.getCode());
        Assert.assertTrue(response.getData());
    }


    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExistGroupId() throws Exception {
        BaseResponse<Boolean> response = this.weEventClient.exist(this.topicName);
        Assert.assertEquals(0, response.getCode());
        Assert.assertTrue(response.getData());
    }

    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExist() throws Exception {
        BaseResponse<Boolean> response = this.weEventClient.exist(this.topicName);
        Assert.assertEquals(0, response.getCode());
        Assert.assertTrue(response.getData());
    }

    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExist001() throws Exception {
        BaseResponse<Boolean> response = this.weEventClient.exist("not exist");
        Assert.assertEquals(0, response.getCode());
        Assert.assertFalse(response.getData());
    }

    /**
     * Method: testListGroupId(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testListGroupId() throws Exception {
        BaseResponse<TopicPage> response = this.weEventClient.list(0, 10);
        Assert.assertEquals(0, response.getCode());
        Assert.assertTrue(response.getData().getTotal() > 0);
    }


    /**
     * Method: list(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testList() throws Exception {
        BaseResponse<TopicPage> response = this.weEventClient.list(0, 10);
        Assert.assertEquals(0, response.getCode());
        Assert.assertTrue(response.getData().getTotal() > 0);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testStateGroupId() throws Exception {
        BaseResponse<TopicInfo> response = this.weEventClient.state(this.topicName);
        Assert.assertEquals(0, response.getCode());
        Assert.assertEquals(response.getData().getTopicName(), this.topicName);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testState() throws Exception {
        BaseResponse<TopicInfo> response = this.weEventClient.state(this.topicName);
        Assert.assertEquals(0, response.getCode());
        Assert.assertEquals(response.getData().getTopicName(), this.topicName);
    }

    /**
     * Method: testGetEventGroupId(String eventId,String groupId)
     */
    @Test(expected = BrokerException.class)
    public void testGetEventGroupId() throws Exception {
        this.weEventClient.getEvent("not exist");
    }

    /**
     * Method: getEvent(String eventId)
     */
    @Test(expected = BrokerException.class)
    public void testGetEvent() throws Exception {
        this.weEventClient.getEvent("not exist");
    }
}
