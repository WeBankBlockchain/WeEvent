package com.webank.weevent.ST;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.WeEventClient;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Slf4j
public class JavaSdkTest extends JUnitTestBase {
    private WeEventClient client;
    private String groupId = "1";
    private String eventId;
    private Map<String, String> extensions = new HashMap<>();

    @Before
    public void before() throws Exception {
	String url = "http://localhost:" + listenPort + "/weevent";
	this.client = new WeEventClient(url);
	client.open(topicName, groupId);
	SendResult sendResult = client.publish(topicName, groupId, "Hello World!".getBytes(), extensions);
	this.eventId = sendResult.getEventId();
    }

    /**
     * test topic exist
     */
    @Test
    public void testOpen_topicExist() {
	try {
	    boolean result = client.open(topicName, groupId);
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic not exist
     */
    @Test
    public void testOpen_topicNotExist() {
	try {
	    String topic = this.topicName + System.currentTimeMillis();
	    boolean result = client.open(topic, groupId);
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testOpen_topicOverMaxLen() {
	try {
	    String topic = "topiclengthexceeding64-123456789012345678901234567890123456789012";
	    boolean result = client.open(topic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
	}
    }

    /**
     * test topic length = 64
     */
    @Test
    public void testOpen_topicLenEq64() {
	try {
	    String topic = "topiclengthexceeding64-12345678901234567890123456789012345678901";
	    boolean result = client.open(topic, groupId);
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic is blank ""
     */
    @Test
    public void testOpen_topicIsBlank() {
	try {
	    boolean result = client.open("", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic is " "
     */
    @Test
    public void testOpen_topicIsBlank2() {
	try {
	    boolean result = client.open(" ", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain special char not in [32,128]
     */
    @Test
    public void testOpen_topicContainSpeciaChar() {
	try {
	    char[] charStr = { 69, 72, 31 };
	    String illegalTopic = new String(charStr);
	    boolean result = client.open(illegalTopic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain Chinese char
     */
    @Test
    public void testOpen_topicContainChiChar() {
	try {
	    boolean result = client.open("中国", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic is null
     */
    @Test
    public void testOpen_topicIsNull() {
	try {
	    boolean result = client.open(null, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is null
     */
    @Test
    public void testOpen_groupIdIsNull() {
	try {
	    boolean result = client.open(topicName, null);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is not number
     */
    @Test
    public void testOpen_groupIdIsNotNum() {
	try {
	    boolean result = client.open(topicName, "sfs");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is 0
     */
    @Test
    public void testOpen_groupIdEq0() {
	try {
	    boolean result = client.open(topicName, "0");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId < 0
     */
    @Test
    public void testOpen_groupIdLt0() {
	try {
	    boolean result = client.open(topicName, "-1");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId not Exist
     */
    @Test
    public void testOpen_groupIdNotExist() {
	try {
	    boolean result = client.open(topicName, "4");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test other Exist groupId
     */
    @Test
    public void testOpen_otherGroupIdExist() {
	try {
	    boolean result = client.open(topicName, "2");
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("open topic error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic exist
     */
    @Test
    public void testClose_topicExist() {
	try {
	    boolean result = client.close(topicName, groupId);
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic not exist
     */
    @Test
    public void testClose_topicNotExist() {
	try {
	    String topic = this.topicName + System.currentTimeMillis();
	    boolean result = client.close(topic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_NOT_EXIST.getCode(), e.getCode());
	}
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testClose_topicOverMaxLen() {
	try {
	    String topic = "topiclengthexceeding64-123456789012345678901234567890123456789012";
	    boolean result = client.close(topic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
	}
    }

    /**
     * test topic is blank ""
     */
    @Test
    public void testClose_topicIsBlank() {
	try {
	    boolean result = client.close("", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic is " "
     */
    @Test
    public void testClose_topicIsBlank2() {
	try {
	    boolean result = client.close(" ", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain special char not in [32,128]
     */
    @Test
    public void testClose_topicContainSpeciaChar() {
	try {
	    char[] charStr = { 69, 72, 31 };
	    String illegalTopic = new String(charStr);
	    boolean result = client.close(illegalTopic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain Chinese char
     */
    @Test
    public void testClose_topicContainChiChar() {
	try {
	    boolean result = client.close("中国", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic is null
     */
    @Test
    public void testClose_topicIsNull() {
	try {
	    boolean result = client.close(null, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is null
     */
    @Test
    public void testClose_groupIdIsNull() {
	try {
	    boolean result = client.close(topicName, null);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is not number
     */
    @Test
    public void testClose_groupIdIsNotNum() {
	try {
	    boolean result = client.close(topicName, "sfs");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is 0
     */
    @Test
    public void testClose_groupIdEq0() {
	try {
	    boolean result = client.close(topicName, "0");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId < 0
     */
    @Test
    public void testClose_groupIdLt0() {
	try {
	    boolean result = client.close(topicName, "-1");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId not Exist
     */
    @Test
    public void testClose_groupIdNotExist() {
	try {
	    boolean result = client.close(topicName, "4");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test other Exist groupId
     */
    @Test
    public void testClose_otherGroupIdExist() {
	try {
	    boolean result = client.close(topicName, "2");
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("close topic error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic exist
     */
    @Test
    public void testExist_topicExist() {
	try {
	    boolean result = client.exist(topicName, groupId);
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic not exist
     */
    @Test
    public void testExist_topicNotExist() {
	try {
	    String topic = this.topicName + System.currentTimeMillis();
	    boolean result = client.exist(topic, groupId);
	    assertFalse(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testExist_topicOverMaxLen() {
	try {
	    String topic = "topiclengthexceeding64-123456789012345678901234567890123456789012";
	    boolean result = client.exist(topic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
	}
    }

    /**
     * test topic is blank ""
     */
    @Test
    public void testExist_topicIsBlank() {
	try {
	    boolean result = client.exist("", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic is " "
     */
    @Test
    public void testExist_topicIsBlank2() {
	try {
	    boolean result = client.exist(" ", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain special char not in [32,128]
     */
    @Test
    public void testExist_topicContainSpeciaChar() {
	try {
	    char[] charStr = { 69, 72, 31 };
	    String illegalTopic = new String(charStr);
	    boolean result = client.exist(illegalTopic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain Chinese char
     */
    @Test
    public void testExist_topicContainChiChar() {
	try {
	    boolean result = client.exist("中国", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic is null
     */
    @Test
    public void testExist_topicIsNull() {
	try {
	    boolean result = client.exist(null, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is null
     */
    @Test
    public void testExist_groupIdIsNull() {
	try {
	    boolean result = client.exist(topicName, null);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("existerror: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is not number
     */
    @Test
    public void testExist_groupIdIsNotNum() {
	try {
	    boolean result = client.exist(topicName, "sfs");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is 0
     */
    @Test
    public void testExist_groupIdEq0() {
	try {
	    boolean result = client.exist(topicName, "0");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId < 0
     */
    @Test
    public void testExist_groupIdLt0() {
	try {
	    boolean result = client.exist(topicName, "-1");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId not Exist
     */
    @Test
    public void testExist_groupIdNotExist() {
	try {
	    boolean result = client.exist(topicName, "4");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test other Exist groupId
     */
    @Test
    public void testExist_otherGroupIdExist() {
	try {
	    boolean result = client.exist(topicName, "2");
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic exist
     */
    @Test
    public void testState_topicExist() {
	try {
	    TopicInfo result = client.state(topicName, groupId);
	    Assert.assertTrue(result != null);
	    Assert.assertTrue(!result.getTopicAddress().equals(""));
	    Assert.assertTrue(!result.getSenderAddress().equals(""));
	    Assert.assertTrue(!(result.getCreatedTimestamp() == 0));
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic not exist
     */
    @Test
    public void testState_topicNotExist() {
	try {
	    String topic = this.topicName + System.currentTimeMillis();
	    TopicInfo result = client.state(topic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_NOT_EXIST.getCode(), e.getCode());
	}
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testState_topicOverMaxLen() {
	try {
	    String topic = "topiclengthexceeding64-123456789012345678901234567890123456789012";
	    TopicInfo result = client.state(topic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
	}
    }

    /**
     * test topic is blank ""
     */
    @Test
    public void testState_topicIsBlank() {
	try {
	    TopicInfo result = client.state("", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic is " "
     */
    @Test
    public void testState_topicIsBlank2() {
	try {
	    TopicInfo result = client.state(" ", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain special char not in [32,128]
     */
    @Test
    public void testState_topicContainSpeciaChar() {
	try {
	    char[] charStr = { 69, 72, 31 };
	    String illegalTopic = new String(charStr);
	    TopicInfo result = client.state(illegalTopic, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain Chinese char
     */
    @Test
    public void testState_topicContainChiChar() {
	try {
	    TopicInfo result = client.state("中国", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic is null
     */
    @Test
    public void testState_topicIsNull() {
	try {
	    TopicInfo result = client.state(null, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is null
     */
    @Test
    public void testState_groupIdIsNull() {
	try {
	    TopicInfo result = client.state(topicName, null);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("existerror: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is not number
     */
    @Test
    public void testState_groupIdIsNotNum() {
	try {
	    TopicInfo result = client.state(topicName, "sfs");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is 0
     */
    @Test
    public void testState_groupIdEq0() {
	try {
	    TopicInfo result = client.state(topicName, "0");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId < 0
     */
    @Test
    public void testState_groupIdLt0() {
	try {
	    TopicInfo result = client.state(topicName, "-1");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId not Exist
     */
    @Test
    public void testState_groupIdNotExist() {
	try {
	    TopicInfo result = client.state(topicName, "4");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test other Exist groupId
     */
    @Test
    public void testState_otherGroupIdExist() {
	try {
	    TopicInfo result = client.state(topicName, "2");
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertNull(e);
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
	    TopicPage result = client.list(pageIndex, pageSize, groupId);
	    Assert.assertTrue(result != null);
	    Assert.assertTrue(result.getTopicInfoList().size() > 0);
	} catch (BrokerException e) {
	    assertNull(e);
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
	    TopicPage result = client.list(pageIndex, pageSize, groupId);
	    Assert.assertTrue(result != null);
	    Assert.assertTrue(result.getTopicInfoList().size() > 0);
	} catch (BrokerException e) {
	    assertNull(e);
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
	    TopicPage result = client.list(pageIndex, pageSize, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    Assert.assertNotNull(e);
	    assertEquals(ErrorCode.TOPIC_PAGE_INDEX_INVALID.getCode(), e.getCode());
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
	    TopicPage result = client.list(pageIndex, pageSize, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    Assert.assertNotNull(e);
	    assertEquals(ErrorCode.TOPIC_PAGE_INDEX_INVALID.getCode(), e.getCode());
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
	    TopicPage result = client.list(pageIndex, pageSize, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    Assert.assertNotNull(e);
	    assertEquals(ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode(), e.getCode());
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
	    TopicPage result = client.list(pageIndex, pageSize, groupId);
	    Assert.assertTrue(result != null);
	    Assert.assertTrue(result.getTopicInfoList().size() > 0);
	} catch (BrokerException e) {
	    assertNull(e);
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
	    TopicPage result = client.list(pageIndex, pageSize, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    Assert.assertNotNull(e);
	    assertEquals(ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode(), e.getCode());
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
	    TopicPage result = client.list(pageIndex, pageSize, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    Assert.assertNotNull(e);
	    assertEquals(ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode(), e.getCode());
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
	    TopicPage result = client.list(pageIndex, pageSize, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    Assert.assertNotNull(e);
	    assertEquals(ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is null
     */
    @Test
    public void testList_groupIdIsNull() {
	try {
	    TopicPage result = client.list(0, 10, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("existerror: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is not number
     */
    @Test
    public void testList_groupIdIsNotNum() {
	try {
	    TopicPage result = client.list(0, 10, "sfs");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is 0
     */
    @Test
    public void testList_groupIdEq0() {
	try {
	    TopicPage result = client.list(0, 10, "0");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId < 0
     */
    @Test
    public void testList_groupIdLt0() {
	try {
	    TopicPage result = client.list(0, 10, "-1");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId not Exist
     */
    @Test
    public void testList_groupIdNotExist() {
	try {
	    TopicPage result = client.list(0, 10, "4");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test other Exist groupId
     */
    @Test
    public void testList_otherGroupIdExist() {
	try {
	    TopicPage result = client.list(0, 10, "2");
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic exist
     */
    @Test
    public void testPublish_topicExist() {
	try {
	    SendResult result = client.publish(topicName, groupId, "hello".getBytes(), extensions);
	    assertEquals(SendResult.SendResultStatus.SUCCESS, result.getStatus());
	    assertNotNull(result.getEventId());
	    assertEquals(result.getTopic(), topicName);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic not exist
     */
    @Test
    public void testPublish_topicNotExist() {
	try {
	    String topic = this.topicName + System.currentTimeMillis();
	    SendResult result = client.publish(topic, groupId, "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_NOT_EXIST.getCode(), e.getCode());
	}
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testPublish_topicOverMaxLen() {
	try {
	    String topic = "topiclengthexceeding64-123456789012345678901234567890123456789012";
	    SendResult result = client.publish(topic, groupId, "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
	}
    }

    /**
     * test topic is blank ""
     */
    @Test
    public void testPublish_topicIsBlank() {
	try {
	    SendResult result = client.publish("", groupId, "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic is " "
     */
    @Test
    public void testPublish_topicIsBlank2() {
	try {
	    SendResult result = client.publish(" ", groupId, "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain special char not in [32,128]
     */
    @Test
    public void testPublish_topicContainSpeciaChar() {
	try {
	    char[] charStr = { 69, 72, 31 };
	    String illegalTopic = new String(charStr);
	    SendResult result = client.publish(illegalTopic, groupId, "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain Chinese char
     */
    @Test
    public void testPublish_topicContainChiChar() {
	try {
	    SendResult result = client.publish("中国", groupId, "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic is null
     */
    @Test
    public void testPublish_topicIsNull() {
	try {
	    SendResult result = client.publish(null, groupId, "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is null
     */
    @Test
    public void testPublish_groupIdIsNull() {
	try {
	    SendResult result = client.publish(this.topicName, null, "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("existerror: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is not number
     */
    @Test
    public void testPublish_groupIdIsNotNum() {
	try {
	    SendResult result = client.publish(this.topicName, "sfs", "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is 0
     */
    @Test
    public void testPublish_groupIdEq0() {
	try {
	    SendResult result = client.publish(this.topicName, "0", "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId < 0
     */
    @Test
    public void testPublish_groupIdLt0() {
	try {
	    SendResult result = client.publish(this.topicName, "-1", "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId not Exist
     */
    @Test
    public void testPublish_groupIdNotExist() {
	try {
	    SendResult result = client.publish(this.topicName, "4", "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test other Exist groupId
     */
    @Test
    public void testPublish_otherGroupIdExist() {
	try {
	    SendResult result = client.publish(this.topicName, "2", "hello".getBytes(), extensions);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test content is null
     */
    @Test
    public void testPublish_contentIsNull() {
	try {
	    SendResult result = client.publish(this.topicName, groupId, null, extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_CONTENT_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test content is ""
     */
    @Test
    public void testPublish_contentIsBlank() {
	try {
	    SendResult result = client.publish(this.topicName, groupId, "".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_CONTENT_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test content is " "
     */
    @Test
    public void testPublish_contentIsBlank2() {
	try {
	    SendResult result = client.publish(this.topicName, groupId, " ".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_CONTENT_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test extensions is null
     */
    @Test
    public void testPublish_extIsNull() {
	try {
	    SendResult result = client.publish(this.topicName, groupId, "hello".getBytes(), extensions);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_EXTENSIONS_IS_NUll.getCode(), e.getCode());
	}
    }

    /**
     * test extensions key is null
     */
    @Test
    public void testPublish_extKeyIsNull() {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put(null, "this is a test!");
	    SendResult result = client.publish(this.topicName, groupId, "hello".getBytes(), ext);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_EXTENSIONS_IS_NUll.getCode(), e.getCode());
	}
    }

    /**
     * test extensions key is null
     */
    @Test
    public void testPublish_extValueIsNull() {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put("test", null);
	    SendResult result = client.publish(this.topicName, groupId, "hello".getBytes(), ext);
	    assertNotNull(result.getEventId());
	    assertEquals(result.getTopic(), this.topicName);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test extensions contain one key value
     */
    @Test
    public void testPublish_extContainOne() {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put("test", "test1");
	    SendResult result = client.publish(this.topicName, groupId, "hello".getBytes(), ext);
	    assertNotNull(result.getEventId());
	    assertEquals(result.getTopic(), this.topicName);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test extensions contain multiple key value
     */
    @Test
    public void testPublish_extContainMultivalue() {
	Map<String, String> ext = new HashMap<>();
	try {
	    ext.put("test", "test1");
	    ext.put("test2", "test2");
	    ext.put("test3", "test3");
	    SendResult result = client.publish(this.topicName, groupId, "hello".getBytes(), ext);
	    assertNotNull(result.getEventId());
	    assertEquals(result.getTopic(), this.topicName);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test eventId exist
     */
    @Test
    public void testEvent_eventIdExist() {
	try {
	    WeEvent event = client.getEvent(this.eventId, groupId);
	    assertEquals(event.getEventId(), this.eventId);
	    assertEquals(new String(event.getContent()), "Hello World!");
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test eventId is illegal "sdfs"
     */
    @Test
    public void testEvent_eventIdIlllegal1() {
	try {
	    WeEvent result = client.getEvent("sdfs", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	}
    }

    /**
     * test eventId is illegal "317e7c4c-75-dsff"
     */
    @Test
    public void testEvent_eventIdIlllegal2() {
	try {
	    WeEvent result = client.getEvent("317e7c4c-75-dsff", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	}
    }

    /**
     * test eventId is illegal "317e7c4c-75-329000"
     */
    @Test
    public void testEvent_eventIdIlllegal3() {
	try {
	    WeEvent result = client.getEvent("317e7c4c-75-329000", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_MISMATCH.getCode(), e.getCode());
	}
    }

    /**
     * test eventId is legal but not exist
     */
    @Test
    public void testEvent_eventIdNotExist() {
	try {
	    WeEvent result = client.getEvent("317e7c4c-75-3", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_NOT_EXIST.getCode(), e.getCode());
	}
    }

    /**
     * test eventId is null
     */
    @Test
    public void testEvent_eventIdIsNull() {
	try {
	    WeEvent result = client.getEvent(null, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	}
    }

    /**
     * test eventId is ""
     */
    @Test
    public void testEvent_eventIdIsBlank() {
	try {
	    WeEvent result = client.getEvent("", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	}
    }

    /**
     * test eventId is " "
     */
    @Test
    public void testEvent_eventIdIsBlank2() {
	try {
	    WeEvent result = client.getEvent(" ", groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	}
    }

    /**
     * test eventId length > 64
     */
    @Test
    public void testEvent_eventIdOverMaxLen() {
	try {
	    String id = "317e7c4csdxcfvbhjklpoutredwsaqsdfghjkoiuf-2782345678901234567-329";
	    WeEvent result = client.getEvent(id, groupId);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is null
     */
    @Test
    public void testEvent_groupIdIsNull() {
	try {
	    WeEvent result = client.getEvent(this.eventId, null);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("existerror: ", e);
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is not number
     */
    @Test
    public void testEvent_groupIdIsNotNum() {
	try {
	    WeEvent result = client.getEvent(this.eventId, "sfs");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * test groupId is 0
     */
    @Test
    public void testEvent_groupIdEq0() {
	try {
	    WeEvent result = client.getEvent(this.eventId, "0");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId < 0
     */
    @Test
    public void testEvent_groupIdLt0() {
	try {
	    WeEvent result = client.getEvent(this.eventId, "-1");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test groupId not Exist
     */
    @Test
    public void testEvent_groupIdNotExist() {
	try {
	    WeEvent result = client.getEvent(this.eventId, "4");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	}
    }

    /**
     * test other Exist groupId
     */
    @Test
    public void testEvent_otherGroupIdExist() {
	try {
	    SendResult publish = client.publish(topicName, "2", "Hello World!".getBytes(), extensions);
	    WeEvent result = client.getEvent(publish.getEventId(), "2");
	    assertEquals(result.getEventId(), publish.getEventId());
	    assertEquals(new String(result.getContent()), "Hello World!");
	} catch (BrokerException e) {
	    log.error("exist error: ", e);
	    assertNull(e);
	}
    }

    /**
     * test topic is null
     */
    @Test
    public void testSubscribe_topicIsNull() {
	try {
	    String result = client.subscribe(null, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic is blank ""
     */
    @Test
    public void testSubscribe_topicIsBlank() {
	try {
	    String result = client.subscribe("", WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic is blank " "
     */
    @Test
    public void testSubscribe_topicIsBlank2() {
	try {
	    String result = client.subscribe(" ", WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testSubscribe_topicOverMaxLen() {
	try {
	    String topic = "qwertyuioplkjhgfdsazxcvbnmlkjhgfjshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff";
	    String result = client.subscribe(topic, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain special char not in [32,128]
     */
    @Test
    public void testSubscribe_topicContainSpecialChar() {
	try {
	    char[] charStr = { 69, 72, 31 };
	    String illegalTopic = new String(charStr);
	    String result = client.subscribe(illegalTopic, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic contain special char not in [32,128]
     */
    @Test
    public void testSubscribe_topicContainChiChar() {
	try {
	    String result = client.subscribe("测试", WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * test topic offset = WeEvent.OFFSET_FIRST
     */
    @Test
    public void testSubscribe_offsetIsFirst() {
	try {
	    String result = client.subscribe(this.topicName, WeEvent.OFFSET_FIRST, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNotNull(result);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test topic offset = WeEvent.OFFSET_LAST
     */
    @Test
    public void testSubscribe_offsetIsLast() {
	try {
	    String result = client.subscribe(this.topicName, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNotNull(result);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test topic offset is eventId
     */
    @Test
    public void testSubscribe_offsetIsEventId() {
	try {
	    String result = client.subscribe(this.topicName, this.eventId, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNotNull(result);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test topic offset is illegal
     */
    @Test
    public void testSubscribe_offsetIsIllegal() {
	try {
	    String result = client.subscribe(this.topicName, "sfs", new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    System.out.println(result);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	}
    }

    /**
     * test topic offset is illegal2
     */
    @Test
    public void testSubscribe_offsetIsIllegal2() {
	try {
	    String result = client.subscribe(this.topicName, "317e7c4c-dsflf-sdfls", new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	}
    }

    /**
     * test topic offset is illegal3
     */
    @Test
    public void testSubscribe_offsetIsIllegal3() {
	try {
	    String result = client.subscribe(this.topicName, "317e7c5c-75-sdfls", new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	}
    }

    /**
     * test topic offset is illegal4
     */
    @Test
    public void testSubscribe_offsetIsIllegal4() {
	try {
	    String result = client.subscribe(this.topicName, "317e7c5c-lsfl-3", new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	}
    }

    /**
     * test topic offset contain height > blockchain height
     */
    @Test
    public void testSubscribe_offsetNumGtBlock() {
	try {
	    String result = client.subscribe(this.topicName, "317e7c5c-75-329000", new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_MISMATCH.getCode(), e.getCode());
	}
    }

    /**
     * test topic offset length > 64
     */
    @Test
    public void testSubscribe_offsetOverMaxLen() {

	try {
	    String offset = "317e7c4c45gfjfs5369875452364875962-1213456789632145678564547896354775-329";
	    String result = client.subscribe(this.topicName, offset, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode(), e.getCode());
	}
    }

    /**
     * test topic offset is null
     */
    @Test
    public void testSubscribe_offsetIsNull() {

	try {
	    String result = client.subscribe(this.topicName, null, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic offset is blank ""
     */
    @Test
    public void testSubscribe_offsetIsBlank() {

	try {
	    String result = client.subscribe(this.topicName, "", new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test topic offset is null
     */
    @Test
    public void testSubscribe_offsetIsBlank2() {

	try {
	    String result = client.subscribe(this.topicName, " ", new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test listener is null
     */
    @Test
    public void testSubscribe_listennerIsNull() {

	try {
	    String result = client.subscribe(this.topicName, " ", null);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * test subId exist
     */
    @Test
    public void testUnSubscribe_subIdExist() {

	try {
	    String subId = client.subscribe(this.topicName, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {

		@Override
		public void onException(Throwable e) {

		}

		@Override
		public void onEvent(WeEvent event) {

		}
	    });
	    Boolean result = client.unSubscribe(subId);
	    assertTrue(result);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test subId not exist
     */
    @Test
    public void testUnSubscribe_subIdNotExist() {

	try {
	    Boolean result = client.unSubscribe("sfsghhr");
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.SUBSCRIPTIONID_NOT_EXIST.getCode(), e.getCode());
	}
    }

    /**
     * test subId is null
     */
    @Test
    public void testUnSubscribe_subIdIsNull() {

	try {
	    Boolean result = client.unSubscribe(null);
	    assertFalse(result);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test subId is blank ""
     */
    @Test
    public void testUnSubscribe_subIdIsBlank() {

	try {
	    Boolean result = client.unSubscribe("");
	    assertFalse(result);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

    /**
     * test subId is " "
     */
    @Test
    public void testUnSubscribe_subIdIsBlank2() {

	try {
	    Boolean result = client.unSubscribe(" ");
	    assertFalse(result);
	} catch (BrokerException e) {
	    assertNull(e);
	}
    }

}
