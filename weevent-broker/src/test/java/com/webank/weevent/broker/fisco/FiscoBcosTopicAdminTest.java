package com.webank.weevent.broker.fisco;

import java.math.BigInteger;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.QueryEntity;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
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
    private QueryEntity queryEntity;
    private String groupId = WeEvent.DEFAULT_GROUP_ID;
    private final BigInteger blockNumber = BigInteger.valueOf(1);


    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

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
        Assert.assertTrue(this.iProducer.exist(this.topicName, this.groupId));
    }

    @Test
    public void state() throws Exception {
        TopicInfo topicInfo = this.iProducer.state(this.topicName, this.groupId);
        Assert.assertNotNull(topicInfo);
        Assert.assertFalse(topicInfo.getSenderAddress().isEmpty());
        Assert.assertTrue(topicInfo.getCreatedTimestamp() != 0);
    }

    @Test
    public void testList() throws Exception {
        TopicPage topicPage = this.iProducer.list(1, 10, this.groupId);
        Assert.assertTrue(topicPage.getTotal() > 0);
    }

    /**
     * open topic exist
     */
    @Test
    public void testOpenTopicExist() throws Exception {
        boolean result = this.iProducer.open(this.topicName, this.groupId);
        Assert.assertTrue(result);
    }

    /**
     * topic not exist
     */
    @Test
    public void testOpenTopicNotExist() throws Exception {
        String topicStr = "testtopic" + System.currentTimeMillis();
        boolean result = this.iProducer.open(topicStr, this.groupId);
        Assert.assertTrue(result);
    }

    /**
     * topic length > 64
     */
    @Test
    public void testOpenTopicOverMaxLen() {
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
        String topicStr = "topiclengthequal64zxcvbnmlkjhgfdsaqwertyuioplokiuj-" + System.currentTimeMillis();
        boolean result = this.iProducer.open(topicStr, this.groupId);
        Assert.assertTrue(result);
    }

    /**
     * topic is null
     */
    @Test
    public void testOpenTopicIsNull() {
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
    public void testOpenGroupIdIsNull() throws BrokerException {
        boolean result = this.iProducer.open(this.topicName, null);
        Assert.assertTrue(result);
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testOpenGroupIdIsNotNum() {
        try {
            this.iProducer.open(this.topicName, "sdfsg");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * groupId is not exist
     */
    @Test
    public void testOpenGroupIdIsNotExist() {
        try {
            this.iProducer.open(this.topicName, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * topic exist
     */
    @Test
    public void testCloseTopicExist() throws Exception {
        Assert.assertTrue(this.iProducer.close(this.topicName, this.groupId));
    }

    /**
     * topic is null
     */
    @Test
    public void testCloseTopicIsNull() {
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
    public void testCloseGroupIdIsNull() throws BrokerException {
        boolean result = this.iProducer.close(this.topicName, null);
        Assert.assertTrue(result);
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testCloseGroupIdIsNotNum() {
        try {
            this.iProducer.close(this.topicName, "sdfsg");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * groupId not exist
     */
    @Test
    public void testCloseGroupIdNotExist() {
        try {
            this.iProducer.close(this.topicName, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * topic exist
     */
    @Test
    public void testExistTopicExist() throws Exception {
        boolean result = this.iProducer.exist(this.topicName, this.groupId);
        Assert.assertTrue(result);
    }

    /**
     * topic not exist
     */
    @Test
    public void testExistTopicNotExist() throws Exception {
        String falseTopic = "sdlkufhdsighfskhdsf";
        Assert.assertFalse(this.iProducer.exist(falseTopic, this.groupId));
    }

    /**
     * topic is null
     */
    @Test
    public void testExistTopicIsNull() {
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
    public void testExistTopicOverMaxLength() {
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
    public void testExistGroupIdIsNull() throws BrokerException {
        boolean exist = this.iProducer.exist(this.topicName, null);
        Assert.assertTrue(exist);
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testExistGroupIdIsNotNum() {
        try {
            this.iProducer.exist(this.topicName, "sdfsg");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * groupId not exist
     */
    @Test
    public void testExistGroupIdNotExist() {
        try {
            this.iProducer.exist(this.topicName, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * topic exist
     */
    @Test
    public void testStateTopicExist() throws Exception {
        TopicInfo topicInfo = this.iProducer.state(this.topicName, this.groupId);
        Assert.assertNotNull(topicInfo);
        Assert.assertFalse(topicInfo.getTopicName().isEmpty());
        Assert.assertFalse(topicInfo.getSenderAddress().isEmpty());
        Assert.assertTrue(topicInfo.getCreatedTimestamp().intValue() != 0);
    }

    /**
     * topic length > 64s
     */
    @Test
    public void testStateTopicOverMaxLen() {
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
    public void testStateGroupIdIsNull() throws BrokerException {
        TopicInfo result = this.iProducer.state(this.topicName, null);
        Assert.assertNotNull(result);
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testStateGroupIdIsNotNum() {
        try {
            this.iProducer.state(this.topicName, "abc");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * groupId not exist
     */
    @Test
    public void testStateGroupIdNotExist() {
        try {
            this.iProducer.state(this.topicName, "100");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 10
     */
    @Test
    public void testList1() throws Exception {
        TopicPage topicPage = this.iProducer.list(0, 10, this.groupId);
        Assert.assertNotNull(topicPage);
        Assert.assertFalse(topicPage.getTopicInfoList().isEmpty());
    }

    /**
     * list test pageIndex = 1 & pageSize = 10
     */
    @Test
    public void testList2() throws Exception {
        TopicPage topicPage = this.iProducer.list(1, 10, this.groupId);
        Assert.assertTrue(topicPage.getTotal() > 0);
    }

    /**
     * list test pageIndex = null & pageSize = 10
     */
    @Test
    public void testList3() {
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
        TopicPage topicPage = this.iProducer.list(0, 100, this.groupId);
        Assert.assertTrue(topicPage.getTotal() > 0);
    }

    /**
     * list test pageIndex = 0 & pageSize = 101
     */
    @Test
    public void testList7() {
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
        TopicPage topicPage = this.iProducer.list(1000, 50, this.groupId);
        Assert.assertNotNull(topicPage);
        Assert.assertTrue(topicPage.getTotal() > 0);
        Assert.assertTrue(topicPage.getTopicInfoList().isEmpty());
    }

    /**
     * list test ,groupId is null
     */
    @Test
    public void testListGroupIdIsNull() throws BrokerException {
        TopicPage result = this.iProducer.list(0, 10, null);
        Assert.assertNotNull(result);
    }

    /**
     * list test ,groupId is not number
     */
    @Test
    public void testListGroupIdIsNotNum() {
        try {
            this.iProducer.list(0, 10, "sdfsg");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * groupId not Exist
     */
    @Test
    public void testListGroupIdNotExist() {
        try {
            this.iProducer.list(0, 10, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * test get Event : eventId is exist
     */
    @Test
    public void testGetEventEventIdExist() throws Exception {
        WeEvent weEvent = this.iProducer.getEvent(this.eventId, this.groupId);
        Assert.assertEquals(weEvent.getEventId(), this.eventId);
    }

    /**
     * test get Event : eventId is illegal1
     */
    @Test
    public void testGetEventEventIdIsIllegal() {
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
    public void testGetEventGroupIdIsNull() throws BrokerException {
        WeEvent event = this.iProducer.getEvent(this.eventId, null);
        Assert.assertNotNull(event);
    }

    /**
     * get event test ,groupId is not number
     */
    @Test
    public void testGetEventGroupIdIsNotNum() {
        try {
            this.iProducer.getEvent(this.eventId, "sfdsfs");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * get event test ,groupId is not Exist
     */
    @Test
    public void testGetEventGroupIdIsNotExist() {
        try {
            this.iProducer.getEvent(this.eventId, "4");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
        }
    }

    /**
     * test getGroupGeneral
     */
    @Test
    public void testGetGroupGeneral() throws BrokerException {
        GroupGeneral groupGeneral = this.iProducer.getGroupGeneral(this.groupId);
        Assert.assertNotNull(groupGeneral);
        Assert.assertNotNull(groupGeneral.getLatestBlock());
        Assert.assertNotNull(groupGeneral.getTransactionCount());
    }

    /**
     * test queryTransList
     */
    @Test
    public void queryTransList() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        queryEntity.setPageNumber(1);
        queryEntity.setPageSize(10);
        ListPage<TbTransHash> tbTransHashes = this.iProducer.queryTransList(queryEntity);

        Assert.assertNotNull(tbTransHashes);
    }


    /**
     * test queryTransList with blockNumber
     */
    @Test
    public void queryTransListBlockNumber() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        queryEntity.setPageSize(10);
        queryEntity.setPageNumber(1);
        queryEntity.setBlockNumber(blockNumber);
        ListPage<TbTransHash> tbTransHashes = this.iProducer.queryTransList(queryEntity);

        Assert.assertNotNull(tbTransHashes);
        Assert.assertTrue(tbTransHashes.getTotal() > 0);
        Assert.assertEquals(tbTransHashes.getPageData().get(0).getBlockNumber().toString(), this.blockNumber.toString());
    }


    /**
     * test queryTransList with tranHash
     */
    @Test
    public void queryTransListTranHash() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        queryEntity.setBlockNumber(new BigInteger("5000"));
        queryEntity.setPageNumber(2);
        queryEntity.setPageSize(10);
        ListPage<TbTransHash> tbTransHashes = this.iProducer.queryTransList(queryEntity);
        Assert.assertNotNull(tbTransHashes);
    }

    /**
     * test queryBlockList
     */
    @Test
    public void queryBlockList() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setPageSize(10);
        queryEntity.setPageNumber(10);
        queryEntity.setGroupId(this.groupId);
        ListPage<TbBlock> tbBlocks = this.iProducer.queryBlockList(queryEntity);
        Assert.assertNotNull(tbBlocks);
        Assert.assertTrue(tbBlocks.getTotal() > 0);
    }

    /**
     * test queryBlockList with blockNumber
     */
    @Test
    public void queryBlockListBlockNumber() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        queryEntity.setBlockNumber(blockNumber);
        queryEntity.setPageSize(10);
        queryEntity.setPageNumber(1);
        ListPage<TbBlock> tbBlocks = this.iProducer.queryBlockList(queryEntity);

        Assert.assertNotNull(tbBlocks);
        Assert.assertTrue(tbBlocks.getTotal() > 0);
        Assert.assertEquals(tbBlocks.getPageData().get(0).getBlockNumber().toString(), this.blockNumber.toString());
    }

    /**
     * test queryBlockList with blockHash
     */
    @Test
    public void queryBlockListBlockHash() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        queryEntity.setBlockNumber(blockNumber);
        queryEntity.setPageSize(10);
        queryEntity.setPageNumber(1);

        ListPage<TbBlock> tbBlocks = this.iProducer.queryBlockList(queryEntity);
        Assert.assertNotNull(tbBlocks);
        Assert.assertTrue(tbBlocks.getTotal() > 0);
    }

    /**
     * test queryNodeList
     */
    @Test
    public void queryNodeList() throws BrokerException {
        this.queryEntity = new QueryEntity();
        queryEntity.setGroupId(this.groupId);
        ListPage<TbNode> tbNodes = this.iProducer.queryNodeList(queryEntity);
        Assert.assertNotNull(tbNodes);
        Assert.assertTrue(tbNodes.getTotal() > 0);
    }


}
