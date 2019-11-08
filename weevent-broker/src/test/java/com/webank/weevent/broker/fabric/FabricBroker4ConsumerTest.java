package com.webank.weevent.broker.fabric;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * FabricBroker4ConsumerTest Tester.
 *
 * @author v_wbhwliu
 * @version 1.1
 * @since 10/15/2019
 */
@Slf4j
@Ignore("Fabric is not default setting")
public class FabricBroker4ConsumerTest extends JUnitTestBase {
    private final String topic2 = topicName + "1";
    private final String topic3 = topicName + "2";
    private final long wait3s = 3000;

    private IProducer iProducer;
    private IConsumer iConsumer;
    private String lastEventId = "";
    private Map<IConsumer.SubscribeExt, String> ext = new HashMap<>();
    private IConsumer.ConsumerListener defaultListener = new MyConsumerListener();

    class MyConsumerListener implements IConsumer.ConsumerListener {
        public List<String> notifiedEvents = new ArrayList<>();

        @Override
        public void onEvent(String subscriptionId, WeEvent event) {
            log.info("********** {}", event);

            this.notifiedEvents.add(event.getEventId());
        }

        @Override
        public void onException(Throwable e) {
            log.error("**********", e);

            this.notifiedEvents.clear();
        }
    }

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.iProducer = IProducer.build();
        this.iConsumer = IConsumer.build();
        Assert.assertTrue(this.iProducer.startProducer());
        Assert.assertTrue(this.iConsumer.startConsumer());
        Assert.assertTrue(this.iProducer.open(this.topicName, this.channelName));
        Assert.assertTrue(this.iProducer.open(this.topic2, this.channelName));
        Assert.assertTrue(this.iProducer.open(this.topic3, this.channelName));

        if (StringUtils.isBlank(this.lastEventId)) {
            String data = String.format("hello world! %s", System.currentTimeMillis());
            WeEvent weEvent = new WeEvent(this.topicName, data.getBytes());
            SendResult sendResultDto = this.iProducer.publish(weEvent, this.channelName);
            Assert.assertEquals(SendResult.SendResultStatus.SUCCESS, sendResultDto.getStatus());
            this.lastEventId = sendResultDto.getEventId();
            log.info("publish lastEventId: {}", this.lastEventId);
            Assert.assertTrue(this.lastEventId.length() > 1);
        }

        ext.put(IConsumer.SubscribeExt.InterfaceType, "junit");
    }

    @After
    public void after() {
        Assert.assertTrue(this.iProducer.shutdownProducer());
        Assert.assertTrue(this.iConsumer.shutdownConsumer());
        ext.clear();
    }

    /**
     * test topic is ""
     */
    @Test
    public void testSingleSubscribeTopicIsBlank() {
        try {
            this.iConsumer.subscribe("", this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * test topic is " "
     */
    @Test
    public void testSingleSubscribeTopicIsBlank2() {
        try {
            this.iConsumer.subscribe("", this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testSingleSubscribeTopicOverMaxLen() {
        try {
            this.iConsumer.subscribe(
                    "qwertyuioplkjhgfdsazxcvbnmlkjhgfjshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff",
                    this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * topic contain special char
     */
    @Test
    public void testSingleSubscribeContainSpecialChar() {
        try {
            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            this.iConsumer.subscribe(illegalTopic, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topic contain Chinese char
     */
    @Test
    public void testSingleSubscribeContainChineseChar() {
        try {
            this.iConsumer.subscribe("中国", this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * channelName is null
     */
    @Test
    public void testSingleSubscribeChannelNameNull() {
        try {
            this.iConsumer.subscribe(this.topicName, null, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.FABRICSDK_CHANNEL_NAME_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * channelName not exist
     */
    @Test
    public void testSingleSubscribeChannelNameNotExist() {
        try {
            this.iConsumer.subscribe(this.topicName, "test", WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.FABRICSDK_CHANNEL_NAME_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * offset eventId contain height large than block height
     */
    @Test
    public void testSingleSubscribeOffsetNumGtBlock() {
        try {
            this.iConsumer.subscribe(this.topicName, this.channelName, "317e7c4c-75-3290000000",
                    this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_ID_IS_MISMATCH.getCode(), e.getCode());
        }
    }

    /**
     * offset length > 64
     */
    @Test
    public void testSingleSubscribeOffsetOverMaxLen() {
        try {
            this.iConsumer.subscribe(this.topicName, this.channelName,
                    "317e7c4c45gfjfs5369875452364875962-1213456789632145678564547896354775-329", this.ext,
                    this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * offset is null
     */
    @Test
    public void testSingleSubscribeOffsetIsNull() {
        try {
            this.iConsumer.subscribe(this.topicName, this.channelName, null, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * offset is blank " "
     */
    @Test
    public void testSingleSubscribeOffsetIsBlank() {
        try {
            this.iConsumer.subscribe(this.topicName, this.channelName, " ", this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * listener is null
     */
    @Test(expected = NullPointerException.class)
    public void testSingleSubscribeListenerIsNull() {
        try {
            this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST, this.ext, null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.fail();
        }
    }

    @Test
    public void testSingleTopicSubscribeLastEventIdCheck01() throws Exception {
        String result = this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST,
                this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribeLastEventIdCheck02() {
        try {
            this.iConsumer.subscribe(this.topicName, this.channelName, "123", this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribeLastEventIdCheck03() {
        try {
            this.iConsumer.subscribe(this.topicName, this.channelName,
                    "123456789012345678901234567890123456789012345678901234567890123456",
                    this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribeLastEventIDCheck04() {
        try {
            this.iConsumer.subscribe(this.topicName, this.channelName, "xxx_xxxx", this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    /**
     * topic not same first subscribe
     */
    @Test
    public void testReSubscribeTopicNotSameFirst() {
        try {
            String subId = this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST,
                    this.ext, this.defaultListener);
            Assert.assertFalse(subId.isEmpty());
            this.ext.put(IConsumer.SubscribeExt.SubscriptionId, subId);
            this.iConsumer.subscribe(topic2, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.SUBSCRIPTIONID_ALREADY_EXIST.getCode(), e.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribeLastEventId() throws Exception {
        MyConsumerListener listener = new MyConsumerListener();
        log.info("lastEventId: {}", this.lastEventId);
        String result = this.iConsumer.subscribe(this.topicName, this.channelName, this.lastEventId, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()), this.channelName).getStatus());

        Thread.sleep(this.wait3s);

        Assert.assertFalse(listener.notifiedEvents.isEmpty());
        Assert.assertFalse(listener.notifiedEvents.contains(this.lastEventId));
    }

    @Test
    public void testSingleTopicSubscribeBoolean01() throws Exception {
        MyConsumerListener listener = new MyConsumerListener();
        String result = this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_FIRST, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()), this.channelName).getStatus());
        Thread.sleep(wait3s * 10);

        Assert.assertFalse(listener.notifiedEvents.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribeBoolean02() throws Exception {
        MyConsumerListener listener = new MyConsumerListener();
        String result = this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        log.info("lastEventId: {}", this.lastEventId);
        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()), this.channelName).getStatus());
        Thread.sleep(wait3s);

        Assert.assertFalse(listener.notifiedEvents.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribeList01() throws Exception {
        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribeList02() throws Exception {
        MyConsumerListener listener = new MyConsumerListener();
        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, this.channelName, this.lastEventId, this.ext, listener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribeList04() {
        try {
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, this.channelName, null, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
        }
    }

    @Test
    public void testRepeatTopicSubscribe() throws Exception {
        // normal
        String result = this.iConsumer.subscribe(this.topicName, this.channelName, this.lastEventId, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());

        // allow again
        result = this.iConsumer.subscribe(this.topicName, this.channelName, this.lastEventId, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testMultipleTopicSubscribe01() throws Exception {
        String[] topics = {this.topicName, this.topic2, this.topic3};
        String result = this.iConsumer.subscribe(topics, this.channelName, this.lastEventId, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testMultipleTopicSubscribe02() {
        try {
            String[] topics = {this.topicName, this.topic2, this.topic3};
            this.iConsumer.subscribe(topics, this.channelName, "xxx_xxx", this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    @Test
    public void testMultipleTopicSubscribe05() throws Exception {
        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testUnsubscribe01() {
        try {
            this.iConsumer.unSubscribe(null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode());
        }
    }

    @Test
    public void testUnsubscribe03() throws Exception {
        String subscription = this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertFalse(subscription.isEmpty());
        boolean result = this.iConsumer.unSubscribe(subscription);
        Assert.assertTrue(result);
    }

    @Test
    public void testUnsubscribe04() throws Exception {
        String[] topics = {this.topicName, this.topic2};
        String subscription = this.iConsumer.subscribe(topics, this.channelName, this.lastEventId, this.ext, this.defaultListener);
        Assert.assertFalse(subscription.isEmpty());

        boolean result = this.iConsumer.unSubscribe(subscription);
        Assert.assertTrue(result);
    }

    @Test
    public void testSubscribeCharacterSet() throws Exception {
        MyConsumerListener listener = new MyConsumerListener();
        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, this.channelName, this.lastEventId, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        SendResult sendResult = this.iProducer
                .publish(new WeEvent(this.topicName,
                        String.format("我是中文. %s", System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)), this.channelName);

        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
        Thread.sleep(wait3s);

        Assert.assertFalse(listener.notifiedEvents.isEmpty());
        Assert.assertFalse(listener.notifiedEvents.contains(this.lastEventId));
    }

    /**
     * subId is null
     */
    @Test
    public void testReSubscribeSubIdIsNull() {
        try {
            this.ext.put(IConsumer.SubscribeExt.SubscriptionId, null);
            this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * subId is blank
     */
    @Test
    public void testReSubscribeSubIdIsBlank() {
        try {
            this.ext.put(IConsumer.SubscribeExt.SubscriptionId, "");
            this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * subId is illegal
     */
    @Test
    public void testReSubscribeSubIdIsIllegal() {
        try {
            this.ext.put(IConsumer.SubscribeExt.SubscriptionId, "sdgsgsgdg");
            this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.SUBSCRIPTIONID_FORMAT_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * subId legal but not exist
     */
    @Test
    public void testReSubscribeSubIdIsNotExist() throws Exception {
        String origin = "ec1776da-1748-4c68-b0eb-ed3e92f9aadb";
        this.ext.put(IConsumer.SubscribeExt.SubscriptionId, origin);
        String subId = this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertEquals(subId, origin);
    }

    /**
     * topics list topic is " "
     */
    @Test
    public void testMulSubscribeTopicIsBlank() {
        try {
            String[] topics = {""};
            this.iConsumer.subscribe(topics, this.channelName, this.lastEventId, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic length > 64
     */
    @Test
    public void testMulSubscribeTopicOverMaxLen() {
        try {
            String[] topics = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"};
            this.iConsumer.subscribe(topics, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * topics is empty
     */
    @Test
    public void testMulSubscribeTopicsIsEmpty() {
        try {
            String[] topics = {};
            this.iConsumer.subscribe(topics, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_LIST_IS_NULL.getCode(), e.getCode());
        }
    }

    /**
     * topic contain special char
     */
    @Test
    public void testMulSubscribeTopicContainSpecialChar() {
        try {
            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            String[] topics = {illegalTopic};
            this.iConsumer.subscribe(topics, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topic contain Chinese char
     */
    @Test
    public void testMulSubscribeTopicsContainChiChar() {
        try {
            String[] topics = {"中国"};
            this.iConsumer.subscribe(topics, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topics contain multiple topic
     */
    @Test
    public void testMulSubscribeContainMultipleTopic() throws Exception {
        String[] topics = {this.topicName, topic2, topic3};
        String subID = this.iConsumer.subscribe(topics, this.channelName, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertFalse(subID.isEmpty());
    }

    /**
     * channelName is null
     */
    @Test
    public void testMulSubscribeChannelNameIsNull() {
        try {
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, null, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.FABRICSDK_CHANNEL_NAME_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * offset is null
     */
    @Test
    public void testMulSubscribeOffsetIsNull() {
        try {
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, this.channelName, null, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
        }
    }

    @Test
    public void testSubscribePublishTag() throws Exception {
        // normal subscribe
        MyConsumerListener listener = new MyConsumerListener();
        String result = this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        // publish with tag
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, "publish_tag");
        WeEvent event = new WeEvent(this.topicName, "hello world.".getBytes(), ext);
        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(event, this.channelName).getStatus());

        Thread.sleep(this.wait3s);

        Assert.assertFalse(listener.notifiedEvents.isEmpty());
    }

    @Test
    public void testSubscribeTopicTag() throws Exception {
        // subscribe tag
        String tag = "topic_tag";
        MyConsumerListener listener = new MyConsumerListener();

        this.ext.put(IConsumer.SubscribeExt.TopicTag, tag);
        String result = this.iConsumer.subscribe(this.topicName, this.channelName, WeEvent.OFFSET_LAST, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        // publish tag
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);
        WeEvent event = new WeEvent(this.topicName, "hello world.".getBytes(), ext);
        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(event, this.channelName).getStatus());

        Thread.sleep(this.wait3s);

        Assert.assertFalse(listener.notifiedEvents.isEmpty());
    }
}
