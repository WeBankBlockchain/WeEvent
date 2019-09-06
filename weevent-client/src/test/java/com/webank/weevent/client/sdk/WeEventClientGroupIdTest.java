package com.webank.weevent.client.sdk;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

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

    private String topicName = "com.webank.weevent";

    private String groupId = WeEvent.DEFAULT_GROUP_ID;

    private String tag = "1.1.0";

    private String format = "json";

    private IWeEventClient weEventClient;

    @Before
    public void before() throws Exception {
        this.extensions.put("weevent-url", "https://github.com/WeBankFinTech/WeEvent");
        this.weEventClient = IWeEventClient.build("http://localhost:8080/weevent");
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
        log.info("===================={}", this.testName.getMethodName());
        // test groupId
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent, this.groupId);
        log.info("sendResult {}", sendResult);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }


    /**
     * Method: publish(WeEvent weEvent)
     */
    @Test
    public void testPublish() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * test eventId
     */
    @Test
    public void testPublishEventId() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // test eventId
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent, this.groupId);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);

        WeEvent event = this.weEventClient.getEvent(sendResult.getEventId());
        Assert.assertNotNull(event);
        Assert.assertEquals(this.topicName, event.getTopic());
    }

    /**
     * test extensions
     */
    @Test
    public void testPublishExtensions() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // test extensions
        this.extensions.put(WeEvent.WeEvent_FORMAT, "json");
        this.extensions.put(WeEvent.WeEvent_TAG, tag);
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent, this.groupId);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);

        WeEvent event = this.weEventClient.getEvent(sendResult.getEventId());
        Assert.assertNotNull(event);
        Assert.assertNotNull(event.getExtensions());
        Assert.assertEquals(this.tag, event.getExtensions().get(WeEvent.WeEvent_TAG));
        Assert.assertEquals(this.format, event.getExtensions().get(WeEvent.WeEvent_FORMAT));

    }

    /**
     * test empty content、extensions
     */
    @Test(expected = BrokerException.class)
    public void testPublish_001() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        WeEvent weEvent = new WeEvent(this.topicName, null, null);
        this.weEventClient.publish(weEvent, this.groupId);
    }

    /**
     * test empty topic
     */
    @Test(expected = BrokerException.class)
    public void testPublish_002() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        WeEvent weEvent = new WeEvent(null, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        this.weEventClient.publish(weEvent, this.groupId);
    }

    /**
     * test empty content
     */
    @Test(expected = BrokerException.class)
    public void testPublish_003() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        WeEvent weEvent = new WeEvent("this topic is not exist", null, extensions);
        this.weEventClient.publish(weEvent);
    }

    /**
     * test empty groupId
     */
    @Test(expected = BrokerException.class)
    public void testPublish_004() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // test groupId
        WeEvent weEvent = new WeEvent("this topic is not exist", "hello world".getBytes(StandardCharsets.UTF_8), extensions);
        this.weEventClient.publish(weEvent, null);
    }

    /**
     * test  extensions
     */
    @Test(expected = BrokerException.class)
    public void testPublish_005() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // test extensions
        this.extensions = new HashMap<>();
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), extensions);
        this.weEventClient.publish(weEvent, this.groupId);
    }

    /**
     * Method: subscribe(String topic, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // create subscriber
        String subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                System.out.println(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
        Assert.assertFalse(subscriptionId.isEmpty());
        Thread.sleep(60000);
    }

    /**
     * Method: subscribe(String topic, groupId, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribe_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // create subscriber
        String subscriptionId = this.weEventClient.subscribe(this.topicName, this.groupId, "317e7c4c-8-26", new IWeEventClient.EventListener() {
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
        Thread.sleep(60000);
    }


    /**
     * Method: subscribe(String topic, groupId, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribeWildCard() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // create subscriber
        String subscriptionId = this.weEventClient.subscribe("com.webank.weevent/#", this.groupId, "447c022f-10-2508", new IWeEventClient.EventListener() {
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
        Thread.sleep(60000);
    }

    /**
     * Method: unSubscribe(String subscriptionId)
     */
    @Test
    public void testUnSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        String groupId = this.groupId;//if not set default 1
        String subscriptionId = this.weEventClient.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
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
        boolean result = this.weEventClient.open(this.topicName, this.groupId);
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
        boolean result = this.weEventClient.close(topicName, this.groupId);
        Assert.assertTrue(result);
    }


    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExistGroupId() throws Exception {
        boolean result = this.weEventClient.exist(this.topicName, this.groupId);
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
    public void testExist_001() throws Exception {
        boolean result = this.weEventClient.exist("not exist", this.groupId);
        Assert.assertFalse(result);
    }

    /**
     * Method: testListGroupId(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testListGroupId() throws Exception {
        TopicPage list = this.weEventClient.list(0, 10, this.groupId);
        Assert.assertTrue(list.getTotal() > 0);
    }


    /**
     * Method: list(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testList() throws Exception {
        TopicPage list = this.weEventClient.list(0, 10);
        Assert.assertTrue(list.getTotal() > 0);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testStateGroupId() throws Exception {
        TopicInfo info = this.weEventClient.state(this.topicName, this.groupId);
        Assert.assertEquals(info.getTopicName(), this.topicName);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testState() throws Exception {
        TopicInfo info = this.weEventClient.state(this.topicName);
        Assert.assertEquals(info.getTopicName(), this.topicName);
    }

    /**
     * Method: testGetEventGroupId(String eventId,String groupId)
     */
    @Test(expected = BrokerException.class)
    public void testGetEventGroupId() throws Exception {
        this.weEventClient.getEvent("not exist", this.groupId);
    }

    /**
     * Method: getEvent(String eventId)
     */
    @Test(expected = BrokerException.class)
    public void testGetEvent() throws Exception {
        this.weEventClient.getEvent("not exist");
    }
}
