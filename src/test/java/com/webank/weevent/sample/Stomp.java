package com.webank.weevent.sample;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.ConnectionLostException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * STOMP client, see
 * https://docs.spring.io/spring/docs/5.0.8.RELEASE/spring-framework-reference/web.html#websocket-stomp
 * https://docs.spring.io/spring/docs/5.0.8.RELEASE/spring-framework-reference/web.html#websocket-stomp-client
 *
 * @author matthewliu
 * @version 1.0
 * @since 2019/02/14
 */
@Slf4j
public class Stomp {
    public static void main(String[] args) {
        System.out.println("This is WeEvent stomp sample.");

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();

        stompOverWebSocket(taskScheduler);

        stompOverSockjs(taskScheduler);
    }

    private static StompSessionHandlerAdapter getStompSessionHandlerAdapter() {
        return new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                log.info("connection open, {}", session.getSessionId());
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.info("connection exception, {} {}", session.getSessionId(), command);
                log.error("exception, {}", exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                if (exception instanceof ConnectionLostException) {
                    log.info("connection closed, {}", session.getSessionId());
                    // can do auto reconnect in this handle
                } else {
                    log.info("connection error, {}", session.getSessionId());
                    log.error("exception, {}", exception);
                }
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("session handleFrame, header: {} payload: {}", headers, payload);
            }
        };
    }

    private static void sendAndsubscribe(StompSession stompSession) throws InterruptedException {
        stompSession.setAutoReceipt(true);

        String topic = "com.webank.test";
        log.info("subscribe topic, {}", topic);
        StompSession.Subscription subscription = stompSession.subscribe(topic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                log.info("subscribe handleFrame, header: {} payload: {}", headers, payload);

                assertTrue(true);
            }
        });
        log.info("subscribe result, subscription id: {}", subscription.getSubscriptionId());

        log.info("send topic, {}", topic);
        StompSession.Receiptable receiptable = stompSession.send(topic, "hello world, from web socket");
        log.info("send result, receipt id: {}", receiptable.getReceiptId());

        Thread.sleep(3 * 1000L);
        log.info("unsubscribe, {}", subscription.getSubscriptionId());
        subscription.unsubscribe();

        // webEnvironment is startup by junit client
        Thread.sleep(20 * 1000L);
    }

    private static void stompOverWebSocket(ThreadPoolTaskScheduler taskScheduler) {
        // standard web socket transport
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);

        // no difference in the following
        // MappingJackson2MessageConverter
        stompClient.setMessageConverter(new StringMessageConverter());
        stompClient.setTaskScheduler(taskScheduler); // for heartbeats
        ListenableFuture<StompSession> f = stompClient.connect("ws://localhost:8080/weevent/stomp", getStompSessionHandlerAdapter());

        try {
            StompSession stompSession = f.get();

            sendAndsubscribe(stompSession);

            assertTrue(true);
        } catch (InterruptedException | ExecutionException e) {
            log.error("web socket task failed", e);
            fail();
        }
    }

    private static void stompOverSockjs(ThreadPoolTaskScheduler taskScheduler) {

        // sock js transport
        List<Transport> transports = new ArrayList<>(2);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        transports.add(new RestTemplateXhrTransport());

        SockJsClient sockjsClient = new SockJsClient(transports);
        WebSocketStompClient stompClient = new WebSocketStompClient(sockjsClient);

        // no difference in the following
        // StringMessageConverter
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        stompClient.setTaskScheduler(taskScheduler); // for heartbeats
        ListenableFuture<StompSession> f = stompClient.connect("http://localhost:8080/weevent/sockjs", getStompSessionHandlerAdapter());

        try {
            StompSession stompSession = f.get();

            sendAndsubscribe(stompSession);

            assertTrue(true);
        } catch (InterruptedException | ExecutionException e) {
            log.error("sockjs task failed", e);
            fail();
        }
    }
}
