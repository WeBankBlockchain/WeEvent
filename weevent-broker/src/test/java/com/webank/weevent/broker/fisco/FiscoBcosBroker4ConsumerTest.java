package com.webank.weevent.broker.fisco;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
import org.junit.Test;

/**
 * FiscoBcosBroker4Consumer Tester.
 *
 * @author matthewliu
 * @version 1.0
 * @since 11/08/2018
 */
@Slf4j
public class FiscoBcosBroker4ConsumerTest extends JUnitTestBase {
    private final String topic2 = topicName + "1";
    private final String topic3 = topicName + "2";
    private final long wait3s = 3000;

    private IProducer iProducer;
    private IConsumer iConsumer;
    private String lastEventId = "";
    private Map<IConsumer.SubscribeExt, String> ext = new HashMap<>();

    class MyConsumerListener implements IConsumer.ConsumerListener {
        int received = 0;

        @Override
        public void onEvent(String subscriptionId, WeEvent event) {
            log.info("********** {}", event);

            received++;
        }

        @Override
        public void onException(Throwable e) {
            log.info("********** {}", e);

            received = -10000;
        }
    }

    private IConsumer.ConsumerListener defaultListener = new MyConsumerListener();

    @Before
    public void before() throws Exception {
        this.iProducer = IProducer.build();
        this.iConsumer = IConsumer.build();
        Assert.assertTrue(this.iProducer.startProducer());
        Assert.assertTrue(this.iConsumer.startConsumer());
        Assert.assertTrue(this.iProducer.open(this.topicName, this.groupId));
        Assert.assertTrue(this.iProducer.open(this.topic2, this.groupId));
        Assert.assertTrue(this.iProducer.open(this.topic3, this.groupId));

        if (StringUtils.isBlank(this.lastEventId)) {
            String data = String.format("hello world! %s", System.currentTimeMillis());
            WeEvent weEvent = new WeEvent(this.topicName, data.getBytes());
            SendResult sendResultDto = this.iProducer.publish(weEvent, this.groupId);
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
    public void testSingleSubscribe_topicIsBlank() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe("", this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * test topic is " "
     */
    @Test
    public void testSingleSubscribe_topicIsBlank2() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe("", this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testSingleSubscribe_topicOverMaxLen() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(
                    "qwertyuioplkjhgfdsazxcvbnmlkjhgfjshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff",
                    this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * topic contain special char
     */
    @Test
    public void testSingleSubscribe_containSpecialChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            this.iConsumer.subscribe(illegalTopic, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topic contain Chinese char
     */
    @Test
    public void testSingleSubscribe_containChineseChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe("中国", groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * groupId is null
     */
    @Test
    public void testSingleSubscribe_groupIdIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, null, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testSingleSubscribe_groupIdIsNotNum() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, "abc", WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId not exist
     */
    @Test
    public void testSingleSubscribe_groupIdNotExist() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, "100", WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * offset eventId contain height large than block height
     */
    @Test
    public void testSingleSubscribe_offsetNumGtBlock() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, this.groupId, "317e7c4c-75-3290000000",
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
    public void testSingleSubscribe_offsetOverMaxLen() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, this.groupId,
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
    public void testSingleSubscribe_offsetIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, this.groupId, null, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * offset is blank " "
     */
    @Test
    public void testSingleSubscribe_offsetIsBlank() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, this.groupId, " ", this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * listener is null
     */
    @Test
    public void testSingleSubscribe_listenerIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, this.ext, null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode(), e.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribe_lastEventIdCheck_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String result = this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST,
                this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribe_lastEventIdCheck_02() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, this.groupId, "123", this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribe_lastEventIdCheck_03() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, this.groupId,
                    "123456789012345678901234567890123456789012345678901234567890123456",
                    this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribeLastEventID_check_04() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.subscribe(this.topicName, this.groupId, "xxx_xxxx", this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    /**
     * topic not same first subscribe
     */
    @Test
    public void testReSubscribe_topicNotSameFirst() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String subId = this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST,
                    this.ext, this.defaultListener);
            Assert.assertFalse(subId.isEmpty());
            this.ext.put(IConsumer.SubscribeExt.SubscriptionId, subId);
            this.iConsumer.subscribe(topic2, groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_NOT_MATCH.getCode(), e.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribe_lastEventId() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        MyConsumerListener listener = new MyConsumerListener();
        log.info("lastEventId: {}", this.lastEventId);
        String result = this.iConsumer.subscribe(this.topicName, this.groupId, this.lastEventId, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()), this.groupId).getStatus());

        Thread.sleep(this.wait3s);
        Assert.assertTrue(listener.received > 0);
    }

    @Test
    public void testSingleTopicSubscribe_boolean_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        MyConsumerListener listener = new MyConsumerListener();
        String result = this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_FIRST, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()), this.groupId).getStatus());
        Thread.sleep(wait3s * 10);

        Assert.assertTrue(listener.received > 0);
    }

    @Test
    public void testSingleTopicSubscribe_boolean_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        MyConsumerListener listener = new MyConsumerListener();
        String result = this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        log.info("lastEventId: {}", this.lastEventId);
        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()), this.groupId).getStatus());
        Thread.sleep(wait3s);

        Assert.assertTrue(listener.received > 0);
    }

    @Test
    public void testSingleTopicSubscribe_list_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribe_list_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        MyConsumerListener listener = new MyConsumerListener();
        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, this.groupId, this.lastEventId, this.ext, listener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribe_list_04() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, this.groupId, null, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
        }
    }

    @Test
    public void testRepeatTopicSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        // normal
        String result = this.iConsumer.subscribe(this.topicName, this.groupId, this.lastEventId, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());

        // allow again
        result = this.iConsumer.subscribe(this.topicName, this.groupId, this.lastEventId, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testMultipleTopicSubscribe_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String[] topics = {this.topicName, this.topic2, this.topic3};
        String result = this.iConsumer.subscribe(topics, this.groupId, this.lastEventId, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testMultipleTopicSubscribe_02() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String[] topics = {this.topicName, this.topic2, this.topic3};
            this.iConsumer.subscribe(topics, this.groupId, "xxx_xxx", this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    @Test
    public void testMultipleTopicSubscribe_05() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testUnsubscribe_01() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.unSubscribe(null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode());
        }
    }

    @Test
    public void testUnsubscribe_03() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String subscription = this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertFalse(subscription.isEmpty());
        boolean result = this.iConsumer.unSubscribe(subscription);
        Assert.assertTrue(result);
    }

    @Test
    public void testUnsubscribe_04() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String[] topics = {this.topicName, this.topic2};
        String subscription = this.iConsumer.subscribe(topics, this.groupId, this.lastEventId, this.ext, this.defaultListener);
        Assert.assertFalse(subscription.isEmpty());

        boolean result = this.iConsumer.unSubscribe(subscription);
        Assert.assertTrue(result);
    }

    @Test
    public void testSubscribeCharacterSet() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        MyConsumerListener listener = new MyConsumerListener();
        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, this.groupId, this.lastEventId, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        SendResult sendResult = this.iProducer
                .publish(new WeEvent(this.topicName,
                        String.format("我是中文. %s", System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)), this.groupId);

        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
        Thread.sleep(wait3s);
        Assert.assertTrue(listener.received > 0);
    }

    /**
     * subId is null
     */
    @Test
    public void testReSubscribe_subIdIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.ext.put(IConsumer.SubscribeExt.SubscriptionId, null);
            this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * subId is blank
     */
    @Test
    public void testReSubscribe_subIdIsBlank() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.ext.put(IConsumer.SubscribeExt.SubscriptionId, "");
            this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * subId is illegal
     */
    @Test
    public void testReSubscribe_subIdIsIllegal() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.ext.put(IConsumer.SubscribeExt.SubscriptionId, "sdgsgsgdg");
            this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.SUBSCRIPTIONID_FORMAT_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * subId legal but not exist
     */
    @Test
    public void testReSubscribe_subIdIsNotExist() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String origin = "ec1776da-1748-4c68-b0eb-ed3e92f9aadb";
        this.ext.put(IConsumer.SubscribeExt.SubscriptionId, origin);
        String subId = this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertEquals(subId, origin);
    }

    /**
     * topics list topic is " "
     */
    @Test
    public void testMulSubscribe_topicIsBlank() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String[] topics = {""};
            this.iConsumer.subscribe(topics, this.groupId, this.lastEventId, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic length > 64
     */
    @Test
    public void testMulSubscribe_topicOverMaxLen() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String[] topics = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"};
            this.iConsumer.subscribe(topics, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * topics is empty
     */
    @Test
    public void testMulSubscribe_topicsIsEmpty() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String[] topics = {};
            this.iConsumer.subscribe(topics, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_LIST_IS_NULL.getCode(), e.getCode());
        }
    }

    /**
     * topic contain special char
     */
    @Test
    public void testMulSubscribe_topicContainSpecialChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            String[] topics = {illegalTopic};
            this.iConsumer.subscribe(topics, this.groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topic contain Chinese char
     */
    @Test
    public void testMulSubscribe_topicsContainChiChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String[] topics = {"中国"};
            this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topics contain multiple topic
     */
    @Test
    public void testMulSubscribe_containMultipleTopic() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String[] topics = {this.topicName, topic2, topic3};
        String subID = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
        Assert.assertFalse(subID.isEmpty());
    }

    /**
     * groupId is null
     */
    @Test
    public void testMulSubscribe_groupIdIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, null, WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testMulSubscribe_groupIdIsNotNum() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, "abc", WeEvent.OFFSET_LAST, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * offset is null
     */
    @Test
    public void testMulSubscribe_offsetIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, this.groupId, null, this.ext, this.defaultListener);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
        }
    }

    @Test
    public void testSubscribe_PublishTag() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        // normal subscribe
        MyConsumerListener listener = new MyConsumerListener();
        String result = this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        // publish with tag
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, "publish_tag");
        WeEvent event = new WeEvent(this.topicName, "hello world.".getBytes(), ext);
        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(event, this.groupId).getStatus());

        Thread.sleep(this.wait3s);
        Assert.assertTrue(listener.received > 0);
    }

    @Test
    public void testSubscribe_TopicTag() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        // subscribe tag
        String tag = "topic_tag";
        MyConsumerListener listener = new MyConsumerListener();

        this.ext.put(IConsumer.SubscribeExt.TopicTag, tag);
        String result = this.iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, this.ext, listener);
        Assert.assertFalse(result.isEmpty());

        // publish tag
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);
        WeEvent event = new WeEvent(this.topicName, "hello world.".getBytes(), ext);
        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(event, this.groupId).getStatus());

        Thread.sleep(this.wait3s);
        Assert.assertTrue(listener.received > 0);
    }
}
