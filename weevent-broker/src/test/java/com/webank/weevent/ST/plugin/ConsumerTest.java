package com.webank.weevent.ST.plugin;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Slf4j
public class ConsumerTest extends JUnitTestBase {
    private final String topic2 = this.topicName + "2";
    private final String topic3 = this.topicName + "3";
    private static final long wait3s = 100;

    private IProducer iProducer;
    private IConsumer iConsumer;
    private String eventId = "";
    private String groupId = "1";
    private String subId ="";
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
        
        this.subId = this.iConsumer.subscribe(this.topicName, groupId, this.eventId, "sdk", new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {
            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
                fail();
            }
        });
        assertNotEquals(subId, "");
    }

    @After
    public void after() throws Exception {
        assertTrue(this.iProducer.shutdownProducer());
        assertTrue(this.iConsumer.shutdownConsumer());
        this.iProducer = null;
        this.iConsumer = null;
    }

    /**
     * Method: startConsumer()
     */
    @Test
    public void testStartConsumer() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        assertTrue(this.iConsumer.startConsumer());
    }
    
    /**
     * Method: shutDownConsumer()
     */
    @Test
    public void testShutdownConsumer() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        assertTrue(this.iConsumer.shutdownConsumer());
    }

    /**
     * test shutdown Multiple 3 times
     */
    @Test
    public void testMultipleShutdownConsumer() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        assertTrue(this.iConsumer.shutdownConsumer());
        assertTrue(this.iConsumer.shutdownConsumer());
        assertTrue(this.iConsumer.shutdownConsumer());
    }

    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_topicIsBlank() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe("", groupId, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_topicIsBlank2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(" ", groupId, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  length > 64
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_topicOverMaxLen() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(
        	    "qwertyuioplkjhgfdsazxcvbnmlkjhgfjshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff", 
        	    groupId, WeEvent.OFFSET_FIRST, 
        	    "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic contian special character withoutin 32-128
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_containSpeciaChar() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(illegalTopic, groupId, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }

                @Override
                public void onEvent(String subscriptionId, WeEvent event) {

                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic contain Chinese character
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_containChinChar() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe("中国", groupId, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {

                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_groupIdIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, null, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertTrue(!event.getEventId().isEmpty());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId is not number
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_groupIdIsNotNum() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "sdfsf", this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertTrue(!event.getEventId().isEmpty());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId = 0
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_groupIdNumEq0() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "0", this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertTrue(!event.getEventId().isEmpty());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId < 0
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_groupIdNumLt0() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "-1", this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId is not currentGroupId
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_groupIdNotCurrentId() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "4", this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    } 

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset=WeEvent.OFFSET_FIRST
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_offsetIsFrist() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    assertNotNull(event);
                    assertEquals(topicName,event.getTopic());
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset=WeEvent.OFFSET_LAST
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_offsetIsLast() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertNotNull(event);
                    assertEquals(topicName,event.getTopic());
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_normalEventId() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertNotNull(event);
                    assertEquals(topicName,event.getTopic());
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
        }
        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is general string like lsjflsjfljls
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_offsetIsIllegal() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "lsjflsjfljls", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }


    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset string like 317e7c4c-fsfds-sdfsdf
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_ofssetIsIllegal2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-fsfds-sdfsdf", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset string like 317e7c4c-75-sdfsdf
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_ofssetIsIllegal3() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-75-sdfsdf", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset string like 317e7c4c-lsjfls-329
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_ofssetIsIllegal4() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-lsjfls-329", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset eventId  blolck number > blockchain block height
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_offsetNumGtBlock() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-75-32900000", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_MISMATCH.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset eventId length >64
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_offsetOverMaxLen() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(
        	    this.topicName, 
        	    groupId, 
        	    "317e7c4c45gfjfs5369875452364875962-1213456789632145678564547896354775-329", 
        	    "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_offsetIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, null, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_offsetIsBlank() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_offsetIsBlank2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, " ", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,listener is null 
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe_listenerIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, this.eventId, "sdk", null);
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

 
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic not same first
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicNotSameFirst() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(topic2, groupId, WeEvent.OFFSET_LAST, subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_MATCH.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicIsBlank() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            
            String result = this.iConsumer.subscribe("", groupId, WeEvent.OFFSET_FIRST, subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicIsBlank2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(" ", groupId, WeEvent.OFFSET_FIRST, subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  length > 64
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicOverMaxLen() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(
        	    "qwertyuioplkjhgfdsazxcvbnmlkjhgfjshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff", 
        	    groupId, WeEvent.OFFSET_FIRST,
        	    subId,
        	    "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, null, this.eventId, subId,"sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertTrue(!event.getEventId().isEmpty());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId is not number
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdIsNotNum() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            
            String result = this.iConsumer.subscribe(this.topicName, "sdfsf", this.eventId, subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertTrue(!event.getEventId().isEmpty());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId = 0
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdNumEq0() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "0", this.eventId, subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertTrue(!event.getEventId().isEmpty());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId < 0
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdNumLt0() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "-1", this.eventId, subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId is not currentGroupId
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdNotCurrentId() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "4", this.eventId, subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    } 
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset=WeEvent.OFFSET_FIRST
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsFrist() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_FIRST, subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    assertNotNull(event);
                    assertEquals(topicName,event.getTopic());
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
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset=WeEvent.OFFSET_LAST
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsLast() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertNotNull(event);
                    assertEquals(topicName,event.getTopic());
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
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_normalEventId() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, this.eventId, subId,"sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertNotNull(event);
                    assertEquals(topicName,event.getTopic());
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
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * subId is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_subIdIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, null, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * subId is blank ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_subIdIsBlank() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * subId is blank " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_subIdIsBlank2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, " ", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.SUBSCRIPTIONID_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * subId is illegal
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_subIdIsIllegal() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdgsgsgdg", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.SUBSCRIPTIONID_FORMAT_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * subId is legal but not exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_subIdIsNotExists() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "ec1776da-1748-4c68-b0eb-ed3e92f9aadb", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertNotNull(event);
                    assertEquals(topicName,event.getTopic());
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
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is general string like lsjflsjfljls
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsIllegal() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "lsjflsjfljls", subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }


    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset string like 317e7c4c-fsfds-sdfsdf
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_ofssetIsIllegal2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-fsfds-sdfsdf", subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset string like 317e7c4c-75-sdfsdf
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_ofssetIsIllegal3() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-75-sdfsdf", subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset string like 317e7c4c-lsjfls-329
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_ofssetIsIllegal4() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-lsjfls-329", subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset eventId  block number > block chain block height
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetNumGtBlock() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-75-32900000", subId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_MISMATCH.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     *  test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset eventId length >64
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetOverMaxLen() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(
        	    this.topicName, 
        	    groupId, 
        	    "317e7c4c45gfjfs5369875452364875962-1213456789632145678564547896354775-329", 
        	    subId,
        	    "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            
            String result = this.iConsumer.subscribe(this.topicName, groupId, null, subId,"sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsBlank() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            
            String result = this.iConsumer.subscribe(this.topicName, groupId, "", subId,"sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsBlank2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, " ", subId,"sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String subscriptionId, String interfaceType, ConsumerListener listener)
     * topic  exists,listener is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_listenerIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            
            String result = this.iConsumer.subscribe(this.topicName, groupId, this.eventId, subId,"sdk", null);
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics key is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_topicIsBlank() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {""};
            String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("subscribe(topics,groupId,offset,interfaceType,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics key is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_topicIsBlank2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {" "};
            String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics key  length > 64
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_topicOverMaxLen() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {"jshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsffjshfljjdkdkfeffslkfsnkhkhhj"};
            String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics is blank
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_topicsIsEmpty() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {};
            String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_LIST_IS_NULL.getCode());
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics topic contain special character without in 32-128
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_topicsContainSpecialChar() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();

            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            String[] topics = {illegalTopic};
            String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST,"sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics topic contain Chinese character
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_topicsContainChiChar() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {"中国"};
            String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics contain multiple topic ,offset is legal and exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_topicsContainMultipleTopic() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName,topic2,topic3};
            String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_groupIdIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, null, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertTrue(!event.getEventId().isEmpty());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId is not number
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_groupIdIsNotNum() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, "sdfsf", this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertTrue(!event.getEventId().isEmpty());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId = 0
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_groupIdNumEq0() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, "0", this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent {}", event);
                    assertTrue(!event.getEventId().isEmpty());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId < 0
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_groupIdNumLt0() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, "-1", this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset= this.eventId groupId is not currentGroupId
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_groupIdNotCurrentId() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, "4", this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_GROUP_ID_INVALID.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    } 
    
    /**
     * topics  exists, offset = WeEvent.OFFSET_FIRST
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_offsetIsFrist() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
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
     * topics exists,offset = WeEvent.OFFSET_LAST
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_offsetIsLast() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
     * topics exists, offset is illegal
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_offsetIsIllegal() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, "lsjflsjfljls", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics exists,offset height > current block height 
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_offsetHeightGtBlock() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, this.eventId + "000", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_MISMATCH.getCode());
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics exists,offset is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_offsetIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, null, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics exists,offset value ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_offsetIsBlank() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, "", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_LIST_IS_NULL.getCode());
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topic  exists,offset is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_offsetIsBlank2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, " ", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_LIST_IS_NULL.getCode());
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics  exists,listener is null 
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_listernerIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", null);
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
        }

        Thread.sleep(wait3s);
    }

    
    /**
     * topics exists ,offset is legal and exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe_normalEventId() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
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
     * test unSubscribe method
     * subId  exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testUnSubscribe_existsSuId() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            boolean result = this.iConsumer.unSubscribe(subId);
            assertTrue(result);
        } catch (BrokerException e) {
            log.error("unSubscribe(subId)", e);
            assertNull(e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test unSubscribe method
     * subId  not exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testUnSubscribe_subIdNotExists() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            boolean result = this.iConsumer.unSubscribe("sfsghhr");
            assertNull(result);
        } catch (BrokerException e) {
            log.error("unSubscribe(subId) ", e);
            assertEquals(e.getCode(), ErrorCode.SUBSCRIPTIONID_NOT_EXIST.getCode()); 
        }

        Thread.sleep(wait3s);
    }

    /**
     * test unSubscribe method
     * subId  is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testUnSubscribe_subIdIsBlank() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            boolean result = this.iConsumer.unSubscribe("");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("unSubscribe(subId)", e);
            assertNull(e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test unSubscribe method
     * subId  is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testUnSubscribe_subIdIsBlank2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            boolean result = this.iConsumer.unSubscribe(" ");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("unSubscribe(subId) ", e);
            assertNull(e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test unSubscribe method
     * subId  is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testUnSubscribe_subIdIsNull() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            boolean result = this.iConsumer.unSubscribe(null);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("unSubscribe(subId)", e);
            assertNull(e);
        }

        Thread.sleep(wait3s);
    }
    
}
