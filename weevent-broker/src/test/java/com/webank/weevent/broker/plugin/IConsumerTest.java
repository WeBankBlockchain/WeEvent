package com.webank.weevent.broker.plugin;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Slf4j
public class IConsumerTest extends JUnitTestBase {

    private final String topic2 = this.topicName + "2";
    private final String topic3 = this.topicName + "3";
    private static final long wait3s = 100;

    private IProducer iProducer;
    private IConsumer iConsumer;
    private String eventId = "";
    private String groupId = "1";
    private Map<String, String> extensions = new HashMap<>();

    @Before
    public void before() throws Exception {
	this.iProducer = IProducer.build();
	this.iConsumer = IConsumer.build();
	extensions.put("weevent-url", "https://github.com/WeBankFinTech/WeEvent");
	assertTrue(this.iProducer.open(this.topicName, this.groupId));
	assertTrue(this.iProducer.open(this.topic2, this.groupId));
	assertTrue(this.iProducer.open(this.topic3, this.groupId));
	assertTrue(this.iProducer.startProducer());

	String data = String.format("hello world! %s", System.currentTimeMillis());
	WeEvent weEvent = new WeEvent(this.topicName, data.getBytes(), extensions);
	SendResult sendResultDto = this.iProducer.publish(weEvent, groupId);

	assertEquals(SendResult.SendResultStatus.SUCCESS, sendResultDto.getStatus());
	this.eventId = sendResultDto.getEventId();
	log.info("publish eventId: {}", this.eventId);

	assertTrue(this.eventId.length() > 1);

	// charset with utf-8
	data = String.format("中文消息! %s", System.currentTimeMillis());
	weEvent = new WeEvent(this.topicName, data.getBytes(), extensions);
	sendResultDto = this.iProducer.publish(weEvent, groupId);

	assertEquals(SendResult.SendResultStatus.SUCCESS, sendResultDto.getStatus());
	this.eventId = sendResultDto.getEventId();
	log.info("publish eventId charset: {}", this.eventId);

	assertTrue(this.eventId.length() > 1);
    }

    @After
    public void after() throws Exception {
	assertTrue(this.iProducer.shutdownProducer());
	assertTrue(this.iConsumer.shutdownConsumer());
	this.iProducer = null;
	this.iConsumer = null;
    }

    /**
     * test topic is ""
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_topicIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe("", groupId, WeEvent.OFFSET_FIRST, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * test topic is " "
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_topicIsBlank2() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe("", groupId, WeEvent.OFFSET_FIRST, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * test topic length > 64
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_topicOverMaxLen() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(
		    "qwertyuioplkjhgfdsazxcvbnmlkjhgfjshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff", groupId,
		    WeEvent.OFFSET_FIRST, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * topic contain special char
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_containSpeciaChar() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	char[] charStr = { 69, 72, 31 };
	try {
	    String illegalTopic = new String(charStr);
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(illegalTopic, groupId, this.eventId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * topic contain Chinese char
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_containChinChar() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe("中国", groupId, this.eventId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * groupId is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_groupIdIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, null, this.eventId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * groupId is not a number
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_groupIdIsNotNum() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, "sdfsf", this.eventId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * groupId not exist
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_groupIdNotExist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, "4", this.eventId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset is WeEvent.OFFSET_FRIST
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsFrist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_FIRST, "sdk",
		    new ConsumerListener());
	    log.info("result id: " + result);
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertNull(e);
	}
	Thread.sleep(wait3s);
    }

    /**
     * offset is WeEvent.OFFSET_LAST
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsLast() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertNull(e);
	}
	Thread.sleep(wait3s);
    }

    /**
     * offset is normal eventId
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_normalEventId() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, this.eventId, "sdk",
		    new ConsumerListener());
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertNull(e);
	}
	Thread.sleep(wait3s);
    }

    /**
     * offset is illegal
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsIllegal() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, "lsjflsjfljls", "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset eventId contain height large than block height
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetNumGtBlock() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-75-32900000", "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_MISMATCH.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset length > 64
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetOverMaxLen() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId,
		    "317e7c4c45gfjfs5369875452364875962-1213456789632145678564547896354775-329", "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, null, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset is blank " "
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, " ", "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * listener is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_listenerIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, this.eventId, "sdk", null);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * topic not same first subscribe
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicNotSameFirst() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());

	    String result = this.iConsumer.subscribe(topic2, groupId, WeEvent.OFFSET_LAST, subId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_NOT_MATCH.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * topic is blank " "
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(" ", groupId, WeEvent.OFFSET_FIRST, subId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * topic length > 64
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicOverMaxLen() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(
		    "qwertyuioplkjhgfdsazxcvbnmlkjhgfjshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff", groupId,
		    WeEvent.OFFSET_FIRST, subId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * groupId is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, null, this.eventId, subId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * groupId is not a number
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdIsNotNum() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, "sdfsf", this.eventId, subId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * group id not exist
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdNotExist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, "4", this.eventId, subId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset is WeEvent.OFFSET_FRIST
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsFrist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_FIRST, subId, "sdk",
		    new IConsumer.ConsumerListener() {
			@Override
			public void onEvent(String subscriptionId, WeEvent event) {
			    assertNotNull(event);
			    assertEquals(topicName, event.getTopic());
			    assertNotNull(event.getEventId());
			}

			@Override
			public void onException(Throwable e) {
			    log.error("onException", e);
			    fail();
			}
		    });
	    log.info("result id: " + result);
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertNull(e);
	}

	Thread.sleep(wait3s);
    }

    /**
     * offset is WeEvent.OFFSET_LAST
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsLast() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, subId, "sdk",
		    new IConsumer.ConsumerListener() {
			@Override
			public void onEvent(String subscriptionId, WeEvent event) {
			    log.info("onEvent {}", event);
			    assertNotNull(event);
			    assertEquals(topicName, event.getTopic());
			    assertNotNull(event.getEventId());
			}

			@Override
			public void onException(Throwable e) {
			    log.error("onException", e);
			    fail();
			}
		    });
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertNull(e);
	}

	Thread.sleep(wait3s);
    }

    /**
     * offset is normal eventId
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_normalEventId() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, groupId, this.eventId, subId, "sdk",
		    new IConsumer.ConsumerListener() {
			@Override
			public void onEvent(String subscriptionId, WeEvent event) {
			    log.info("onEvent {}", event);
			    assertNotNull(event);
			    assertEquals(topicName, event.getTopic());
			    assertNotNull(event.getEventId());
			}

			@Override
			public void onException(Throwable e) {
			    log.error("onException", e);
			    fail();
			}
		    });
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertNull(e);
	}
	Thread.sleep(wait3s);
    }

    /**
     * offset is illegal
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsIllegal() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, groupId, "lsjflsjfljls", subId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset eventId contain height large than block height
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetNumGtBlock() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-75-32900000", subId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_MISMATCH.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * topic length > 64
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetOverMaxLen() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, groupId,
		    "317e7c4c45gfjfs5369875452364875962-1213456789632145678564547896354775-329", subId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, groupId, null, subId, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset is blank " "
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    String result = this.iConsumer.subscribe(this.topicName, groupId, "", subId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * subId is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_subIdIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, null, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * subId is blank
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_subIdIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "", "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * subId is illegal
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_subIdIsIllegal() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdgsgsgdg", "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.SUBSCRIPTIONID_FORMAT_INVALID.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * subId legal but not exist
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_subIdIsNotExist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST,
		    "ec1776da-1748-4c68-b0eb-ed3e92f9aadb", "sdk", new IConsumer.ConsumerListener() {
			@Override
			public void onEvent(String subscriptionId, WeEvent event) {
			    log.info("onEvent {}", event);
			    assertNotNull(event);
			    assertEquals(topicName, event.getTopic());
			    assertNotNull(event.getEventId());
			}

			@Override
			public void onException(Throwable e) {
			    log.error("onException", e);
			    fail();
			}
		    });
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertNull(e);
	}
    }

    /**
     * topics list topic is " "
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_topicIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { "" };
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_IS_BLANK.getCode(), e.getCode());
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener) method error: ", e);
	}
    }

    /**
     * topic length > 64
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_topicOverMaxLen() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { "jshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsffjshfljjdkdkfeffslkfsnkhkhhj" };
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	    assertEquals(ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode(), e.getCode());
	}
    }

    /**
     * topics is empty
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_topicsIsEmpty() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = {};
	    String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.TOPIC_LIST_IS_NULL.getCode(), e.getCode());
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	}
    }

    /**
     * topic contain special char
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_topicContainSpecialChar() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();

	    char[] charStr = { 69, 72, 31 };
	    String illegalTopic = new String(charStr);
	    String[] topics = { illegalTopic };
	    String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * topic contain Chinese char
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_topicsContainChiChar() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { "中国" };
	    String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	    assertEquals(ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode(), e.getCode());
	}
    }

    /**
     * topics contain multiple topic ,offset is legal and exists
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_containMultipleTopic() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName, topic2, topic3 };
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", new ConsumerListener());
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	    assertNull(e);
	}
    }

    /**
     * groupId is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_groupIdIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, null, this.eventId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	}
    }

    /**
     * groupId is not a number
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_groupIdIsNotNum() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, "sdfsf", this.eventId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_GROUP_ID_INVALID.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * group id not exist
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe__groupIdNotExist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, "4", this.eventId, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.WE3SDK_UNKNOWN_GROUP.getCode(), e.getCode());
	    log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
	}
    }

    /**
     * offset is WeEvent.OFFSET_FRIST
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_offsetIsFrist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_FIRST, "sdk",
		    new ConsumerListener());
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	    assertNull(e);
	}
	Thread.sleep(wait3s);
    }

    /**
     * offset is WeEvent.OFFSET_FRIST
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_offsetIsLast() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	    assertNull(e);
	}

	Thread.sleep(wait3s);
    }

    /**
     * offset is normal eventId
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_normalEventId() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk",
		    new IConsumer.ConsumerListener() {
			@Override
			public void onEvent(String subscriptionId, WeEvent event) {
			    log.info("onEvent: {}", event);
			    assertTrue(!event.getEventId().isEmpty());
			}

			@Override
			public void onException(Throwable e) {
			    log.error("onException", e);
			    fail();
			}
		    });
	    assertNotNull(result);
	} catch (BrokerException e) {
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	    assertNull(e);
	}

	Thread.sleep(wait3s);
    }

    /**
     * offset is illegal
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_offsetIsIllegal() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, groupId, "lsjflsjfljls", "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), e.getCode());
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	}
    }

    /**
     * topics exists,offset height > current block height
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_offsetNumGtBlock() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId + "000", "sdk",
		    new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.EVENT_ID_IS_MISMATCH.getCode(), e.getCode());
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	}
    }

    /**
     * offset is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_offsetIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, groupId, null, "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	}
    }

    /**
     * offset is blank " "
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_offsetIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, groupId, " ", "sdk", new ConsumerListener());
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.OFFSET_IS_BLANK.getCode(), e.getCode());
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	}
    }

    /**
     * listener is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_listernerIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", null);
	    assertNull(result);
	} catch (BrokerException e) {
	    assertEquals(ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode(), e.getCode());
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	}
    }

    /**
     * subId exist
     * 
     * @throws InterruptedException
     */
    @Test
    public void testUnSubscribe_existSuId() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());
	try {
	    this.iConsumer.startConsumer();
	    String subId = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
		    new ConsumerListener());
	    boolean result = this.iConsumer.unSubscribe(subId);
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("unSubscribe(subId)", e);
	    assertNull(e);
	}
    }

    /**
     * subId not exists
     * 
     * @throws InterruptedException
     */
    @Test
    public void testUnSubscribe_subIdNotExist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());
	try {
	    this.iConsumer.startConsumer();
	    boolean result = this.iConsumer.unSubscribe("sfsghhr");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("unSubscribe(subId) ", e);
	    assertEquals(ErrorCode.SUBSCRIPTIONID_NOT_EXIST.getCode(), e.getCode());
	}
    }

    /**
     * subId is blank " "
     * 
     * @throws InterruptedException
     */
    @Test
    public void testUnSubscribe_subIdIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());
	try {
	    this.iConsumer.startConsumer();
	    boolean result = this.iConsumer.unSubscribe("");
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("unSubscribe(subId)", e);
	    assertEquals(ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * subId is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testUnSubscribe_subIdIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());
	try {
	    this.iConsumer.startConsumer();
	    boolean result = this.iConsumer.unSubscribe(null);
	    assertNull(result);
	} catch (BrokerException e) {
	    log.error("unSubscribe(subId)", e);
	    assertEquals(ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode(), e.getCode());
	}
    }

    /**
     * start Consumer Test
     */
    @Test
    public void testStartConsumer() {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    assertTrue(this.iConsumer.startConsumer());
	} catch (BrokerException e) {
	    log.info("start consumer error: ", e);
	    assertNull(e);
	}
    }

    /**
     * shutdownConsumer Test
     */
    @Test
    public void testShutdownConsumer() {
	log.info("===================={}", this.testName.getMethodName());

	assertTrue(this.iConsumer.shutdownConsumer());
    }

    /**
     * test shutdown Multiple 3 times
     */
    @Test
    public void testShutdownConsumer_multiple() {
	log.info("===================={}", this.testName.getMethodName());

	assertTrue(this.iConsumer.shutdownConsumer());
	assertTrue(this.iConsumer.shutdownConsumer());
	assertTrue(this.iConsumer.shutdownConsumer());
    }

}

class ConsumerListener implements IConsumer.ConsumerListener {

    @Override
    public void onEvent(String subscriptionId, WeEvent event) {

    }

    @Override
    public void onException(Throwable e) {

    }

}
