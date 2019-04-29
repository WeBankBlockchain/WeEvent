package com.webank.weevent.ST.plugin;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Slf4j
public class ConsumerTest extends JUnitTestBase {
    private final String topic2 = this.topicName + "2";
    private final String topic3 = this.topicName + "3";
    private static final long wait3s = 100;

    private IProducer iProducer;
    private IConsumer iConsumer;
    private String lastEventId = "";

    @Before
    public void before() throws Exception {
        this.iProducer = IProducer.build();
        this.iConsumer = IConsumer.build();

        assertTrue(this.iProducer.open(this.topicName));
        assertTrue(this.iProducer.open(this.topic2));
        assertTrue(this.iProducer.open(this.topic3));
        assertTrue(this.iProducer.startProducer());

        String data = String.format("hello world! %s", System.currentTimeMillis());
        WeEvent weEvent = new WeEvent(this.topicName, data.getBytes());
        SendResult sendResultDto = this.iProducer.publish(weEvent);

        assertEquals(SendResult.SendResultStatus.SUCCESS, sendResultDto.getStatus());
        this.lastEventId = sendResultDto.getEventId();
        log.info("publish lastEventId: {}", this.lastEventId);

        assertTrue(this.lastEventId.length() > 1);

        // charset with utf-8
        data = String.format("中文消息! %s", System.currentTimeMillis());
        weEvent = new WeEvent(this.topicName, data.getBytes());
        sendResultDto = this.iProducer.publish(weEvent);

        assertEquals(SendResult.SendResultStatus.SUCCESS, sendResultDto.getStatus());
        this.lastEventId = sendResultDto.getEventId();
        log.info("publish lastEventId charset: {}", this.lastEventId);

        assertTrue(this.lastEventId.length() > 1);

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
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe1() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(null, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe("", WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe3() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(" ", WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  length > 32
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe4() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe("jshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff", WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset=WeEvent.OFFSET_FIRST
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe5() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    assertNotNull(event);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            System.out.println("result id: " + result);
            assertNotNull(result);
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset=WeEvent.OFFSET_LAST
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe6() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
            assertNotNull(result);
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset is general string like lsjflsjfljls
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe7() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "lsjflsjfljls", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset string like lsjfls-jfljls
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe8() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "lsjfls-jfljls", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset string like 4646464-jfljls
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe9() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "4646464-jfljls", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset string like lsjfls-854546
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe10() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "lsjfls-854546", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset eventId  blolck number > blockchain block height
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe11() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "64864-364510000", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_MISMATCH.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset eventId length >32
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe12() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "123456789012345678901234567890-67", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertNotNull(e);
            assertNotNull(e.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe13() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, null, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe14() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe15() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, " ", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,listener is null 500401
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe16() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "6464-464", "sdk", null);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic  exists,offset 为eventId "366347-6154"
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe17() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
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
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic is not exists,offset 为eventId "366347-6154"
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe18() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe("sdlkufhdsighfskhdsf", "sdk", "3667-6154", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {

                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic contian special character withoutin 32-128
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe19() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(illegalTopic, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }

                @Override
                public void onEvent(String subscriptionId, WeEvent event) {

                }
            });
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topic contian Chinese character
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe20() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe("中国", "3667-6154", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {

                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe1() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();

        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(null, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_MODEL_MAP_IS_NULL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put("", "5486-5848");

        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe3() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(" ", "5486-5848");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  length > 32
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe4() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put("jshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff", "5486-5848");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key not exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe5() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put("jsfldsjflds", "5486-5848");

        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  exists,topis value=WeEvent.OFFSET_FIRST
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe6() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, WeEvent.OFFSET_FIRST);
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
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
            log.error("subscribe(topics,listener) method error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  exists,topis value=WeEvent.OFFSET_LAST
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe7() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, WeEvent.OFFSET_LAST);
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
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
            log.error("subscribe(topics,listener) method error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String offset, ConsumerListener listener)
     * topics key  exists,topis value除WeEvent.OFFSET_LAST外的不合法字符串
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe8() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, "lsjflsjfljls");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  exists,topis value除WeEvent.OFFSET_LAST外的不合法字符串2
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe9() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, "lsjfls-jfljls");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  exists,topis value除WeEvent.OFFSET_LAST外的不合法字符串3
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe10() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, "4646464-jfljls");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  exists,topis value除WeEvent.OFFSET_LAST外的不合法字符串4
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe11() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, "lsjfls-854546");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  exists,topis value 块高大于当前区块链块高     36451过大
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe12() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, "64864-364510000");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_MISMATCH.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  exists,topis value length >32
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe13() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, "123456789012345678901234567890-67");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertNotNull(e);
            assertNotNull(e.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  exists,topis value is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe14() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, null);
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_MODEL_MAP_IS_NULL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics key  exists,topis value ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe15() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, "");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topic  exists,offset is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe16() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, " ");

        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics  exists,listener is null 500401
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe17() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, lastEventId);
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", null);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics 是空的
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe20() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_MODEL_MAP_IS_NULL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics topic contain special character without in 32-128
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe21() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        char[] charStr = {69, 72, 31};
        String illegalTopic = new String(charStr);
        topics.put(illegalTopic, lastEventId);
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics topic contain Chinese character
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe22() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put("中国", lastEventId);
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }

        Thread.sleep(wait3s);
    }


    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topic key exists,value 为eventId "366347-6154"
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe18() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, "3667-6154");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
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
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(Map<String, String> topics, ConsumerListener listener)
     * topics 含有多个
     *
     * @throws InterruptedException
     */
    @Test
    public void testMapSubscribe19() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topics = new HashMap<>();
        topics.put(this.topicName, "3667-6100");
        topics.put(this.topic2, lastEventId);
        topics.put(this.topic3, "36877-6100");
        try {
            this.iConsumer.startConsumer();
            Map<String, String> result = this.iConsumer.subscribe(topics, "sdk", new IConsumer.ConsumerListener() {
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
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
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
    public void testUnSubscribe1() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            String subId = this.iConsumer.subscribe(this.topicName, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent: {}", event);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            boolean result = this.iConsumer.unSubscribe(subId);
            assertTrue(result);
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
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
    public void testUnSubscribe2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            String subId = this.iConsumer.subscribe(this.topicName, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent: {}", event);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            boolean result = this.iConsumer.unSubscribe("sfsghhr");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
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
    public void testUnSubscribe3() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            String subId = this.iConsumer.subscribe(this.topicName, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent: {}", event);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            boolean result = this.iConsumer.unSubscribe("");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
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
    public void testUnSubscribe4() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            String subId = this.iConsumer.subscribe(this.topicName, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent: {}", event);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            boolean result = this.iConsumer.unSubscribe(" ");
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
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
    public void testUnSubscribe5() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            String subId = this.iConsumer.subscribe("com.webank.test.matthew", "3434376330323266-7-1043", "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("onEvent: {}", event);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            boolean result = this.iConsumer.unSubscribe(null);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }
}
