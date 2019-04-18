package com.webank.weevent.broker.fisco;

import java.nio.charset.StandardCharsets;
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

    private IProducer iProducer;
    private IConsumer iConsumer;
    private boolean result = false;
    private String lastEventId = "";
    private volatile int received = 0;

    @Before
    public void before() throws Exception {
        this.iProducer = IProducer.build();
        this.iConsumer = IConsumer.build();

        assertTrue(this.iProducer.open(this.topicName));
        assertTrue(this.iProducer.open(this.topic2));
        assertTrue(this.iProducer.open(this.topic3));
        assertTrue(this.iProducer.startProducer());

        String data = String.format("hello world! %s", System.currentTimeMillis());
        WeEvent weEvent = new WeEvent(topicName, data.getBytes());
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
            String result = this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribe_lastEventIdCheck_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();
        try {
            String result = this.iConsumer.subscribe(this.topicName, "123", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, "1234567890123456789012345678901234567890", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, "xxx_xxxx", "sdk", new IConsumer.ConsumerListener() {
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
            String result = this.iConsumer.subscribe(this.topicName, this.lastEventId, "sdk", new IConsumer.ConsumerListener() {
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
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.OFFSET_IS_BLANK.getCode());
        }
        log.info("lastEventId: {}", this.lastEventId);
        assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes())).getStatus());
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
            String result = this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
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
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes())).getStatus());
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
        try {
            String result = this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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

        }
        log.info("lastEventId: {}", this.lastEventId);
        assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes())).getStatus());
        Thread.sleep(wait3s);
        assertTrue(this.received > 0);
    }

    /**
     * Method: subscribe(Map<String, Object> map, ConsumerListener listener)
     */
    @Test
    public void testSingleTopicSubscribe_map_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, WeEvent.OFFSET_LAST);
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
        Map<String, String> result = this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        assertTrue(!result.isEmpty());
    }

    /**
     * Method: subscribe(Map<String, Object> map, ConsumerListener listener)
     */
    @Test
    public void testSingleTopicSubscribe_map_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, this.lastEventId);
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

        Map<String, String> result = this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        assertTrue(!result.isEmpty());

    }

    @Test
    public void testSingleTopicSubscribe_map_03() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(null, this.lastEventId);

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
            this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    @Test
    public void testSingleTopicSubscribe_map_04() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, null);

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
            this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_MODEL_MAP_IS_NULL.getCode());
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
            String result = this.iConsumer.subscribe(this.topicName, this.lastEventId, "sdk", consumerListener);
            assertTrue(!result.isEmpty());

            // allow again
            result = this.iConsumer.subscribe(this.topicName, this.lastEventId, "sdk", consumerListener);
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
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, WeEvent.OFFSET_LAST);
        topicModelMap.put(this.topic2, WeEvent.OFFSET_FIRST);
        topicModelMap.put(this.topic3, this.lastEventId);
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

        Map<String, String> result = this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        assertTrue(!result.isEmpty());
    }

    /**
     * Method: subscribe(Map<String, Object> map, ConsumerListener listener)
     */
    @Test
    public void testMultipleTopicSubscribe_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, WeEvent.OFFSET_LAST);
        topicModelMap.put(this.topic2, "xxx_xxx");
        topicModelMap.put(this.topic3, this.lastEventId);
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
            this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    /**
     * Method: subscribe(Map<String, Object> map, ConsumerListener listener)
     */
    @Test
    public void testMultipleTopicSubscribe_03() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, WeEvent.OFFSET_LAST);
        topicModelMap.put(this.topic2, this.lastEventId);
        topicModelMap.put(this.topic3, null);
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
            this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_MODEL_MAP_IS_NULL.getCode());
        }
    }

    /**
     * Method: subscribe(Map<String, Object> map, ConsumerListener listener)
     */
    @Test
    public void testMultipleTopicSubscribe_04() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, WeEvent.OFFSET_LAST);
        topicModelMap.put(this.topic2, this.lastEventId);
        topicModelMap.put("topic-AAA", this.lastEventId);
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
            this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
        }
    }


    /**
     * Method: subscribe(Map<String, Object> map, ConsumerListener listener)
     */
    @Test
    public void testMultipleTopicSubscribe_05() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, WeEvent.OFFSET_LAST);
        topicModelMap.put(this.topic2, this.lastEventId);
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

        Map<String, String> result = this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        assertTrue(!result.isEmpty());

        topicModelMap.clear();
        topicModelMap.put(this.topic3, this.lastEventId);
        result = this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        assertTrue(!result.isEmpty());
    }

    /**
     * Method: subscribe(Map<String, Object> map, ConsumerListener listener)
     */
    @Test
    public void testMultipleTopicSubscribe_06() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, WeEvent.OFFSET_LAST);
        topicModelMap.put(this.topic2, this.lastEventId);
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

        Map<String, String> result = this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        assertTrue(!result.isEmpty());

        topicModelMap.clear();
        topicModelMap.put(this.topic2, this.lastEventId);
        result = this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        assertTrue(!result.isEmpty());
    }

    /**
     * Method: unsubscribe(String, topic)
     */
    @Test
    public void testUnsubscribe_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
     * Method: unsubscribe(String, topic)
     */
    @Test
    public void testUnsubscribe_02() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
            }
        });

        this.result = this.iConsumer.unSubscribe("topic-AAA");
        assertTrue(!this.result);
    }

    /**
     * Method: unsubscribe(String, topic)
     */
    @Test
    public void testUnsubscribe_03() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        String subscription = this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, WeEvent.OFFSET_LAST);
        topicModelMap.put(this.topic2, this.lastEventId);
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
        Map<String, String> result = this.iConsumer.subscribe(topicModelMap, "sdk", consumerListener);
        assertTrue(!result.isEmpty());

        for (Map.Entry<String, String> entry : result.entrySet()) {
            this.result = this.iConsumer.unSubscribe(entry.getValue());
            assertTrue(this.result);
        }
    }


    @Test
    public void testConsumerIsStart() throws Exception {

        if (!iConsumer.isStarted()) {
            iConsumer.startConsumer();
        }
        String result = this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_FIRST, "sdk", new IConsumer.ConsumerListener() {
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
     * Method: subscribe(String topic, ConsumerListener listener)
     */
    @Test
    public void testSubscribeCharacterSet() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        this.received = 0;
        Map<String, String> topicModelMap = new HashMap<>();
        topicModelMap.put(this.topicName, this.lastEventId);
        Map<String, String> result = new HashMap<>();
        try {
            result = this.iConsumer.subscribe(topicModelMap, "sdk", new IConsumer.ConsumerListener() {
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
        } catch (BrokerException e) {
            assertTrue(!result.isEmpty());
        }

        assertEquals(SendResult.SendResultStatus.SUCCESS,
                this.iProducer
                        .publish(new WeEvent(this.topicName,
                                String.format("我是中文. %s", System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8)))
                        .getStatus());
        Thread.sleep(wait3s);
    }

    /**
     * Method: shutdownConsumer()
     */
    @Test
    public void testShutdownConsumer() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        this.iConsumer.startConsumer();

        String result = this.iConsumer.subscribe(this.topicName, WeEvent.OFFSET_LAST, "sdk", new IConsumer.ConsumerListener() {
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
