package com.webank.weevent.broker.fisco;

import java.nio.charset.StandardCharsets;
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

/**
 * FiscoBcosBroker4Consumer Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since 11/08/2018
 */
@Slf4j
public class FiscoBcosBroker4ConsumerTest extends JUnitTestBase {
    private final String topic2 = topicName + "1";
    private final String topic3 = topicName + "2";
    private final long wait3s = 3000;
    private final String groupId = "1";
    private Map<String, String> extensions = new HashMap<>();

    private IProducer iProducer;
    private IConsumer iConsumer;
    private boolean result = false;
    private String lastEventId = "";
    private volatile int received = 0;

    @Before
    public void before() throws Exception {
        this.iProducer = IProducer.build();
        this.iConsumer = IConsumer.build();
        extensions.put("weevent-url", "https://github.com/WeBankFinTech/WeEvent");
        assertTrue(this.iProducer.open(this.topicName, groupId));
        assertTrue(this.iProducer.open(this.topic2, groupId));
        assertTrue(this.iProducer.open(this.topic3, groupId));
        assertTrue(this.iProducer.startProducer());

        String data = String.format("hello world! %s", System.currentTimeMillis());
        WeEvent weEvent = new WeEvent(topicName, data.getBytes(), extensions);
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

        this.result = false;
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

    @Test
    public void testSingleTopicSubscribe_lastEventIdCheck_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();
        try {
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
            log.error("subscribe error:{}", e);
        }
    }

    @Test
    public void testSingleTopicSubscribe_lastEventIdCheck_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();
        try {
            String result = this.iConsumer.subscribe(this.topicName, groupId, "123", "sdk", new IConsumer.ConsumerListener() {
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
            log.error("subscribe error:{}", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribe_lastEventIdCheck_03() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        try {
            String result = this.iConsumer.subscribe(this.topicName, groupId, "123456789012345678901234567890123456789012345678901234567890123456", "sdk", new IConsumer.ConsumerListener() {
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
        }
    }

    @Test
    public void testSingleTopicSubscribeLastEventID_check_04() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.iConsumer.startConsumer();
        try {
            String result = this.iConsumer.subscribe(this.topicName, groupId, "xxx_xxxx", "sdk", new IConsumer.ConsumerListener() {
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
            log.error("subscribe err:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    /**
     * Method: subscribe(String topic, String lastEventId, ConsumerListener listener)
     */
    @Test
    public void testSingleTopicSubscribe_lastEventId() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.received = 0;
        this.iConsumer.startConsumer();
        try {
            String result = this.iConsumer.subscribe(this.topicName, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("********** {}", event);

                    assertTrue(!event.getEventId().isEmpty());
                    received++;
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertTrue(!result.isEmpty());
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
        }
        log.info("lastEventId: {}", this.lastEventId);
        assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes(), extensions), groupId).getStatus());

        Thread.sleep(wait3s);
    }

    /**
     * Method: subscribe(String topic, boolean begin, ConsumerListener listener)
     */
    @Test
    public void testSingleTopicSubscribe_boolean_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.received = 0;
        this.iConsumer.startConsumer();

        try {
            String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("********** {}", new String(event.getContent()));

                    assertTrue(!event.getEventId().isEmpty());
                    received++;
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });
            assertTrue(!result.isEmpty());
        } catch (BrokerException e) {

        }

        assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes(), extensions), groupId).getStatus());
        Thread.sleep(wait3s);
    }

    /**
     * Method: subscribe(String topic, boolean begin, ConsumerListener listener)
     */
    @Test
    public void testSingleTopicSubscribe_boolean_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.received = 0;
        this.iConsumer.startConsumer();
        String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {
                log.info("********** {}", event);

                assertTrue(!event.getEventId().isEmpty());
                received++;
            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
                fail();
            }
        });
        assertTrue(!result.isEmpty());

        log.info("lastEventId: {}", this.lastEventId);
        assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes(), extensions), groupId).getStatus());
        Thread.sleep(wait3s);
        assertTrue(this.received > 0);
    }

    @Test
    public void testSingleTopicSubscribe_list_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.iConsumer.startConsumer();
        IConsumer.ConsumerListener consumerListener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        };

        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk", consumerListener);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribe_list_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.iConsumer.startConsumer();
        IConsumer.ConsumerListener consumerListener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        };

        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", consumerListener);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testSingleTopicSubscribe_list_04() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.iConsumer.startConsumer();
        IConsumer.ConsumerListener consumerListener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        };

        try {
            String[] topics = {this.topicName};
            this.iConsumer.subscribe(topics, groupId, null, "sdk", consumerListener);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
        }
    }

    /**
     * Method: subscribe(String topic, String lastEventId, ConsumerListener listener)
     */
    @Test
    public void testRepeatTopicSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        IConsumer.ConsumerListener consumerListener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        };
        try {
            // normal
            String result = this.iConsumer.subscribe(this.topicName, groupId, this.lastEventId, "sdk", consumerListener);
            assertTrue(!result.isEmpty());

            // allow again
            result = this.iConsumer.subscribe(this.topicName, groupId, this.lastEventId, "sdk", consumerListener);
            assertTrue(!result.isEmpty());
        } catch (Exception e) {
            log.error("subscribe err:{}", e);
        }
    }

    /**
     * Method: subscribe(Map<String, Object> map, ConsumerListener listener)
     */
    @Test
    public void testMultipleTopicSubscribe_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.iConsumer.startConsumer();
        IConsumer.ConsumerListener consumerListener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        };

        String[] topics = {this.topicName, this.topic2, this.topic3};
        String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", consumerListener);
        assertFalse(result.isEmpty());
    }

    /**
     * Method: subscribe(topics, groupId, offset, interfaceType, consumerListener);
     */
    @Test
    public void testMultipleTopicSubscribe_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.iConsumer.startConsumer();
        IConsumer.ConsumerListener consumerListener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        };

        try {
            String[] topics = {this.topicName, this.topic2, this.topic3};
            this.iConsumer.subscribe(topics, groupId, "xxx_xxx", "sdk", consumerListener);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    @Test
    public void testMultipleTopicSubscribe_05() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.iConsumer.startConsumer();
        IConsumer.ConsumerListener consumerListener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        };

        String[] topics = {this.topicName};
        String result = this.iConsumer.subscribe(topics, groupId, WeEvent.OFFSET_LAST, "sdk", consumerListener);
        assertFalse(result.isEmpty());
    }

    /**
     * Method: unsubscribe(String, topic)
     */
    @Test
    public void testUnsubscribe_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        });

        try {
            this.iConsumer.unSubscribe(null);
        } catch (BrokerException e) {
            log.error("subscribe error:{}", e);
        }
    }

    /**
     * Method: unsubscribe(String subId)
     */
    @Test
    public void testUnsubscribe_03() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        String subscription = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        });
        this.result = this.iConsumer.unSubscribe(subscription);
        assertTrue(this.result);
    }

    /**
     * Method: unsubscribe(String, topic)
     */
    @Test
    public void testUnsubscribe_04() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.iConsumer.startConsumer();
        IConsumer.ConsumerListener consumerListener = new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        };

        String[] topics = {this.topicName, this.topic2};
        String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", consumerListener);
        assertFalse(result.isEmpty());

        this.result = this.iConsumer.unSubscribe(result);
        assertTrue(this.result);
    }


    @Test
    public void testConsumerIsStart() throws Exception {

        if (!iConsumer.isStarted()) {
            iConsumer.startConsumer();
        }
        String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {
                log.info("********** {}, content {}", event, new String(event.getContent()));

                assertTrue(!event.getEventId().isEmpty());
                received++;
            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
                fail();
            }
        });
        iConsumer.shutdownConsumer();
        if (!iConsumer.isStarted()) {
            iConsumer.startConsumer();
        }
    }

    /**
     * Method: subscribe(String[] topics, groupId,offset ,interfaceType,ConsumerListener listener)
     */
    @Test
    public void testSubscribeCharacterSet() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        this.received = 0;
        try {
            String[] topics = {this.topicName};
            String result = this.iConsumer.subscribe(topics, groupId, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
                @Override
                public void onEvent(String subscriptionId, WeEvent event) {
                    log.info("********** {}, content {}", event, new String(event.getContent()));

                    assertTrue(!event.getEventId().isEmpty());
                    received++;
                }

                @Override
                public void onException(Throwable e) {
                    log.error("onException", e);
                    fail();
                }
            });

            assertFalse(result.isEmpty());
        } catch (BrokerException e) {
            fail();
        }

        SendResult sendResult = this.iProducer
                .publish(new WeEvent(this.topicName,
                        String.format("我是中文. %s", System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8), extensions), groupId);

        assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
        Thread.sleep(wait3s);
    }

    /**
     * Method: shutdownConsumer()
     */
    @Test
    public void testShutdownConsumer() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        });
        assertTrue(!result.isEmpty());
        assertTrue(this.iConsumer.shutdownConsumer());
    }
}
