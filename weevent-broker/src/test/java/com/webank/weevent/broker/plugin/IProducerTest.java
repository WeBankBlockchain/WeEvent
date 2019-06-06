package com.webank.weevent.broker.plugin;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * producer test
 * 
 * @version 1.0
 * @since 11/08/2018
 */
@Slf4j
public class IProducerTest extends JUnitTestBase {

    private IProducer iProducer;
    private static String eventId = "";
    private String groupId = "1";
    private static final long wait3s = 100;
    private Map<String, String> extensions = new HashMap<>();

    @Before
    public void before() {
	try {
	    iProducer = IProducer.build();
	    extensions.put("weevent-url", "https://github.com/WeBankFinTech/WeEvent");
	    assertTrue(iProducer != null);
	    assertTrue(iProducer.open(this.topicName, groupId));
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
     * start producer test
     */
    @Test
    public void testStartProducer() {
	try {
	    assertTrue(iProducer.startProducer());
	} catch (BrokerException e) {
	    log.error("start producer error:", e);
	}
    }

    /**
     * shutdown producer
     */
    @Test
    public void testShutdownProducer() {
	Assert.assertTrue(iProducer.shutdownProducer());
    }

    /**
     * test shutdownProducer 3 times
     */
    @Test
    public void testShutdownProducer_multiple() {
	Assert.assertTrue(iProducer.shutdownProducer());
	Assert.assertTrue(iProducer.shutdownProducer());
	Assert.assertTrue(iProducer.shutdownProducer());
    }

    /**
     * open topic exist
     */
    @Test
    public void testOpen_topicExist() {
	try {
	    boolean result = iProducer.open(this.topicName, groupId);
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("producer open error::", e);
	    assertNull(e);
	}
    }

    /**
     * topic not exist
     */
    @Test
    public void testOpen_topicNotExist() {
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
     * topic length > 64
     */
    @Test
    public void testOpen_topicOverMaxLen() {
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
     * topic length = 64
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
     * topic is null
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
     * topic is blank " "
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
     * topic contain special char
     */
    @Test
    public void testOpen_topicContainSpeciaChar() {
	char[] charStr = { 69, 72, 31 };
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
     * topic contain Chinese char
     */
    @Test
    public void testOpen_topicContainChiChar() {
	try {
	    boolean result = iProducer.open("中国", groupId);
	    assert (result);
	} catch (BrokerException e) {
	    log.error("producer open error:", e);
	    assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
	}
    }

    /**
     * groupId is null
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
     * groupId is not a number
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
     * groupId is not exist
     */
    @Test
    public void testOpen_groupIdIsExist() {
	try {
	    boolean result = iProducer.open(this.topicName, "4");
	    assertFalse(result);
	} catch (BrokerException e) {
	    log.error("producer open error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
	}
    }

    /**
     * topic exist
     */
    @Test
    public void testClose_topicExist() {
	try {
	    Assert.assertTrue(iProducer.close(this.topicName, groupId));
	} catch (BrokerException e) {
	    log.error("producer close error:", e);
	    assertNull(e);
	}
    }

    /**
     * topic is null
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
     * topic is blank " "
     */
    @Test
    public void testClose_topicIsBlank() {
	try {
	    Assert.assertFalse(iProducer.close(" ", groupId));
	} catch (BrokerException e) {
	    log.error("producer close error:", e);
	    assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
	}
    }

    /**
     * topic length > 64
     */
    @Test
    public void testClose_topicOverMaxLen() {
	try {
	    String topicStr = "topiclengthlonger64azxsqwedcvfrtgbnhyujmkiolpoiuytr-" + System.currentTimeMillis();
	    Assert.assertFalse(iProducer.close(topicStr, groupId));
	} catch (BrokerException e) {
	    log.error("producer close error:", e);
	    assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
	}
    }

    /**
     * topic contain special char
     */
    @Test
    public void testClose_topicContainSpecialChar() {
	char[] charStr = { 69, 72, 31 };
	try {
	    String illegalTopic = new String(charStr);
	    Assert.assertFalse(iProducer.close(illegalTopic, groupId));
	} catch (BrokerException e) {
	    log.error("producer close error:", e);
	    assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
	}
    }

    /**
     * topic contain Chinese char
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
     * groupId is null
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
     * groupId is not a number
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
     * groupId not exist
     */
    @Test
    public void testClose_groupIdNotExist() {
	try {
	    boolean result = iProducer.close(this.topicName, "4");
	    assertFalse(result);
	} catch (BrokerException e) {
	    log.error("producer close error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
	}
    }

    /**
     * topic exist
     */
    @Test
    public void testExist_topicExist() {
	try {
	    boolean result = iProducer.exist(this.topicName, groupId);
	    Assert.assertTrue(result);
	} catch (BrokerException e) {
	    log.error("method Exist error:", e);
	    Assert.assertNull(e);
	}
    }

    /**
     * topic not exist
     */
    @Test
    public void testExist_topicNotExist() {
	try {
	    String falseTopic = "sdlkufhdsighfskhdsf";
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
    public void testExist_topicOverMaxlen() {
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
     * topic is blank " "
     */
    @Test
    public void testExist_topicIsBlank() {
	try {
	    String falseTopic = " ";
	    Assert.assertFalse(iProducer.exist(falseTopic, groupId));
	} catch (BrokerException e) {
	    log.error("method Exist error:", e);
	    Assert.assertNotNull(e);
	    assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
	}
    }

    /**
     * topic contain special char
     */
    @Test
    public void testExist_topicContainSpecialChar() {
	char[] charStr = { 69, 72, 31 };
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
     * topic contain Chinese char
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
     * groupId is null
     */
    @Test
    public void testExist_groupIdIsNull() {
	try {
	    boolean result = iProducer.exist(this.topicName, null);
	    assertFalse(result);
	} catch (BrokerException e) {
	    log.error("producer exist error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
	}
    }

    /**
     * groupId is not a number
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
     * groupId not exist
     */
    @Test
    public void testExist_groupIdNotExist() {
	try {
	    boolean result = iProducer.exist(this.topicName, "4");
	    assertFalse(result);
	} catch (BrokerException e) {
	    log.error("producer exist error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
	}
    }

    /**
     * topic exist
     */
    @Test
    public void testState_topicExist() {
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
     * topic length > 64s
     */
    @Test
    public void testState_topicOverMaxLen() {
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
     * topic not exist
     */
    @Test
    public void testState_topicNotExist() {
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
     * topic is blank
     */
    @Test
    public void testState_topicIsBlank() {
	try {
	    iProducer.state(" ", groupId);
	} catch (BrokerException e) {
	    log.error("method state error:", e);
	    Assert.assertNotNull(e);
	    assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
	}
    }

    /**
     * topic contain special char
     */
    @Test
    public void testState_topicContainSpeciaChar() {
	char[] charStr = { 69, 72, 31 };
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
     * groupId is null
     */
    @Test
    public void testState_groupIdIsNull() {
	try {
	    iProducer.state(this.topicName, groupId);
	} catch (BrokerException e) {
	    log.error("producer close error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
	}
    }

    /**
     * groupId is not a number
     */
    @Test
    public void testState_groupIdIsNotNum() {
	try {
	    iProducer.state(this.topicName, groupId);
	} catch (BrokerException e) {
	    log.error("producer close error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
	}
    }

    /**
     * groupId not exist
     */
    @Test
    public void testState_groupIdNotExist() {
	try {
	    iProducer.state(this.topicName, groupId);
	} catch (BrokerException e) {
	    log.error("producer close error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
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
     * groupId not Exist
     */
    @Test
    public void testList_groupIdNotExist() {
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
     * test get Event : eventId is exist
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
    public void testGetEvent_eventIdIsIllegal() {
	try {
	    WeEvent weEvent = iProducer.getEvent("sfshfwefjf", groupId);
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
	    WeEvent weEvent = iProducer.getEvent(" ", groupId);
	    assertNull(weEvent);
	} catch (BrokerException e) {
	    log.error("get event error: ", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
	}
    }

    /**
     * test get Event : eventId length > 64
     */
    @Test
    public void testGetEvent_eventIdOverMaxLen() {
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
     * get event test ,groupId is not Exist
     */
    @Test
    public void testGetEvent_groupIdIsNotExist() {
	try {
	    WeEvent weEvent = iProducer.getEvent(eventId, "4");
	    Assert.assertNull(weEvent);
	} catch (BrokerException e) {
	    log.error("producer getEvent error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
	}
    }

    /**
     * topic is exist and content is Chinese
     */
    @Test
    public void testPublish_topicExists() {
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
    public void testPublish_extIsNull() {
	try {
	    SendResult dto = iProducer
		    .publish(new WeEvent(this.topicName, "this is only test message".getBytes(), null), groupId);
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
    public void testPublish_extKeyIsNull() {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put(null, "this is a test!");
	    SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext),
		    groupId);
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
    public void testPublish_extValueIsNull() {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put("test", null);
	    SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext),
		    groupId);
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
    public void testPublish_extContainMulKeyValue() {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put("test1", "test value");
	    ext.put("test2", "test value2");
	    ext.put("test3", "test value3");
	    SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext),
		    groupId);
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
    public void testPublish_extContainOneKeyValue() {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put("test1", "test value");
	    SendResult dto = iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext),
		    groupId);
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
    public void testPublish_topicNotExist() {
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
    public void testPublish_topicIsBlank() {
	try {
	    SendResult dto = iProducer.publish(new WeEvent(" ", "中文消息.".getBytes(), extensions), groupId);
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
    public void testPublish_topicIsNull() {
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
    public void testPublish_topicOverMaxLen() {
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
    public void testPublish_contentIsNull() {

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
    public void testPublish_contenIsBlank() {

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
     * groupId is null
     */
    @Test
    public void testPublish_groupIdIsNull() {
	try {
	    SendResult dto = iProducer
		    .publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), null);
	    Assert.assertNull(dto);
	} catch (BrokerException e) {
	    log.error("producer publish error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
	}
    }

    /**
     * groupId is not exist
     */
    @Test
    public void testPublish_groupIdIsNotExist() {
	try {
	    SendResult dto = iProducer
		    .publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "4");
	    Assert.assertNull(dto);
	} catch (BrokerException e) {
	    log.error("producer publish error:", e);
	    assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
	}
    }

    /**
     * groupId is not number
     */
    @Test
    public void testPublish_groupIdIsNotNum() {
	try {
	    SendResult dto = iProducer
		    .publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "sfsdf");
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
    public void testPublish_topicContainSpecialChar() {

	char[] charStr = { 69, 72, 31 };
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
    public void testPublish_topicContainChinChar() {

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
     * topic is exist ,content is Chinese
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_topicExist() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent(this.topicName, "中文消息.".getBytes(), extensions), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * topic is not exist
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_topicNotExist() throws InterruptedException {
	try {
	    String notExistsTopic = "sglsjhglsj";
	    iProducer.publish(new WeEvent(notExistsTopic, "hello world.".getBytes(), extensions), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * topic length > 64
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_topicOverMaxLen() throws InterruptedException {
	try {
	    String notExistsTopic = "qazwsxedcrfvtgbnhyujmkiolpoiuytrsglsjhglsjqwertyuioplkjhgfdsazxcvbnm";
	    iProducer.publish(new WeEvent(notExistsTopic, "hello world.".getBytes(), extensions), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * topic is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_topicIsNull() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent(null, "hello world.".getBytes(), extensions), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * topic is blank
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_topicIsBlank() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent(" ", "hello world.".getBytes(), extensions), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * content is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_contentIsNull() throws InterruptedException {
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

	Thread.sleep(wait3s);
    }

    /**
     * content is blank
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_contentIsBlank() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent(this.topicName, " ".getBytes(), extensions), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * topic is exist ,content is English
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_contentIsEnglish() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent(this.topicName, "helloWorld".getBytes(), extensions), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * topic contain special character withoutin [32,128]
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_topicContainSpecialChar() throws InterruptedException {
	char[] charStr = { 69, 72, 31 };
	try {
	    String illegalTopic = new String(charStr);
	    iProducer.publish(new WeEvent(illegalTopic, "helloWorld".getBytes(), extensions), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * topic is Chinese character
     *
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_topicContainChiChar() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent("中国", "helloWorld".getBytes(), extensions), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * test extensions is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_extIsNull() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), null), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * extensions key is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_extKeyIsNull() throws InterruptedException {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put(null, "this is a test!");
	    iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * extensions value is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_extValueIsNull() throws InterruptedException {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put("test", null);
	    iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * extensions contain multiple key,value
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_extContainMulKeyValue() throws InterruptedException {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put("test1", "test value");
	    ext.put("test2", "test value2");
	    ext.put("test3", "test value3");
	    iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * extensions contain one key,value
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_extContainOneKeyValue() throws InterruptedException {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put("test1", "test value");
	    iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), ext), groupId,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * PublishForEventCallBack test,groupId is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_groupIdIsNull() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), null,
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * PublishForEventCallBack test,groupId is not number
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_groupIdIsNotNum() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "sfsdf",
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }

    /**
     * groupId not exist
     * 
     * @throws InterruptedException
     */
    @Test
    public void testCallbackPublish_groupIdNotExist() throws InterruptedException {
	try {
	    iProducer.publish(new WeEvent(this.topicName, "this is only test message".getBytes(), extensions), "0",
		    new IProducer.SendCallBack() {
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

	Thread.sleep(wait3s);
    }
}
