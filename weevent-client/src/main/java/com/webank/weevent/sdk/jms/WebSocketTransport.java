package com.webank.weevent.sdk.jms;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompDecoder;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.util.LinkedMultiValueMap;

/**
 * stomp transport over web socket.
 *
 * @author matthewliu
 * @author cristicmei
 * @since 2019/04/01
 */
@Slf4j
public class WebSocketTransport extends WebSocketClient {
    // binding JMS connection
    private WeEventTopicConnection topicConnection;

    // stomp invoke timeout
    private int timeout;

    // not only web socket, it's means stomp connection is ok
    private boolean connected;

    // atomic sequence on connection
    private AtomicLong sequence;

    // stomp command response (receipt <-> ResponseFuture)
    private Map<Long, ResponseFuture> futures;

    // (receiptId in stomp <-> subscriptionId in biz)
    private Map<String, String> subscriptionId2ReceiptId;

    // (receiptId <-> subscriptionId)
    private Map<String, String> receiptId2SubscriptionId;

    // (headerId in stomp <-> asyncSeq in biz )
    private Map<String, Long> sequence2Id;

    //(subscription <-> WeEvent topic)
    private Map<String, WeEventTopic> subscription2EventCache;

    private Pair<String, String> account;

    private boolean connectFlag = false;

    class ResponseFuture implements Future<Message> {
        private Long key;
        private CountDownLatch latch;

        private Message response;

        ResponseFuture(Long key) {
            this.key = key;
            this.latch = new CountDownLatch(1);

            futures.put(this.key, this);
        }

        public void setResponse(Message response) {
            this.latch.countDown();
            this.response = response;
            futures.remove(this.key);
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
        public Message get() throws InterruptedException, ExecutionException {
            latch.await();
            return this.response;
        }

        @Override
        public Message get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            if (latch.await(timeout, unit)) {
                if (this.response == null) {
                    throw new TimeoutException();
                } else {
                    return this.response;
                }
            } else {
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

    public void setTopicConnection(WeEventTopicConnection topicConnection) {
        this.topicConnection = topicConnection;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isConnected() {
        return connected;
    }

    // Stomp command
    public Message stompRequest(String req, Long asyncSeq) throws JMSException {
        log.info("stomp request, size: {}", req.length());

        try {
            // asyncSeq use for synchronous to asynchronous
            ResponseFuture response = new ResponseFuture(asyncSeq);
            this.send(req);
            return response.get(timeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("stomp command invoke failed", e);
            throw WeEventConnectionFactory.error2JMSException(ErrorCode.SDK_JMS_EXCEPTION_STOMP_EXECUTE);
        } catch (TimeoutException e) {
            log.error("stomp command invoke timeout", e);
            throw WeEventConnectionFactory.error2JMSException(ErrorCode.SDK_JMS_EXCEPTION_STOMP_TIMEOUT);
        }
    }


    public boolean stompConnect(String userName, String password) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String req = stompCommand.encodeConnect(userName, password);
        sequence2Id.put(Long.toString(0L), 0L);
        Message stompResponse = this.stompRequest(req, 0L);

        this.account = new Pair<>(userName, password);

        // initialize connection context
        this.cleanup();
        this.receiptId2SubscriptionId.clear();
        this.subscriptionId2ReceiptId.clear();
        return !stompCommand.isError(stompResponse);
    }

    public boolean stompDisconnect() throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String req = stompCommand.encodeDisConnect();
        sequence2Id.put(Long.toString(1L), 1L);
        Message stompResponse = this.stompRequest(req, 1L);
        return !stompCommand.isError(stompResponse);
    }

    // return eventId
    public String stompSend(WeEventTopic topic, BytesMessage bytesMessage) throws JMSException {
        Long asyncSeq = (this.sequence.incrementAndGet());
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        byte[] body = new byte[(int) bytesMessage.getBodyLength()];
        bytesMessage.readBytes(body);
        //header id equal asyncSeq
        String req = stompCommand.encodeSend(topic, body, asyncSeq);
        sequence2Id.put(Long.toString(asyncSeq), asyncSeq);
        Message stompResponse = this.stompRequest(req, asyncSeq);
        if (stompCommand.isError(stompResponse)) {
            log.info("stomp request is fail");
            return "";
        }
        return stompCommand.getReceipt(stompResponse);
    }

    // return subscriptionId
    public String stompSubscribe(WeEventTopic topic) throws JMSException {
        Long asyncSeq = this.sequence.incrementAndGet();
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String req = stompCommand.encodeSubscribe(topic, topic.getOffset(), asyncSeq);
        sequence2Id.put(Long.toString(asyncSeq), asyncSeq);
        Message stompResponse = this.stompRequest(req, asyncSeq);
        if (stompCommand.isError(stompResponse)) {
            log.info("stomp request is fail");
            return "";
        } else {
            // cache the subscribption id and the WeEventTopic,the subscription2EventCache which can use for reconnect
            topic.setContinueSubscriptionId(stompCommand.getSubscriptionId(stompResponse));
            this.subscription2EventCache.put(stompCommand.getSubscriptionId(stompResponse), topic);

            LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) stompResponse.getHeaders().get("nativeHeaders"));

            if (nativeHeaders != null) {
                // send command receipt Id
                Object subscriptionId = nativeHeaders.get("subscription-id");
                String subscriptionIdStr = null;
                if (subscriptionId != null) {
                    subscriptionIdStr = ((List) subscriptionId).get(0).toString();
                    // map the receipt id and the subscription id
                    this.receiptId2SubscriptionId.put(String.valueOf(asyncSeq), subscriptionIdStr);
                    this.subscriptionId2ReceiptId.put(subscriptionIdStr, String.valueOf(asyncSeq));
                }
            }

        }

        return stompCommand.getSubscriptionId(stompResponse);
    }

    /**
     * stompUnsubscribe stomp unsubscribe
     *
     * @param subscriptionId subscribetion id
     * @return true unsubcribe success,false unsubscribe fail
     * @throws JMSException error
     */
    public boolean stompUnsubscribe(String subscriptionId) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String headerId = this.subscriptionId2ReceiptId.get(subscriptionId);
        String req = stompCommand.encodeUnSubscribe(subscriptionId, headerId);
        Long asyncSeq = this.sequence.incrementAndGet();
        sequence2Id.put(headerId, asyncSeq);
        Message stompResponse = this.stompRequest(req, asyncSeq);
        return !stompCommand.isError(stompResponse);
    }

    // overwrite method from WebSocketClient
    public WebSocketTransport(URI server) {
        super(server, new Draft_6455());

        this.connected = false;
        this.sequence = new AtomicLong(0);
        this.futures = new ConcurrentHashMap<>();
        this.receiptId2SubscriptionId = new ConcurrentHashMap<>();
        this.subscriptionId2ReceiptId = new ConcurrentHashMap<>();
        this.sequence2Id = new ConcurrentHashMap<>();
        this.subscription2EventCache = new ConcurrentHashMap<>();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("WebSocket transport opened, remote address: {}", this.getRemoteSocketAddress().toString());
    }

    @Override
    public void onMessage(String message) {
        log.info("in onMessage");

        if (this.topicConnection == null) {
            log.info("topic Connection is null");
            return;
        }
        //decode from message
        StompDecoder decoder = new StompDecoder();
        List<Message<byte[]>> messages = decoder.decode(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
        for (Message<byte[]> stompMsg : messages) {
            // handle the frame from the server
            handleFrame(stompMsg);
        }
    }

    private void handleFrame(Message<byte[]> stompMsg) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(stompMsg);
        StompCommand command = accessor.getCommand();
        String cmd = command.name();

        log.debug("stomp command from server: {}", cmd);
        switch (cmd) {
            // connect response
            case "CONNECTED":
                // connect command always 0
                futures.get(0L).setResponse(stompMsg);
                break;

            // disconnect/send/subscribe/unsubscribe response
            case "RECEIPT":
                handleReceiptFrame(stompMsg);
                break;

            case "ERROR":
                try {
                    handleErrorFrame(stompMsg);
                } catch (JMSException e) {
                    log.error(e.toString());
                }
                break;

            case "MESSAGE":
                handleMessageFrame(stompMsg);
                break;

            case "HEARTBEAT":
                break;

            default:
                log.error("unknown command from stomp server: {}", cmd);
        }
    }

    /**
     * handle the receipt frame from the server
     *
     * @param stompMsg handle the receipt frame
     */
    private void handleReceiptFrame(Message<byte[]> stompMsg) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(stompMsg);
        log.info("receipt frame: {}", accessor.toString());

        String receiptId = getHeadersValue(accessor, "receipt-id");
        String subscriptionId = getHeadersValue(accessor, "subscription-id");
        // add the map<receiptId2SubscriptionId>
        if (futures.containsKey(sequence2Id.get(receiptId))) {
            log.info("subscriptionId {}", subscriptionId);
            futures.get(sequence2Id.get(receiptId)).setResponse(stompMsg);
        } else {
            log.error("unknown receipt-id: {}", receiptId);
        }
    }


    /**
     * handle the message frame from the server
     *
     * @param stompMsg handle the message frame
     */
    private void handleMessageFrame(Message<byte[]> stompMsg) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(stompMsg);
        log.info("accessor:{}", accessor.toString());

        String messageId = getHeadersValue(accessor, "message-id");
        String subscriptionId = getHeadersValue(accessor, "subscription-id");
        WeEvent event = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            event = mapper.readValue(stompMsg.getPayload(), WeEvent.class);
        } catch (IOException e) {
            log.error("jackson decode WeEvent failed", e);
        }

        // update the cache eventid
        if (this.subscription2EventCache.containsKey(subscriptionId)) {
            this.subscription2EventCache.get(subscriptionId).setOffset(event.getEventId());
        }

        if (this.receiptId2SubscriptionId.containsKey(messageId)) {
            WeEventStompCommand weEventStompCommand = new WeEventStompCommand(event);
            weEventStompCommand.setSubscriptionId(subscriptionId);
            weEventStompCommand.setHeaderId(messageId);

            // dispatch event message
            this.topicConnection.dispatch(weEventStompCommand);
        }

    }

    /**
     * handle the error frame from the server
     *
     * @param stompMsg handle error frame
     */
    private void handleErrorFrame(Message<byte[]> stompMsg) throws JMSException {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(stompMsg);
        log.info("accessor: {}", accessor.toString());

        String code = getHeadersValue(accessor, "code");
        String receiptId;

        if (code != null) {
            receiptId = getHeadersValue(accessor, "message-id");
            log.info("receiptId:{}", receiptId);
            if (receiptId != null) {
                futures.get(sequence2Id.get(receiptId)).setResponse(stompMsg);
            }
        } else {
            log.info("connnect error");
        }
        throw new JMSException("message:" + accessor.getNativeHeader("message").get(0) + "code:" + code);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket transport closed, code: {} reason: {} remote: {}", code, reason, remote);
        this.connected = false;
        this.cleanup();
        // reconnect if connection lost
        if (remote) {
            if (!this.connectFlag) {
                WSThread wSThread = new WSThread(this);
                wSThread.start();
            }
        }

    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket transport error", ex);
    }

    /**
     * use for get the headers
     *
     * @param accessor StompHeaderAccessor
     * @param headerskey headers name
     * @return the headers value
     */
    private String getHeadersValue(StompHeaderAccessor accessor, String headerskey) {
        String id = null;
        Object idObject = accessor.getNativeHeader(headerskey);
        if (idObject != null) {
            id = ((List) idObject).get(0).toString();
        }
        return id;
    }

    class WSThread extends Thread {
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
                while (!this.webSocketTransport.stompConnect(this.webSocketTransport.account.getKey(),
                        this.webSocketTransport.account.getValue())) {
                    Thread.sleep(3000);
                }
            } catch (InterruptedException | JMSException e) {
                log.error("auto reconnect failed", e);
            }

            for (Map.Entry<String, WeEventTopic> subscription : this.webSocketTransport.subscription2EventCache.entrySet()) {
                try {
                    log.info("subscription cache:{}", subscription.toString());
                    this.webSocketTransport.stompSubscribe(subscription.getValue());
                } catch (JMSException e) {
                    log.error("auto resubscribe failed", e);
                }
            }

            this.webSocketTransport.connectFlag = false;

            log.info("auto redo thread exit");
        }
    }
}

