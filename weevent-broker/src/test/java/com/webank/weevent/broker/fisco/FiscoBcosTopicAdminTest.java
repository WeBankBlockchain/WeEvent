package com.webank.weevent.broker.fisco;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * FiscoBcosTopicAdmin Tester.
 *
 * @author websterchen
 * @author matthewliu
 * @since 11/08/2018
 */
@Slf4j
public class FiscoBcosTopicAdminTest extends JUnitTestBase {
    private IProducer iProducer;
    private String eventId = "";

    @Before
    public void before() throws Exception {
        this.iProducer = IProducer.build();
        Assert.assertNotNull(this.iProducer);
        this.iProducer.startProducer();

        SendResult sendResult = this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()), this.groupId);
        Assert.assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
        this.eventId = sendResult.getEventId();
    }

    @After
    public void after() {
        Assert.assertTrue(this.iProducer.shutdownProducer());
    }

    @Test
    public void testExist() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        Assert.assertTrue(this.iProducer.exist(this.topicName, this.groupId));
    }

    @Test
    public void state() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        TopicInfo topicInfo = this.iProducer.state(this.topicName, this.groupId);
        Assert.assertNotNull(topicInfo);
        Assert.assertFalse(topicInfo.getTopicAddress().isEmpty());
        Assert.assertFalse(topicInfo.getSenderAddress().isEmpty());
        Assert.assertTrue(topicInfo.getCreatedTimestamp() != 0);
    }

    @Test
    public void testList() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        TopicPage topicPage = this.iProducer.list(1, 10, this.groupId);
        Assert.assertTrue(topicPage.getTotal() > 0);
    }

    /**
     * open topic exist
     */
    @Test
    public void testOpenTopicExist() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        boolean result = this.iProducer.open(this.topicName, this.groupId);
        Assert.assertTrue(result);
    }

    /**
     * topic not exist
     */
    @Test
    public void testOpenTopicNotExist() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String topicStr = "testtopic" + System.currentTimeMillis();
        boolean result = this.iProducer.open(topicStr, this.groupId);
        Assert.assertTrue(result);
    }

    /**
     * topic length > 64
     */
    @Test
    public void testOpenTopicOverMaxLen() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String topicStr = "topiclengthlonger64asdfghjklpoiuytrewqazxswcdevfrbg-" + System.currentTimeMillis();
            this.iProducer.open(topicStr, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * topic length = 64
     */
    @Test
    public void testOpenTopicLenEqual64() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String topicStr = "topiclengthequal64zxcvbnmlkjhgfdsaqwertyuioplokiuj-" + System.currentTimeMillis();
        boolean result = this.iProducer.open(topicStr, this.groupId);
        Assert.assertTrue(result);
    }

    /**
     * topic is null
     */
    @Test
    public void testOpenTopicIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.open(null, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic is blank " "
     */
    @Test
    public void testOpenTopicIsBlank() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.open("", this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic contain special char
     */
    @Test
    public void testOpenTopicContainSpecialChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            this.iProducer.open(illegalTopic, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topic contain Chinese char
     */
    @Test
    public void testOpenTopicContainChiChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.open("中国", this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * groupId is null
     */
    @Test
    public void testOpenGroupIdIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.open(this.topicName, null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testOpenGroupIdIsNotNum() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.open(this.topicName, "sdfsg");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId is not exist
     */
    @Test
    public void testOpenGroupIdIsNotExist() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.open(this.topicName, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * topic exist
     */
    @Test
    public void testCloseTopicExist() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        Assert.assertTrue(this.iProducer.close(this.topicName, this.groupId));
    }

    /**
     * topic is null
     */
    @Test
    public void testCloseTopicIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            Assert.assertFalse(this.iProducer.close(null, this.groupId));
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic is blank " "
     */
    @Test
    public void testCloseTopicIsBlank() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.close(" ", this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic length > 64
     */
    @Test
    public void testCloseTopicOverMaxLen() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String topicStr = "topiclengthlonger64azxsqwedcvfrtgbnhyujmkiolpoiuytr-" + System.currentTimeMillis();
            this.iProducer.close(topicStr, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * topic contain special char
     */
    @Test
    public void testCloseTopicContainSpecialChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            this.iProducer.close(illegalTopic, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topic contain Chinese char
     */
    @Test
    public void testCloseTopicContainChiChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.close("中国", groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * groupId is null
     */
    @Test
    public void testCloseGroupIdIsNull() {
        try {
            this.iProducer.close(this.topicName, null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testCloseGroupIdIsNotNum() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.close(this.topicName, "sdfsg");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId not exist
     */
    @Test
    public void testCloseGroupIdNotExist() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.close(this.topicName, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * topic exist
     */
    @Test
    public void testExistTopicExist() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        boolean result = this.iProducer.exist(this.topicName, this.groupId);
        Assert.assertTrue(result);
    }

    /**
     * topic not exist
     */
    @Test
    public void testExistTopicNotExist() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String falseTopic = "sdlkufhdsighfskhdsf";
        Assert.assertFalse(this.iProducer.exist(falseTopic, this.groupId));
    }

    /**
     * topic is null
     */
    @Test
    public void testExistTopicIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.exist(null, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic length > 64
     */
    @Test
    public void testExistTopicOverMaxlen() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String falseTopic = "fasssglsjggtyuioplkjhgfdsaqwezxcvqazxswedcvfrtgbnhyujmkiolpoiuytr";
            this.iProducer.exist(falseTopic, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * topic is blank " "
     */
    @Test
    public void testExistTopicIsBlank() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String falseTopic = " ";
            this.iProducer.exist(falseTopic, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic contain Chinese char
     */
    @Test
    public void testExistTopicContainChiChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.exist("中国", this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * groupId is null
     */
    @Test
    public void testExistGroupIdIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.exist(this.topicName, null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testExistGroupIdIsNotNum() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.exist(this.topicName, "sdfsg");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId not exist
     */
    @Test
    public void testExistGroupIdNotExist() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.exist(this.topicName, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * topic exist
     */
    @Test
    public void testStateTopicExist() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        TopicInfo topicInfo = this.iProducer.state(this.topicName, this.groupId);
        Assert.assertNotNull(topicInfo);
        Assert.assertFalse(topicInfo.getTopicAddress().isEmpty());
        Assert.assertFalse(topicInfo.getSenderAddress().isEmpty());
        Assert.assertTrue(topicInfo.getCreatedTimestamp().intValue() != 0);
    }

    /**
     * topic length > 64s
     */
    @Test
    public void testStateTopicOverMaxLen() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String lengthTopic = "hdflsjglsgqwertyuioplkjhgfdsazxcvbqwertyuioplkjhgfdsazxcvbnmkoiujy";
            this.iProducer.state(lengthTopic, this.groupId);
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * topic not exist
     */
    @Test
    public void testStateTopicNotExist() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String notExistTopic = "hdflsjglsg";
            this.iProducer.state(notExistTopic, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_NOT_EXIST.getCode(), e.getCode());
        }
    }

    /**
     * topic is null
     */
    @Test
    public void testStateTopicIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.state(null, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic is blank
     */
    @Test
    public void testStateTopicIsBlank() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.state(" ", this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
        }
    }

    /**
     * topic contain special char
     */
    @Test
    public void testStateTopicContainSpecialChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            this.iProducer.state(illegalTopic, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * topic contain Chinese character
     */
    @Test
    public void testStateTopicContainChiChar() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.state("中国", this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
        }
    }

    /**
     * groupId is null
     */
    @Test
    public void testStateGroupIdIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.state(this.topicName, null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testStateGroupIdIsNotNum() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.state(this.topicName, "abc");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId not exist
     */
    @Test
    public void testStateGroupIdNotExist() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.state(this.topicName, "100");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 10
     */
    @Test
    public void testList1() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        TopicPage topicPage = this.iProducer.list(0, 10, this.groupId);
        Assert.assertNotNull(topicPage);
        Assert.assertFalse(topicPage.getTopicInfoList().isEmpty());
    }

    /**
     * list test pageIndex = 1 & pageSize = 10
     */
    @Test
    public void testList2() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        TopicPage topicPage = this.iProducer.list(1, 10, this.groupId);
        Assert.assertTrue(topicPage.getTotal() > 0);
    }

    /**
     * list test pageIndex = null & pageSize = 10
     */
    @Test
    public void testList3() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.list(null, 10, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_PAGE_INDEX_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * list test pageIndex < 0 & pageSize = 10
     */
    @Test
    public void testList4() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.list(-1, 10, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_PAGE_INDEX_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = -1
     */
    @Test
    public void testList5() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.list(0, -1, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 100
     */
    @Test
    public void testList6() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        TopicPage topicPage = this.iProducer.list(0, 100, this.groupId);
        Assert.assertTrue(topicPage.getTotal() > 0);
    }

    /**
     * list test pageIndex = 0 & pageSize = 101
     */
    @Test
    public void testList7() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.list(0, 101, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 0
     */
    @Test
    public void testList8() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.list(0, 0, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = null
     */
    @Test
    public void testList9() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.list(0, null, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * list test pageIndex is out of all page
     */
    @Test
    public void testList10() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        TopicPage topicPage = this.iProducer.list(1000, 50, this.groupId);
        Assert.assertNotNull(topicPage);
        Assert.assertTrue(topicPage.getTotal() > 0);
        Assert.assertTrue(topicPage.getTopicInfoList().isEmpty());
    }

    /**
     * list test ,groupId is null
     */
    @Test
    public void testListGroupIdIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.list(0, 10, null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * list test ,groupId is not number
     */
    @Test
    public void testListGroupIdIsNotNum() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.list(0, 10, "sdfsg");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * groupId not Exist
     */
    @Test
    public void testListGroupIdNotExist() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.list(0, 10, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * test get Event : eventId is exist
     */
    @Test
    public void testGetEventEventIdExist() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        WeEvent weEvent = this.iProducer.getEvent(this.eventId, this.groupId);
        Assert.assertEquals(weEvent.getEventId(), this.eventId);
    }

    /**
     * test get Event : eventId is illegal1
     */
    @Test
    public void testGetEventEventIdIsIllegal() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.getEvent("sfshfwefjf", this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
        }
    }

    /**
     * test get Event : eventId is legal but not exists & eventId > blockNumber
     */
    @Test
    public void testGetEventEventIdHeightGtBlock() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.getEvent("317e7c4c-75-32900000", this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_ID_IS_MISMATCH.getCode(), e.getCode());
        }
    }

    /**
     * test get Event : eventId is legal but not exists
     */
    @Test
    public void testGetEventEventIdLegalNotExist() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.getEvent("317e7c4c-278-3", groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_ID_NOT_EXIST.getCode(), e.getCode());
        }
    }

    /**
     * test get Event : eventId is null
     */
    @Test
    public void testGetEventEventIdIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.getEvent(null, this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
        }
    }

    /**
     * test get Event : eventId is blank
     */
    @Test
    public void testGetEventEventIdIsBlank() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.getEvent(" ", this.groupId);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
        }
    }

    /**
     * test get Event : eventId length > 64
     */
    @Test
    public void testGetEventEventIdOverMaxLen() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            String id = "317e7c4csdxcfvbhjklpoutredwsaqsdfghjkoiuf-2782345678901234567-329";
            WeEvent weEvent = this.iProducer.getEvent(id, this.groupId);
            Assert.assertNull(weEvent);
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode(), e.getCode());
        }
    }

    /**
     * get event test,groupId is null
     */
    @Test
    public void testGetEventGroupIdIsNull() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.getEvent(this.eventId, null);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * get event test ,groupId is not number
     */
    @Test
    public void testGetEventGroupIdIsNotNum() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.getEvent(this.eventId, "sfdsfs");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
        }
    }

    /**
     * get event test ,groupId is not Exist
     */
    @Test
    public void testGetEventGroupIdIsNotExist() {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iProducer.getEvent(this.eventId, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }
}
