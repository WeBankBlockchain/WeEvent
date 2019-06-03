package com.webank.weevent.ST.plugin;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.broker.plugin.IProducer;
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
 * FiscoBcosBroker4Producer Tester.
 *
 * @author websterchen
 * @version 1.0
 * @since 11/08/2018
 */
@Slf4j
public class ProducerTest extends JUnitTestBase {
    private IProducer iProducer;
    private static String eventId = "";
    private String groupId = "1";
    private Map<String, String> extensions = new HashMap<>();

    @Before
    public void before() {
        try {
            iProducer = IProducer.build();
            extensions.put("weevent-url", "https://github.com/WeBankFinTech/WeEvent");
            Assert.assertTrue(iProducer != null);
            Assert.assertTrue(iProducer.open(this.topicName, groupId));
            SendResult result = iProducer.publish(new WeEvent(this.topicName, "你好吗？".getBytes(), extensions), groupId);
            eventId = result.getEventId();
        } catch (BrokerException e) {
            log.error("test method before error: ", e);
        }
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: test startProducer(String topic)
     */
    @Test
    public void testStartProducer() {
        try {
            Assert.assertTrue(iProducer.startProducer());
        } catch (BrokerException e) {
            log.error("start producer error:", e);
        }
    }

    /**
     * Method: test shutdownProducer()
     */
    @Test
    public void testShutdownProducer() {
        Assert.assertTrue(iProducer.shutdownProducer());
    }
    
    /**
     * Method: test shutdownProducer()
     */
    @Test
    public void testShutdownProducer_manyTimes() {
        Assert.assertTrue(iProducer.shutdownProducer());
        Assert.assertTrue(iProducer.shutdownProducer());
        Assert.assertTrue(iProducer.shutdownProducer());
    }

    /**
     * Method: open topic exists
     */
    @Test
    public void testOpen_topicExists() {
        try {
            boolean result = iProducer.open(this.topicName, groupId);
            assertTrue(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertNull(e);
        }
    }

    /**
     * Method: open topic not exists
     */
    @Test
    public void testOpen_topicNotExists() {
        try {
            String topicStr = "testtopic" + System.currentTimeMillis();
            boolean result = iProducer.open(topicStr, groupId);
            assertTrue(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertNull(e);
        }
    }

    /**
     * Method: open topic length > 64
     */
    @Test
    public void testOpen_topicLenGt64() {
        try {
            String topicStr = "topiclengthlonger64asdfghjklpoiuytrewqazxswcdevfrbg-" + System.currentTimeMillis();
            boolean result = iProducer.open(topicStr, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * Method: open topic length equal 64
     */
    @Test
    public void testOpen_topicLenEqual64() {
        try {
            String topicStr = "topiclengthequal64zxcvbnmlkjhgfdsaqwertyuioplokiuj-" + System.currentTimeMillis();
            boolean result = iProducer.open(topicStr, groupId);
            assertTrue(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertNull(e);
        }
    }

    /**
     * Method: open topic is null
     */
    @Test
    public void testOpen_topicIsNull() {
        try {
            boolean result = iProducer.open(null, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: open topic is ""
     */
    @Test
    public void testOpen_topicIsBlank() {
        try {
            boolean result = iProducer.open("", groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: open topic is " "
     */
    @Test
    public void testOpen_topicIsBlank2() {
        try {
            boolean result = iProducer.open(" ", groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId is null
     */
    @Test
    public void testOpen_groupIdIsNull() {
        try {
            boolean result = iProducer.open(this.topicName, null);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId is not number
     */
    @Test
    public void testOpen_groupIdIsNotNum() {
        try {
            boolean result = iProducer.open(this.topicName, "sdfsg");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId = 0
     */
    @Test
    public void testOpen_groupIdIsEqual0() {
        try {
            boolean result = iProducer.open(this.topicName, "0");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId < 0
     */
    @Test
    public void testOpen_groupIdIsLt0() {
        try {
            boolean result = iProducer.open(this.topicName, "-1");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId is not current groupId
     */
    @Test
    public void testOpen_groupIdIsNotCurrentGroupId() {
        try {
            boolean result = iProducer.open(this.topicName, "4");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }

    /**
     * Method: open topic contain special character withoutin[32,128]
     */
    @Test
    public void testOpen_topicContainSpeciaChar() {
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            boolean result = iProducer.open(illegalTopic, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * Method: open topic contain Chinese character
     */
    @Test
    public void testOpen_containChiChar() {
        try {
            boolean result = iProducer.open("中国", groupId);
            assert (result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * Method: Close this.topicName exists
     */
    @Test
    public void testClose_topicExists() {
        try {
            Assert.assertTrue(iProducer.close(this.topicName, groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertNull(e);
        }
    }

    /**
     * Method: Close topic is null
     */
    @Test
    public void testClose_topicIsNull() {
        try {
            Assert.assertFalse(iProducer.close(null, groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: Close topic is""
     */
    @Test
    public void testClose_topicIsBlank() {
        try {
            Assert.assertFalse(iProducer.close("", groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: Close topic is " "
     */
    @Test
    public void testClose_topicIsBlank2() {
        try {
            Assert.assertFalse(iProducer.close(" ", groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: open topic ,groupId is null
     */
    @Test
    public void testClose_groupIdIsNull() {
        try {
            boolean result = iProducer.close(this.topicName, null);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId is not number
     */
    @Test
    public void testClose_groupIdIsNotNum() {
        try {
            boolean result = iProducer.close(this.topicName, "sdfsg");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId = 0
     */
    @Test
    public void testClose_groupIdIsEqual0() {
        try {
            boolean result = iProducer.close(this.topicName, "0");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId < 0
     */
    @Test
    public void testClose_groupIdIsLt0() {
        try {
            boolean result = iProducer.close(this.topicName, "-1");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId is current groupId
     */
    @Test
    public void testClose_groupIdIsNotCurrentGroupId() {
        try {
            boolean result = iProducer.close(this.topicName, "4");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: Close topic length  > 64
     */
    @Test
    public void testClose_topicLenGt64() {
        try {
            String topicStr = "topiclengthlonger64azxsqwedcvfrtgbnhyujmkiolpoiuytr-" 
        	    + System.currentTimeMillis();
            Assert.assertFalse(iProducer.close(topicStr, groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * Method: Close topic topic contain special character withoutin[32,128]
     */
    @Test
    public void testClose_topicContainSpecialChar() {
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            Assert.assertFalse(iProducer.close(illegalTopic, groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * Method: Close topic contain Chinese character
     */
    @Test
    public void testClose_topicContainChiChar() {
	
        try {
            Assert.assertFalse(iProducer.close("中国", groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }


    /**
     * topic exist topic exists
     */
    @Test
    public void testExist_topicExists() {
        try {
            boolean result = iProducer.exist(this.topicName, groupId);
            Assert.assertTrue(result);
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNull(e);
        }
    }

    /**
     * topic not exists
     */
    @Test
    public void testExist_topicNotExists() {
        try {
            String falseTopic = "fasssglsjgg";
            Assert.assertFalse(iProducer.exist(falseTopic, groupId));
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNull(e);
        }
    }

    /**
     * topic is null
     */
    @Test
    public void testExist_topicIsNull() {
        try {
            Assert.assertFalse(iProducer.exist(null, groupId));
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic length > 64
     */
    @Test
    public void testExist_topicLenGt64() {
        try {
            String falseTopic = "fasssglsjggtyuioplkjhgfdsaqwezxcvqazxswedcvfrtgbnhyujmkiolpoiuytr";
            iProducer.exist(falseTopic, groupId);
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * topic blank
     */
    @Test
    public void testExist_topicIsBlank() {
        try {
            String falseTopic = "";
            Assert.assertFalse(iProducer.exist(falseTopic, groupId));
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic blank " "
     */
    @Test
    public void testExist_topicIsBlank2() {
        try {
            String falseTopic = "  ";
            Assert.assertFalse(iProducer.exist(falseTopic, groupId));
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic length = 64 not exists
     */
    @Test
    public void testExist_topicLenEqual64() {
        try {
            String falseTopic = "sdfghjklpoiuytrewqazxcvbnmklopiuqazxswedcvfrtgbnhyujmkiolpoiuytr";
            boolean result = iProducer.exist(falseTopic, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            Assert.assertNull(e);
        }
    }
    
    /**
     * Method: open topic ,groupId is null
     */
    @Test
    public void testExists_groupIdIsNull() {
        try {
            boolean result = iProducer.exist(this.topicName, null);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId is not number
     */
    @Test
    public void testExist_groupIdIsNotNum() {
        try {
            boolean result = iProducer.exist(this.topicName, "sdfsg");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId = 0
     */
    @Test
    public void testExist_groupIdIsEqual0() {
        try {
            boolean result = iProducer.exist(this.topicName, "0");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId < 0
     */
    @Test
    public void testExist_groupIdIsLt0() {
        try {
            boolean result = iProducer.exist(this.topicName, "-1");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * Method: open topic ,groupId is not current groupId
     */
    @Test
    public void testExist_groupIdIsNotCurrentGroupId() {
        try {
            boolean result = iProducer.exist(this.topicName, "4");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }

    /**
     * topic contain special character withoutin [32,128]
     */
    @Test
    public void testExist_topicContainSpecialChar() {
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            boolean result = iProducer.exist(illegalTopic, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * topic contain Chinese character
     */
    @Test
    public void testExist_topicContainChiChar() {
        try {
            boolean result = iProducer.exist("中国", groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * this.topicName is exists
     */
    @Test
    public void testState_topicExists() {
        try {
            TopicInfo topicInfo = iProducer.state(this.topicName, groupId);
            Assert.assertTrue(topicInfo != null);
            Assert.assertTrue(!topicInfo.getTopicAddress().equals(""));
            Assert.assertTrue(!topicInfo.getSenderAddress().equals(""));
            Assert.assertTrue(!(topicInfo.getCreatedTimestamp() == 0));
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNull(e);
        }
    }

    /**
     * topic not exists
     */
    @Test
    public void testState_topicNotExists() {
        try {
            String notExistTopic = "hdflsjglsg";
            iProducer.state(notExistTopic, groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
        }
    }

    /**
     * topic length > 64
     */
    @Test
    public void testState_topicLenGt64() {
        try {
            String lengthTopic = "hdflsjglsgqwertyuioplkjhgfdsazxcvbqwertyuioplkjhgfdsazxcvbnmkoiujy";
            iProducer.state(lengthTopic, groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * topic is null
     */
    @Test
    public void testState_topicIsNull() {
        try {
            iProducer.state(null, groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic is blank ""
     */
    @Test
    public void testState_topicIsBlank() {
        try {
            iProducer.state("", groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic is blank topic " "
     */
    @Test
    public void testState_topicIsBlank2() {
        try {
            iProducer.state(" ", groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic length = 64
     */
    @Test
    public void testState_topicLenEqual64() {
        try {
            String notExistTopic = "hdflsjglsgqwertyuioplkjhgfdsazxcqazxswedcvfrtgbnhyujmkiolppoiuyt";
            iProducer.state(notExistTopic, groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
        }
    }

    /**
     * topic contain special character withoutin [32,128]
     */
    @Test
    public void testState_topicContainSpeciaChar() {
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            iProducer.state(illegalTopic, groupId);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * topic contain Chinese character
     */
    @Test
    public void testState_topicContainChiChar() {
        try {
            iProducer.state("中国", groupId);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 10
     */
    @Test
    public void testList1() {
        Integer pageIndex = 0;
        Integer pageSize = 10;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage != null);
            Assert.assertTrue(topicPage.getTopicInfoList().size() > 0);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertTrue(e == null);
        }
    }

    /**
     * list test pageIndex = 1 & pageSize = 10
     */
    @Test
    public void testList2() {
        Integer pageIndex = 1;
        Integer pageSize = 10;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage != null);
            Assert.assertTrue(topicPage.getTotal() > 0);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertTrue(e == null);
        }
    }

    /**
     * list test pageIndex = null & pageSize = 10
     */
    @Test
    public void testList3() {
        Integer pageIndex = null;
        Integer pageSize = 10;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_INDEX_INVALID.getCode());
        }
    }

    /**
     * list test pageIndex < 0 & pageSize = 10
     */
    @Test
    public void testList4() {
        Integer pageIndex = -1;
        Integer pageSize = 10;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_INDEX_INVALID.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = -1
     */
    @Test
    public void testList5() {
        Integer pageIndex = 0;
        Integer pageSize = -1;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode());
        }

    }

    /**
     * list test pageIndex = 0 & pageSize = 100
     */
    @Test
    public void testList6() {
        Integer pageIndex = 0;
        Integer pageSize = 100;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage != null);
            Assert.assertTrue(topicPage.getTotal() > 0);
        } catch (BrokerException e) {
            Assert.assertTrue(e == null);
            log.error("method list error:", e);
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 101
     */
    @Test
    public void testList7() {
        Integer pageIndex = 0;
        Integer pageSize = 101;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage == null);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode());
            log.error("method list error:", e);
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 0
     */
    @Test
    public void testList8() {
        Integer pageIndex = 0;
        Integer pageSize = 0;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage == null);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode());
            log.error("method list error:", e);
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = null
     */
    @Test
    public void testList9() {
        Integer pageIndex = 0;
        Integer pageSize = null;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage == null);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode());
            log.error("method list error:", e);
        }
    }
    
    

    /**
     * list test pageIndex is out of all page
     */
    @Test
    public void testList10() {
        Integer pageIndex = 1000;
        Integer pageSize = 50;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage != null);
            Assert.assertTrue(topicPage.getTotal() > 0);
            Assert.assertTrue(topicPage.getTopicInfoList().size() == 0);
        } catch (BrokerException e) {
            Assert.assertNull(e);
        }
    }
    
    /**
     * list test ,groupId is null
     */
    @Test
    public void testList_groupIdIsNull() {
	Integer pageIndex = 0;
        Integer pageSize = 10;
        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, null);
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * list test ,groupId is not number
     */
    @Test
    public void testList_groupIdIsNotNum() {
	Integer pageIndex = 0;
        Integer pageSize = 10;
        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, "sdfsg");
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * list test ,groupId = 0
     */
    @Test
    public void testList_groupIdIsEqual0() {
	Integer pageIndex = 0;
        Integer pageSize = 10;
        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, "0");
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * list test ,groupId < 0
     */
    @Test
    public void testList_groupIdIsLt0() {
	Integer pageIndex = 0;
        Integer pageSize = 10;
        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, "-1");
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * list test ,groupId is not current groupId
     */
    @Test
    public void testList_groupIdIsNotCurrentGroupId() {
	Integer pageIndex = 0;
        Integer pageSize = 10;
        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, "4");
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("producer exist error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }

    /**
     * topic is exits and content is Chinese
     */
    @Test
    public void testPublishEventCharset_topicExists() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "中文消息.".getBytes(), extensions), groupId);
            assertEquals(SendResult.SendResultStatus.SUCCESS, dto.getStatus());
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            Assert.assertNull(e);
        }
    }
    
    /**
     * test extensions is null
     */
    @Test
    public void testPublishEventCharset_extIsNull() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), null), groupId);
            assertNotNull(dto);
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_EXTENSIONS_IS_NUll.getCode());
        }
    }
    
    /**
     * extensions key is null
     */
    @Test
    public void testPublishEventCharset_extKeyIsNull() {
	Map<String, String> ext = new HashMap<>();
        try {
            ext.put(null, "this is a test!");
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId);
            assertNotNull(dto);
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_EXTENSIONS_IS_NUll.getCode());
        }
    }

    /**
     * extensions value is null
     */
    @Test
    public void testPublishEventCharset_extValueIsNull() {
	Map<String, String> ext = new HashMap<>();
        try {
            ext.put("test", null);
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId);
            assertNotNull(dto);
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_EXTENSIONS_IS_NUll.getCode());
        }
    }

    /**
     * extensions contain multiple key,value
     */
    @Test
    public void testPublishEventCharset_extContainMulKeyValue() {
	Map<String, String> ext = new HashMap<>();
        try {
            ext.put("test1", "test value");
            ext.put("test2", "test value2");
            ext.put("test3", "test value3");
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId);
            assertEquals(SendResult.SendResultStatus.SUCCESS, dto.getStatus());
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            Assert.assertNull(e);
        }
    }
    
    /**
     * extensions contain one key,value
     */
    @Test
    public void testPublishEventCharset_extContainOneKeyValue() {
	Map<String, String> ext = new HashMap<>();
        try {
            ext.put("test1", "test value");
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId);
            assertEquals(SendResult.SendResultStatus.SUCCESS, dto.getStatus());
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            Assert.assertNull(e);
        }
    }
    
    /**
     * topic is not exists
     */
    @Test
    public void testPublishEventCharset_topicNotExists() {
        try {
            String topicNotExists = "fsgdsggdgerer";
            SendResult dto = iProducer.publish(new WeEvent(topicNotExists, "中文消息.".getBytes(), extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
            log.error("method PublishEventCharset error:", e);
        }
    }

    /**
     * topic is blank
     */
    @Test
    public void testPublishEventCharset_topicIsBlank() {
        try {
            SendResult dto = iProducer.publish(new WeEvent("", "中文消息.".getBytes(), extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("method PublishEventCharset error:", e);
        }
    }

    /**
     * topic is null
     */
    @Test
    public void testPublishEventCharset_topicIsNull() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(null, "中文消息.".getBytes(), extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("method PublishEventCharset error:", e);
        }
    }


    /**
     * topic length > 64
     */
    @Test
    public void testPublishEventCharset_topicLengthGt64() {
        try {
            String topicNotExists = "fsgdsggdgererqwertyuioplkjhgfdsazxqazwsxedcrfvtgbyhnujmikolppoiuyt";
            SendResult dto = iProducer.publish(new WeEvent(topicNotExists, "中文消息.".getBytes(), extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
            log.error("method PublishEventCharset error:", e);
        }
    }

    /**
     * topic is exits and content is null
     */
    @Test
    public void testPublishEventCharset_contentIsNull() {

        try {
            byte[] bytes = null;
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, bytes, extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_CONTENT_IS_BLANK.getCode());
        }
    }

    /**
     * topic is exits and content is blank
     */
    @Test
    public void testPublishEventCharset_contenIsBlank() {

        try {
            byte[] bytes = "".getBytes();
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, bytes, extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_CONTENT_IS_BLANK.getCode());
        }
    }
    
    /**
     * list test ,groupId is null
     */
    @Test
    public void testPublishEventCharset_groupIdIsNull() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), null);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("producer publish error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * list test ,groupId is not number
     */
    @Test
    public void testPublishEventCharset_groupIdIsNotNum() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "sfsdf");
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("producer publish error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * list test ,groupId = 0
     */
    @Test
    public void testPublishEventCharset_groupIdIsEqual0() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "0");
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("producer publish error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * list test ,groupId < 0
     */
    @Test
    public void testPublishEventCharset_groupIdIsLt0() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "-1");
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("producer publish error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * list test ,groupId is not current groupId
     */
    @Test
    public void testPublishEventCharset_groupIdIsNotCurrentGroupId() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "4");
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("producer publish error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }

    /**
     * topic contain special character without in [32,128]
     */
    @Test
    public void testPublishEventCharset_topicContainSpecialChar() {

        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            byte[] bytes = "helloworld".getBytes();
            SendResult dto = iProducer.publish(new WeEvent(illegalTopic, bytes, extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * topic is Chinese character
     */
    @Test
    public void testPublishEventCharset_topicContainChinChar() {

        try {
            byte[] bytes = "".getBytes();
            SendResult dto = iProducer.publish(new WeEvent("中国", bytes, extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * test get Event : eventId is exists
     */
    @Test
    public void testGetEvent_eventIdExist() {
        try {
            WeEvent weEvent = iProducer.getEvent(eventId, groupId);
            assertEquals(weEvent.getEventId(), eventId);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
            assertNull(e);
        }
    }

    /**
     * test get Event : eventId is illegal1
     */
    @Test
    public void testGetEvent_eventIdIsIllegal1() {
        try {
            WeEvent weEvent = iProducer.getEvent("sfshfwefjf", groupId);
            assertNull(weEvent);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("get event error: ", e);
        }
    }

    /**
     * test get Event : eventId is illegal2
     */
    @Test
    public void testGetEvent_eventIdIsIllegal2() {
        try {
            WeEvent weEvent = iProducer.getEvent("317e7c4c-75-hkhgjhg", groupId);
            assertNull(weEvent);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("get event error: ", e);
        }
    }

    /**
     * test get Event : eventId is legal but not exists & eventId > blockNumber
     */
    @Test
    public void testGetEvent_eventIdHeightGtBlock() {
        try {
            WeEvent weEvent = iProducer.getEvent("317e7c4c-75-32900000", groupId);
            assertNull(weEvent);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_MISMATCH.getCode());
            log.error("get event error: ", e);
        }
    }

    /**
     * test get Event : eventId is legal but not exists
     */
    @Test
    public void testGetEvent_eventIdLegalNotExist() {
        try {
            WeEvent weEvent = iProducer.getEvent("317e7c4c-278-3", groupId);
            assertNull(weEvent);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_NOT_EXIST.getCode());
        }
    }

    /**
     * test get Event : eventId is null
     */
    @Test
    public void testGetEvent_eventIdIsNull() {
        try {
            WeEvent weEvent = iProducer.getEvent(null, groupId);
            assertNull(weEvent);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    /**
     * test get Event : eventId is blank
     */
    @Test
    public void testGetEvent_eventIdIsBlank() {
        try {
            WeEvent weEvent = iProducer.getEvent("", groupId);
            assertNull(weEvent);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    /**
     * test get Event : eventId is blank
     */
    @Test
    public void testGetEvent_eventIdIsBlank2() {
        try {
            WeEvent weEvent = iProducer.getEvent(" ", groupId);
            assertNull(weEvent);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("get event error: ", e);
        }
    }

    /**
     * test get Event : eventId length > 64
     */
    @Test
    public void testGetEvent_eventIdLenGt64() {
        try {
            String id = "317e7c4csdxcfvbhjklpoutredwsaqsdfghjkoiuf-2782345678901234567-329";
            WeEvent weEvent = iProducer.getEvent(id, groupId);
            assertNull(weEvent);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode());
        }
    }
    
    /**
     * get event test,groupId is null
     */
    @Test
    public void testGetEvent_groupIdIsNull() {
        try {
            WeEvent weEvent = iProducer.getEvent(eventId, null);
            Assert.assertNull(weEvent);
        } catch (BrokerException e) {
            log.error("producer getEvent error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * get event test ,groupId is not number
     */
    @Test
    public void testGetEvent_groupIdIsNotNum() {
        try {
            WeEvent weEvent = iProducer.getEvent(eventId, "sfdsfs");
            Assert.assertNull(weEvent);
        } catch (BrokerException e) {
            log.error("producer getEvent error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * get event test ,groupId = 0
     */
    @Test
    public void testGetEvent_groupIdIsEqual0() {
        try {
            WeEvent weEvent = iProducer.getEvent(eventId, "0");
            Assert.assertNull(weEvent);
        } catch (BrokerException e) {
            log.error("producer publish error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * get event test ,groupId < 0
     */
    @Test
    public void testGetEvent_groupIdIsLt0() {
        try {
            WeEvent weEvent = iProducer.getEvent(eventId, "-1");
            Assert.assertNull(weEvent);
        } catch (BrokerException e) {
            log.error("producer getEvent error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * get event test ,groupId is not current groupId
     */
    @Test
    public void testGetEvent_groupIdIsNotCurrentGroupId() {
        try {
            WeEvent weEvent = iProducer.getEvent(eventId, "4");
            Assert.assertNull(weEvent);
        } catch (BrokerException e) {
            log.error("producer getEvent error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }


    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is exists ,content is Chinese
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_topicExist() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(this.topicName, "中文消息.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is not exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_topicNotExist() throws InterruptedException {
        try {
            String notExistsTopic = "sglsjhglsj";
            iProducer.publish(new WeEvent(notExistsTopic, "hello world.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic length > 64
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_topicLenGt64() throws InterruptedException {
        try {
            String notExistsTopic = "qazwsxedcrfvtgbnhyujmkiolpoiuytrsglsjhglsjqwertyuioplkjhgfdsazxcvbnm";
            iProducer.publish(new WeEvent(notExistsTopic, "hello world.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishEventCallback error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_topicIsNull() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(null, "hello world.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishEventCallback error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is blank
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_topicIsBlank() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent("", "hello world.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("method PublishEventCallback error:", e);
        }
        Thread.sleep(3000);
    }

    /**
     * content is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_contentIsNull() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(this.topicName, null, extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_CONTENT_IS_BLANK.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * content is blank
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_contentIsBlank() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(this.topicName, "".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_CONTENT_IS_BLANK.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is exists ,content is English
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_contentIsEnglish() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(this.topicName, "helloWorld".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic contain special character withoutin [32,128]
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_topicContainSpecialChar() throws InterruptedException {
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            iProducer.publish(new WeEvent(illegalTopic, "helloWorld".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is Chinese character
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack_topicContainChiChar() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent("中国", "helloWorld".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
        Thread.sleep(3000);
    }
    
    /**
     * test extensions is null
     */
    @Test
    public void testPublishForEventCallBack_extIsNull() {
        try {
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), null), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_EXTENSIONS_IS_NUll.getCode());
        }
    }
    
    /**
     * extensions key is null
     */
    @Test
    public void testPublishForEventCallBack_extKeyIsNull() {
	Map<String, String> ext = new HashMap<>();
        try {
            ext.put(null, "this is a test!");
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_EXTENSIONS_IS_NUll.getCode());
        }
    }

    /**
     * extensions value is null
     */
    @Test
    public void testPublishForEventCallBack_extValueIsNull() {
	Map<String, String> ext = new HashMap<>();
        try {
            ext.put("test", null);
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_EXTENSIONS_IS_NUll.getCode());
        }
    }

    /**
     * extensions contain multiple key,value
     */
    @Test
    public void testPublishForEventCallBack_extContainMulKeyValue() {
	Map<String, String> ext = new HashMap<>();
        try {
            ext.put("test1", "test value");
            ext.put("test2", "test value2");
            ext.put("test3", "test value3");
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            Assert.assertNull(e);
        }
    }
    
    /**
     * extensions contain one key,value
     */
    @Test
    public void testPublishForEventCallBack_extContainOneKeyValue() {
	Map<String, String> ext = new HashMap<>();
        try {
            ext.put("test1", "test value");
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            Assert.assertNull(e);
        }
    }
    
    /**
     * PublishForEventCallBack test,groupId is null
     */
    @Test
    public void testPublishForEventCallBack_groupIdIsNull() {
        try {
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), null, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * PublishForEventCallBack test,groupId is not number
     */
    @Test
    public void testPublishForEventCallBack_groupIdIsNotNum() {
        try {
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "sfsdf", new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * PublishForEventCallBack test ,groupId = 0
     */
    @Test
    public void testPublishForEventCallBack_groupIdIsEqual0() {
        try {
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "0", new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * PublishForEventCallBack test ,groupId < 0
     */
    @Test
    public void testPublishForEventCallBack_groupIdIsLt0() {
        try {
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "-1", new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }
    
    /**
     * PublishForEventCallBack test ,groupId is not current groupId
     */
    @Test
    public void testPublishForEventCallBack_groupIdIsNotCurrentGroupId() {
        try {
            iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "4", new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
        }
    }

}

