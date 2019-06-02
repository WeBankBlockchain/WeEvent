package com.webank.weevent.ST.plugin;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.broker.plugin.IConsumer.ConsumerListener;
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe1() throws InterruptedException {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe2() throws InterruptedException {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  length > 64
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe3() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
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
    public void testSubscribe4() throws InterruptedException {
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
            log.info("result id: " + result);
            assertNotNull(result);
        } catch (BrokerException e) {
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
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
    public void testSubscribe5() throws InterruptedException {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is general string like lsjflsjfljls
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe6() throws InterruptedException {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset string like 317e7c4c-fsfds-sdfsdf
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe7() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
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
    public void testSubscribe8() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
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
    public void testSubscribe9() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
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
    public void testSubscribe10() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_MISMATCH.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
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
    public void testSubscribe11() throws InterruptedException {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
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
    public void testSubscribe12() throws InterruptedException {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is ""
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe13() throws InterruptedException {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset is " "
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe14() throws InterruptedException {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,listener is null 
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe15() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, this.lastEventId, "sdk", null);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
            log.error("SingleTopicSubscribe_lastEventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic  exists,offset eventId exist
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe16() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(this.topicName, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic is not exists,offset is eventId legal
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe17() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe("sdlkufhdsighfskhdsf", groupId, "df47fc12-75-3", "sdk", new IConsumer.ConsumerListener() {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic contian special character withoutin 32-128
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe18() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe(illegalTopic, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
     * test subscribe(String topic, String groupId, String offset, String interfaceType, ConsumerListener listener)
     * topic contain Chinese character
     *
     * @throws InterruptedException
     */
    @Test
    public void testSubscribe19() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String result = this.iConsumer.subscribe("中国", groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
    public void testListSubscribe1() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {""};
            String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
    public void testListSubscribe2() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {" "};
            String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
     * topics key  length > 64
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe3() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {"jshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsffjshfljjdkdkfeffslkfsnkhkhhj"};
            String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics  exists, offset = WeEvent.OFFSET_FIRST
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe4() throws InterruptedException {
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
        }
        Thread.sleep(wait3s);
    }

    /**
     * topics exists,offset = WeEvent.OFFSET_LAST
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe5() throws InterruptedException {
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
        }
        Thread.sleep(wait3s);
    }

    /**
     * topics exists, offset is illegal
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe6() throws InterruptedException {
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
    public void testListSubscribe7() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId + "000", "sdk", new IConsumer.ConsumerListener() {
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
    public void testListSubscribe8() throws InterruptedException {
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
    public void testListSubscribe9() throws InterruptedException {
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
    public void testListSubscribe10() throws InterruptedException {
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
    public void testListSubscribe11() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", null);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics is blank
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe12() throws InterruptedException {
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
    public void testListSubscribe13() throws InterruptedException {
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
    public void testListSubscribe14() throws InterruptedException {
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
            log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }

        Thread.sleep(wait3s);
    }

    /**
     * topics exists ,offset is legal and exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe15() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * topics contain multipe topic ,offset is legal and exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testListSubscribe16() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());

        try {
            this.iConsumer.startConsumer();
            String[] topics = {this.topicName,topic2,topic3};
            String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
            String subId = this.iConsumer.subscribe(this.topicName, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
            log.error("unSubscribe(subId)", e);
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
            String subId = this.iConsumer.subscribe(this.topicName, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
            log.error("unSubscribe(subId) ", e);
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
            String subId = this.iConsumer.subscribe(this.topicName, groupId, "", "sdk", new IConsumer.ConsumerListener() {
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
            log.error("unSubscribe(subId)", e);
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
    public void testUnSubscribe4() throws InterruptedException {
        log.info("===================={}", this.testName.getMethodName());
        try {
            this.iConsumer.startConsumer();
            String subId = this.iConsumer.subscribe(this.topicName, groupId, " ", "sdk", new IConsumer.ConsumerListener() {
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
            log.error("unSubscribe(subId) ", e);
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
            String subId = this.iConsumer.subscribe("com.webank.test.matthew", groupId, null, "sdk", new IConsumer.ConsumerListener() {
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
            log.error("unSubscribe(subId)", e);
        }

        Thread.sleep(wait3s);
    }
}
