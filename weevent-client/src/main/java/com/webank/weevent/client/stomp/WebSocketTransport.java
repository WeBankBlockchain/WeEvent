package com.webank.weevent.client.stomp;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.IWeEventClient;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.data.util.Pair;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompDecoder;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;


/**
 * stomp transport over web socket.
 *
 * @author matthewliu
 * @author cristicmei
 * @since 2019/04/01
 */
@Slf4j
public class WebSocketTransport extends WebSocketClient {
    // stomp invoke timeout
    private int timeout;

    // not only web socket, it's means stomp connection is ok
    private boolean connected = false;

    // atomic sequence on connection
    private final AtomicLong sequence = new AtomicLong(0);

    // stomp command response (receipt <-> ResponseFuture)
    private final Map<Long, ResponseFuture> futures = new ConcurrentHashMap<>();

    // (receiptId in stomp <-> subscriptionId in biz)
    private final Map<String, String> subscriptionId2ReceiptId = new ConcurrentHashMap<>();

    // (receiptId <-> subscriptionId)
    private final Map<String, String> receiptId2SubscriptionId = new ConcurrentHashMap<>();

    // (headerId in stomp <-> asyncSeq in biz )
    private final Map<String, Long> sequence2Id = new ConcurrentHashMap<>();

    //(subscription <-> <WeEvent topic, IWeEventClient.EventListener>)
    private final Map<String, Pair<TopicContent, IWeEventClient.EventListener>> subscription2EventCache = new ConcurrentHashMap<>();

    private Pair<String, String> account;

    // is reconnect thread already exist
    private boolean connectFlag = false;

    class ResponseFuture implements Future<StompHeaderAccessor> {
        private final CountDownLatch latch = new CountDownLatch(1);

        private final Long key;
        private StompHeaderAccessor stompHeaderAccessor;

        ResponseFuture(Long key) {
            this.key = key;
            futures.put(this.key, this);
        }

        public void setResponse(Message<?> response) {
            this.stompHeaderAccessor = StompHeaderAccessor.wrap(response);
            futures.remove(this.key);

            this.latch.countDown();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            // not support
            return false;
        }

        @Override
        public boolean isCancelled() {
            // not support
            return false;
        }

        @Override
        public boolean isDone() {
            return this.latch.getCount() == 0;
        }

        @Override
        public StompHeaderAccessor get() throws InterruptedException {
            latch.await();

            return this.stompHeaderAccessor;
        }

        @Override
        public StompHeaderAccessor get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            if (latch.await(timeout, unit)) {
                if (this.stompHeaderAccessor != null) {
                    return this.stompHeaderAccessor;
                } else {
                    log.error("empty response");
                    throw new TimeoutException();
                }
            } else {
                log.error("empty response");
                throw new TimeoutException();
            }
        }
    }

    private void cleanup() {
        // initialize connection context
        this.sequence.set(0L);
        this.futures.clear();
        this.sequence2Id.clear();
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isConnected() {
        return this.connected;
    }

    // Stomp command
    private ResponseFuture stompRequestAsync(String req, Long asyncSeq) {
        log.info("stomp request, seq: {} size: {}", asyncSeq, req.length());

        // asyncSeq use for synchronous to asynchronous
        ResponseFuture response = new ResponseFuture(asyncSeq);
        // .send(String text) is Text Message, .send(byte[] data) is Binary Message
        log.debug("STOMP post text: {}", req);
        this.send(req);
        return response;
    }

    public void stompConnect(String userName, String password) throws BrokerException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String req = stompCommand.encodeConnect(userName, password);

        this.sequence2Id.put(Long.toString(0L), 0L);
        getStompHeaderAccessor(stompCommand, req, 0L, "stompConnect");
        log.info("stomp connect success.");

        this.account = Pair.of(userName, password);

        // initialize connection context
        this.cleanup();
        this.receiptId2SubscriptionId.clear();
        this.subscriptionId2ReceiptId.clear();
        this.connected = true;
    }

    public void stompDisconnect() throws BrokerException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        Long asyncSeq = this.sequence.incrementAndGet();
        String req = stompCommand.encodeDisConnect(asyncSeq);
        this.sequence2Id.put(Long.toString(asyncSeq), asyncSeq);

        getStompHeaderAccessor(stompCommand, req, asyncSeq, "stompDisconnect");
        log.info("stomp disconnect success.");
    }

    // return eventId
    public SendResult stompSend(TopicContent topic, WeEvent event) {
        // header id equal asyncSeq
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        Long asyncSeq = (this.sequence.incrementAndGet());
        String req = stompCommand.encodeSend(asyncSeq, topic, event);
        this.sequence2Id.put(Long.toString(asyncSeq), asyncSeq);

        SendResult sendResult = new SendResult();
        sendResult.setTopic(event.getTopic());
        try {
            StompHeaderAccessor stompHeaderAccessor = getStompHeaderAccessor(stompCommand, req, asyncSeq, "publish");
            sendResult.setEventId(stompHeaderAccessor.getFirstNativeHeader("eventId"));
            sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
            log.info("publish success, event: {}, eventID: {}", event, sendResult.getEventId());
        } catch (BrokerException e) {
            if (ErrorCode.SDK_EXCEPTION_STOMP_TIMEOUT.getCode() == e.getCode()) {
                sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
            } else {
                sendResult.setStatus(SendResult.SendResultStatus.ERROR);
            }
        }
        return sendResult;
    }

    // return CompletableFuture
    public CompletableFuture<SendResult> stompSendAsync(TopicContent topic, WeEvent event) {
        // header id equal asyncSeq
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        Long asyncSeq = (this.sequence.incrementAndGet());
        String req = stompCommand.encodeSend(asyncSeq, topic, event);
        this.sequence2Id.put(Long.toString(asyncSeq), asyncSeq);

        return CompletableFuture.supplyAsync(() -> {
            SendResult sendResult = new SendResult();
            sendResult.setTopic(event.getTopic());
            try {
                StompHeaderAccessor stompHeaderAccessor = this.stompRequestAsync(req, asyncSeq).get();

                stompCommand.checkError(stompHeaderAccessor);
                sendResult.setStatus(SendResult.SendResultStatus.SUCCESS);
                sendResult.setEventId(stompHeaderAccessor.getFirstNativeHeader("eventId"));
                log.info("publish async success, event: {}, eventID: {}", event, sendResult.getEventId());
            } catch (BrokerException | InterruptedException e) {
                log.error("stomp command invoke error, seq: " + asyncSeq, e);
                sendResult.setStatus(SendResult.SendResultStatus.ERROR);
            }
            return sendResult;
        });
    }

    // return subscriptionId
    public String stompSubscribe(TopicContent topic, IWeEventClient.EventListener listener) throws BrokerException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        Long asyncSeq = this.sequence.incrementAndGet();
        String req = stompCommand.encodeSubscribe(topic, asyncSeq);
        this.sequence2Id.put(Long.toString(asyncSeq), asyncSeq);

        StompHeaderAccessor stompHeaderAccessor = getStompHeaderAccessor(stompCommand, req, asyncSeq, "subscribe");

        // cache the subscription id and the WeEventTopic,the subscription2EventCache which can use for reconnect
        String subscriptionId = stompHeaderAccessor.getFirstNativeHeader("subscription-id");
        topic.getExtension().put(WeEvent.WeEvent_SubscriptionId, subscriptionId);

        this.subscription2EventCache.put(subscriptionId, Pair.of(topic, listener));
        // map the receipt id and the subscription id
        this.receiptId2SubscriptionId.put(String.valueOf(asyncSeq), subscriptionId);
        this.subscriptionId2ReceiptId.put(subscriptionId, String.valueOf(asyncSeq));
        log.info("subscribe success, topic: {}", topic.getTopicName());

        return subscriptionId;

    }

    public void stompUnSubscribe(String subscriptionId) throws BrokerException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String headerId = this.subscriptionId2ReceiptId.get(subscriptionId);
        String req = stompCommand.encodeUnSubscribe(subscriptionId, headerId);
        Long asyncSeq = this.sequence.incrementAndGet();
        this.sequence2Id.put(headerId, asyncSeq);

        getStompHeaderAccessor(stompCommand, req, asyncSeq, "unsubscribe");
        log.info("unsubscribe success, subscriptionId: {}", subscriptionId);
    }

    private StompHeaderAccessor getStompHeaderAccessor(WeEventStompCommand stompCommand, String req, Long asyncSeq, String commandStr) throws BrokerException {
        StompHeaderAccessor stompHeaderAccessor;
        try {
            stompHeaderAccessor = this.stompRequestAsync(req, asyncSeq).get(this.timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("{} over stomp execute Interrupted", commandStr, e);
            Thread.currentThread().interrupt();
            throw new BrokerException(ErrorCode.SDK_EXCEPTION_STOMP_EXECUTE);
        } catch (TimeoutException e) {
            log.error("{} over stomp timeout", commandStr, e);
            throw new BrokerException(ErrorCode.SDK_EXCEPTION_STOMP_TIMEOUT);
        }
        stompCommand.checkError(stompHeaderAccessor);
        return stompHeaderAccessor;
    }

    private void handleFrame(Message<byte[]> stompMsg) {
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(stompMsg);
        if (stompHeaderAccessor.isHeartbeat()) {
            return;
        }

        StompCommand command = stompHeaderAccessor.getCommand();
        if (command == null) {
            log.error("miss command in stomp header");
            return;
        }

        log.info("STOMP command received: {}", stompHeaderAccessor.toString());
        switch (command) {
            // connect response
            case CONNECTED:
                // connect command always 0
                futures.get(0L).setResponse(stompMsg);
                break;

            // disconnect/send/subscribe/unsubscribe response
            case RECEIPT:
                handleReceiptFrame(stompHeaderAccessor, stompMsg);
                break;

            case ERROR:
                handleErrorFrame(stompHeaderAccessor, stompMsg);
                break;

            case MESSAGE:
                handleMessageFrame(stompHeaderAccessor, stompMsg);
                break;

            default:
                log.error("unknown STOMP command: {}", command);
                break;
        }
    }

    private void handleReceiptFrame(StompHeaderAccessor stompHeaderAccessor, Message<byte[]> stompMsg) {
        String receiptId = stompHeaderAccessor.getReceiptId();
        // add the map<receiptId2SubscriptionId>
        if (this.futures.containsKey(this.sequence2Id.get(receiptId))) {
            this.futures.get(this.sequence2Id.get(receiptId)).setResponse(stompMsg);
        } else {
            log.error("unknown receipt-id: {}", receiptId);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleMessageFrame(StompHeaderAccessor stompHeaderAccessor, Message<byte[]> stompMsg) {
        String subscriptionId = stompHeaderAccessor.getFirstNativeHeader("subscription-id");

        // custom properties, eventId is in native header
        Map<String, String> extensions = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : ((Map<String, List<String>>) stompMsg.getHeaders().get(NativeMessageHeaderAccessor.NATIVE_HEADERS)).entrySet()) {
            if (entry.getKey().startsWith("weevent-")) {
                extensions.put(entry.getKey(), entry.getValue().get(0));
            }
        }

        WeEvent event = new WeEvent(stompHeaderAccessor.getDestination(), stompMsg.getPayload(), extensions);
        event.setEventId(stompHeaderAccessor.getFirstNativeHeader("eventId"));
        log.info("received: {}", event);

        if (this.subscription2EventCache.containsKey(subscriptionId)) {
            IWeEventClient.EventListener listener = this.subscription2EventCache.get(subscriptionId).getSecond();
            listener.onEvent(event);
            // update the cache eventId
            this.subscription2EventCache.get(subscriptionId).getFirst().setOffset(event.getEventId());
        }
    }

    private void handleErrorFrame(StompHeaderAccessor stompHeaderAccessor, Message<byte[]> stompMsg) {
        String receiptId = stompHeaderAccessor.getReceiptId();
        log.info("stomp ERROR, receipt-id: {}", receiptId);
        if (receiptId == null) {
            return;
        }

        this.futures.get(this.sequence2Id.get(receiptId)).setResponse(stompMsg);
    }

    static class WSThread extends Thread {
        private WebSocketTransport webSocketTransport;

        public WSThread(WebSocketTransport webSocketTransport) {
            this.webSocketTransport = webSocketTransport;
        }

        public void run() {
            log.info("auto redo thread enter");

            this.webSocketTransport.connectFlag = true;
            try {
                // check the websocket
                while (!this.webSocketTransport.reconnectBlocking()) {
                    Thread.sleep(3000);
                }
                // check the stomp connect,and use cache login and password
                while (true) {
                    try {
                        this.webSocketTransport.stompConnect(this.webSocketTransport.account.getFirst(),
                                this.webSocketTransport.account.getSecond());
                        break;
                    } catch (BrokerException e) {
                        Thread.sleep(3000);
                    }
                }
            } catch (InterruptedException e) {
                log.error("InterruptedException while auto reconnect");
                Thread.currentThread().interrupt();
            }

            for (Map.Entry<String, Pair<TopicContent, IWeEventClient.EventListener>> subscription : this.webSocketTransport.subscription2EventCache.entrySet()) {
                try {
                    log.info("subscription cache:{}", subscription.toString());
                    this.webSocketTransport.stompSubscribe(subscription.getValue().getFirst(), subscription.getValue().getSecond());
                } catch (BrokerException e) {
                    log.error("auto resubscribe failed", e);
                }
            }

            this.webSocketTransport.connectFlag = false;

            log.info("auto redo thread exit");
        }
    }

    // The following are methods from super class WebSocketClient

    // overwrite method from WebSocketClient
    public WebSocketTransport(URI server) {
        super(server, new Draft_6455());
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.info("WebSocket transport opened, remote address: {}", this.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(String message) {
        log.debug("received message");

        //decode from message
        StompDecoder decoder = new StompDecoder();
        List<Message<byte[]>> messages = decoder.decode(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
        for (Message<byte[]> stompMsg : messages) {
            // handle the frame from the server
            handleFrame(stompMsg);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket transport closed, code: {} reason: {} remote: {}", code, reason, remote);
        this.connected = false;
        this.cleanup();
        // reconnect if connection lost
        if (remote && !this.connectFlag) {
            WSThread wSThread = new WSThread(this);
            wSThread.start();
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket transport error", ex);
    }
}

