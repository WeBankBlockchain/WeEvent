package com.webank.weevent.broker.st;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import com.webank.weevent.broker.JUnitTestBase;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@Slf4j
public class StompTest extends JUnitTestBase {

    private final String topic = "com.weevent.test";
    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private StompHeaders header = new StompHeaders();
    private AtomicReference<BrokerException> failure;
    private final long wait3s = 3000;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        String brokerStomp = "ws://localhost:" + this.listenPort + "/weevent-broker/stomp";
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();

        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());

        // MappingJackson2MessageConverter
        stompClient.setMessageConverter(new StringMessageConverter());
        stompClient.setTaskScheduler(taskScheduler); // for heartbeats

        this.header.setDestination(topic);
        this.header.set("eventId", WeEvent.OFFSET_LAST);
        this.header.set("groupId", WeEvent.DEFAULT_GROUP_ID);

        this.failure = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        this.stompSession = this.stompClient.connect(brokerStomp, new MyStompSessionHandler(latch, this.failure)).get();
        latch.await();
        this.stompSession.setAutoReceipt(true);
    }

    @After
    public void after() {
        if (this.stompSession.isConnected()) {
            this.stompSession.disconnect();
        }
        this.stompClient.stop();
    }

    @Test
    public void testConnect() {
        Assert.assertTrue(this.stompSession.isConnected());
    }

    @Test
    public void testDisConnect() {
        this.stompSession.disconnect();
        Assert.assertFalse(this.stompSession.isConnected());
    }

    @Test
    public void testSubscribe() {
        StompSession.Subscription subscription = this.stompSession.subscribe(this.header, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("subscribe handleFrame, header: {} payload: {}", headers, payload);
            }
        });

        Assert.assertNotNull(subscription.getSubscriptionId());
    }

    @Test
    public void testSubscribeTopicNotExist() throws InterruptedException {
        this.header.setDestination("notExist");

        this.stompSession.subscribe(this.header, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("subscribe handleFrame, header: {} payload: {}", headers, payload);
            }
        });

        Thread.sleep(wait3s);
        Assert.assertEquals(ErrorCode.TOPIC_NOT_EXIST.getCode(), this.failure.get().getCode());
    }

    @Test
    public void testUnSubscribe() {
        StompSession.Subscription subscription = this.stompSession.subscribe(this.header, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("subscribe handleFrame, header: {} payload: {}", headers, payload);
            }
        });

        subscription.unsubscribe(this.header);
        Assert.assertTrue(true);
    }

    @Test
    public void testSubscribeGroupIdNotExist() throws InterruptedException {
        this.header.set("groupId", "100");
        this.stompSession.subscribe(this.header, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("subscribe handleFrame, header: {} payload: {}", headers, payload);
            }
        });

        Thread.sleep(wait3s);
        Assert.assertEquals(ErrorCode.WEB3SDK_UNKNOWN_GROUP.getCode(), this.failure.get().getCode());
    }

    @Test
    public void testSubscribeEventIdIllegal() throws InterruptedException {
        this.header.set("eventId", "abc");
        this.stompSession.subscribe(this.header, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("subscribe handleFrame, header: {} payload: {}", headers, payload);
            }
        });

        Thread.sleep(wait3s);
        Assert.assertEquals(ErrorCode.EVENT_ID_IS_ILLEGAL.getCode(), this.failure.get().getCode());
    }

    @Test
    public void testSubscribeIdIllegal() throws InterruptedException {
        this.header.set(WeEvent.WeEvent_SubscriptionId, "abc");
        this.stompSession.subscribe(this.header, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("subscribe handleFrame, header: {} payload: {}", headers, payload);
            }
        });

        Thread.sleep(wait3s);
        Assert.assertEquals(ErrorCode.SUBSCRIPTIONID_FORMAT_INVALID.getCode(), this.failure.get().getCode());
    }

    @Test
    public void testSend() throws InterruptedException {
        StompSession.Subscription subscription = this.stompSession.subscribe(this.header, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("subscribe handleFrame, header: {} payload: {}", headers, payload);
            }
        });

        Thread.sleep(wait3s);
        subscription.addReceiptTask(() -> {
            log.info("subscribe success");
        });

        StompSession.Receiptable receipt = this.stompSession.send(topic, "hello WeEvent from web socket");
        Thread.sleep(wait3s);
        receipt.addReceiptTask(() -> {
            log.info("publish event success.");
        });

        Assert.assertNotNull(receipt.getReceiptId());
    }


    private class MyStompSessionHandler extends StompSessionHandlerAdapter {

        private final AtomicReference<BrokerException> failure;
        private final CountDownLatch latch;

        public MyStompSessionHandler(final CountDownLatch latch, AtomicReference<BrokerException> failure) {
            this.latch = latch;
            this.failure = failure;
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            log.info("connection open, {}", session.getSessionId());
            this.latch.countDown();
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            this.failure.set(new BrokerException(Integer.parseInt(headers.get("code").get(0)), headers.get("message").get(0)));
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            log.info("connection exception, {} {}", session.getSessionId(), command);
            log.error("exception", exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable ex) {
            log.info("connection error, {}", session.getSessionId());
            log.error("exception", ex);
        }
    }

}