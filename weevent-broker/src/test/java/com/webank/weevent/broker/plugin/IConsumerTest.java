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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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
     * test topic is ""
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_topicIsBlank() throws InterruptedException {
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
     * test topic is " "
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_topicIsBlank2() throws InterruptedException {
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
     * test topic length > 64
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_topicOverMaxLen() throws InterruptedException {
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
     * topic contain special char 
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
		    new IConsumer.ConsumerListener() {
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
	Thread.sleep(wait3s);
    }
    
    /**
     * topic contain Chinese char
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_containChinChar() throws InterruptedException {
	 log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe("中国", groupId, this.eventId, "sdk",
		    new IConsumer.ConsumerListener() {
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
	Thread.sleep(wait3s);
    }
    
    /**
     * groupId is null
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_groupIdIsNull() throws InterruptedException {
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
     * groupId is not a number
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_groupIdIsNotNum() throws InterruptedException {
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
     * groupId not exist
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_groupIdNotExist() throws InterruptedException {
	 log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, "4", this.eventId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * offset is WeEvent.OFFSET_FRIST
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsFrist() throws InterruptedException {
	 log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_FIRST, "sdk",
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
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsLast() throws InterruptedException {
	 log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, "sdk",
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
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_normalEventId() throws InterruptedException {
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
     * offset is illegal
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsIllegal() throws InterruptedException {
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
     * offset eventId contain height large than block height
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetNumGtBlock() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-75-32900000", "sdk",
		    new IConsumer.ConsumerListener() {
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
     * offset length > 64
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetOverMaxLen() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId,
		    "317e7c4c45gfjfs5369875452364875962-1213456789632145678564547896354775-329", "sdk",
		    new IConsumer.ConsumerListener() {
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
     * offset is null
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsNull() throws InterruptedException {
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
     * offset is blank " "
     * @throws InterruptedException
     */
    @Test
    public void testSingleSubscribe_offsetIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, " ", "sdk",
		    new IConsumer.ConsumerListener() {
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
     * listener is null
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
            assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
            log.error("SingleTopicSubscribe_eventIdCheck methed error: ", e);
        }

        Thread.sleep(wait3s);
    }
    
    /**
     * topic not same first subscribe 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicNotSameFirst() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(topic2, groupId, WeEvent.OFFSET_LAST, subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * topic is blank " "
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();

	    String result = this.iConsumer.subscribe(" ", groupId, WeEvent.OFFSET_FIRST, subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * topic length > 64
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_topicOverMaxLen() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(
		    "qwertyuioplkjhgfdsazxcvbnmlkjhgfjshfljjdkdkfeffslkfsnkhkhhjjjjhggfsfsff", groupId,
		    WeEvent.OFFSET_FIRST, subId, "sdk", new IConsumer.ConsumerListener() {
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
     * groupId is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, null, this.eventId, subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * groupId is not a number
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdIsNotNum() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();

	    String result = this.iConsumer.subscribe(this.topicName, "sdfsf", this.eventId, subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * group id not exist
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_groupIdNotExist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, "4", this.eventId, subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * offset is WeEvent.OFFSET_FRIST
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsFrist() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
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
     * offset is WeEvent.OFFSET_FRIST
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsLast() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
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
	    String result = this.iConsumer.subscribe(this.topicName, groupId, "lsjflsjfljls", subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * offset eventId contain height large than block height
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetNumGtBlock() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId, "317e7c4c-75-32900000", subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * topic length > 64
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetOverMaxLen() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String result = this.iConsumer.subscribe(this.topicName, groupId,
		    "317e7c4c45gfjfs5369875452364875962-1213456789632145678564547896354775-329", subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * offset is null
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsNull() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();

	    String result = this.iConsumer.subscribe(this.topicName, groupId, null, subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * offset is blank " "
     * 
     * @throws InterruptedException
     */
    @Test
    public void testReSubscribe_offsetIsBlank() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();

	    String result = this.iConsumer.subscribe(this.topicName, groupId, "", subId, "sdk",
		    new IConsumer.ConsumerListener() {
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
		    new IConsumer.ConsumerListener() {
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
		    new IConsumer.ConsumerListener() {
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
		    new IConsumer.ConsumerListener() {
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

	Thread.sleep(wait3s);
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
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk",
		    new IConsumer.ConsumerListener() {
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
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk",
		    new IConsumer.ConsumerListener() {
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
		    new IConsumer.ConsumerListener() {
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
		    new IConsumer.ConsumerListener() {
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
		    new IConsumer.ConsumerListener() {
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
    public void testMulSubscribe_containMultipleTopic() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName, topic2, topic3 };
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId, "sdk",
		    new IConsumer.ConsumerListener() {
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
	    String result = this.iConsumer.subscribe(topics, null, this.eventId, "sdk",
		    new IConsumer.ConsumerListener() {
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
	    String result = this.iConsumer.subscribe(topics, "sdfsf", this.eventId, "sdk",
		    new IConsumer.ConsumerListener() {
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
	    String result = this.iConsumer.subscribe(topics, "4", this.eventId, "sdk",
		    new IConsumer.ConsumerListener() {
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
     * offset is WeEvent.OFFSET_FRIST
     * @throws InterruptedException
     */
    @Test
    public void testMulSubscribe_offsetIsLast() throws InterruptedException {
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
	    String result = this.iConsumer.subscribe(topics, groupId, "lsjflsjfljls", "sdk",
		    new IConsumer.ConsumerListener() {
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
    public void testMulSubscribe_offsetNumGtBlock() throws InterruptedException {
	log.info("===================={}", this.testName.getMethodName());

	try {
	    this.iConsumer.startConsumer();
	    String[] topics = { this.topicName };
	    String result = this.iConsumer.subscribe(topics, groupId, this.eventId + "000", "sdk",
		    new IConsumer.ConsumerListener() {
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
	    assertEquals(e.getCode(), ErrorCode.CONSUMER_LISTENER_IS_NULL.getCode());
	    log.error("subscribe(topics,groupId,offset,interfaceType,listener", e);
	}

	Thread.sleep(wait3s);
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
	    boolean result = this.iConsumer.unSubscribe(subId);
	    assertTrue(result);
	} catch (BrokerException e) {
	    log.error("unSubscribe(subId)", e);
	    assertNull(e);
	}

	Thread.sleep(wait3s);
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
	    assertEquals(e.getCode(), ErrorCode.SUBSCRIPTIONID_NOT_EXIST.getCode());
	}

	Thread.sleep(wait3s);
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
	    assertFalse(result);
	} catch (BrokerException e) {
	    log.error("unSubscribe(subId)", e);
	    assertNull(e);
	}

	Thread.sleep(wait3s);
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
	    assertFalse(result);
	} catch (BrokerException e) {
	    log.error("unSubscribe(subId)", e);
	    assertNull(e);
	}

	Thread.sleep(wait3s);
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
	    log.info("start consumer error: ",e);
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
