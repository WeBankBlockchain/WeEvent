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
        this.lastEventId = sendResultDto.getEventId();
        log.info("publish lastEventId: {}", this.lastEventId);

        assertTrue(this.lastEventId.length() > 1);

        // charset with utf-8
        data = String.format("中文消息! %s", System.currentTimeMillis());
        weEvent = new WeEvent(this.topicName, data.getBytes(), extensions);
        sendResultDto = this.iProducer.publish(weEvent, groupId);

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
     * topic is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe2() throws InterruptedException {
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
            String result = this.iConsumer.subscribe("jshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff", groupId, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, groupId, "lsjfls-jfljls", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, groupId, "4646464-jfljls", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, groupId, "lsjfls-854546", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, groupId, "64864-364510000", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, groupId, "123456789012345678901234567890-67", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, groupId, "6464-464", "sdk", null);
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
            String result = this.iConsumer.subscribe(this.topicName, groupId, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe("sdlkufhdsighfskhdsf", groupId, "sdk", "3667-6154", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(illegalTopic, groupId, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe("中国", groupId, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
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
     * topics key is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {""};
            String result = this.iConsumer.subscribe(topics, groupId, "5486-5848", "sdk", new IConsumer.ConsumerListener() {
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
     * topics key is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe3() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {" "};
            String result = this.iConsumer.subscribe(topics, groupId, "5486-5848", "sdk", new IConsumer.ConsumerListener() {
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
     * topics key  length > 84
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe4() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {"jshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsffjshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff"};
            String result = this.iConsumer.subscribe(topics, groupId, "5486-5848", "sdk", new IConsumer.ConsumerListener() {
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
     * topics key  exists, topis value=WeEvent.OFFSET_FIRST
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe6() throws InterruptedException {
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
            log.error("subscribe(topics,listener) method error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * topics key  exists,topis value=WeEvent.OFFSET_LAST
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe7() throws InterruptedException {
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
            log.error("subscribe(topics,listener) method error: ", e);
        }
        Thread.sleep(wait3s);
    }

    /**
     * topics key  exists,topis value除WeEvent.OFFSET_LAST外的不合法字符串
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe8() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics key  exists,topis value 块高大于当前区块链块高     36451过大
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe12() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, "64864-364510000", "sdk", new IConsumer.ConsumerListener() {
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
     * topics key  exists,topis value is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe14() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics key  exists,topis value ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe15() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topic  exists,offset is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe16() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics  exists,listener is null 500401
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe17() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", null);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics 是空的
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe20() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {};
            this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
            assertEquals(e.getCode(), ErrorCode.TOPIC_LIST_IS_NULL.getCode());
            log.error("subscribe(topics,listener) method error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics topic contain special character without in 32-128
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe21() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();

            char[] charStr = {69, 72, 31};
            String illegalTopic = new String(charStr);
            String[] topics = {illegalTopic};
            this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST,"sdk", new IConsumer.ConsumerListener() {
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
     * topics topic contain Chinese character
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe22() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {"中国"};
            this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
            String subId = this.iConsumer.subscribe(this.topicName, groupId, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
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
            String subId = this.iConsumer.subscribe(this.topicName, groupId, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
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
            String subId = this.iConsumer.subscribe(this.topicName, groupId, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
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
            String subId = this.iConsumer.subscribe(this.topicName, groupId, "3667-6154", "sdk", new IConsumer.ConsumerListener() {
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
            String subId = this.iConsumer.subscribe("com.webank.test.matthew", groupId, "3434376330323266-7-1043", "sdk", new IConsumer.ConsumerListener() {
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
