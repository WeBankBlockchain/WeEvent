package com.webank.weevent.sdk.jms;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

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
    private boolean connected = false;

    // atomic sequence on connection
    private AtomicLong sequence = new AtomicLong(0);

    // stomp command response (receipt <-> ResponseFuture)
    private Map<Long, ResponseFuture> futures = new ConcurrentHashMap<>();

    // (receiptId in stomp <-> subscriptionId in biz)
    private Map<String, String> subscriptionId2ReceiptId = new ConcurrentHashMap<>();

    // (receiptId <-> subscriptionId)
    private Map<String, String> receiptId2SubscriptionId = new ConcurrentHashMap<>();

    // (headerId in stomp <-> asyncSeq in biz )
    private Map<String, Long> sequence2Id = new ConcurrentHashMap<>();

    //(subscription <-> WeEvent topic)
    private Map<String, WeEventTopic> subscription2EventCache = new ConcurrentHashMap<>();

    private Pair<String, String> account;

    // is reconnect thread already exist
    private boolean connectFlag = false;

    class ResponseFuture implements Future<Message> {
        private CountDownLatch latch = new CountDownLatch(1);

        private Long key;
        private Message response;

        ResponseFuture(Long key) {
            this.key = key;
            futures.put(this.key, this);
        }

        public void setResponse(Message response) {
            this.response = response;
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
        public Message get() throws InterruptedException {
            latch.await();
            return this.response;
        }

        @Override
        public Message get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            if (latch.await(timeout, unit)) {
                if (this.response != null) {
                    return this.response;
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

    public void setTopicConnection(WeEventTopicConnection topicConnection) {
        this.topicConnection = topicConnection;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isConnected() {
        return this.connected;
    }

    // Stomp command
    public Message stompRequest(String req, Long asyncSeq) throws JMSException {
        log.info("stomp request, seq: {} size: {}", asyncSeq, req.length());

        try {
            // asyncSeq use for synchronous to asynchronous
            ResponseFuture response = new ResponseFuture(asyncSeq);

            // .send(String text) is Text Message, .send(byte[] data) is Binary Message
            log.debug("STOMP post text: {}", req);
            this.send(req);
            return response.get(this.timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("stomp command invoke Interrupted, seq: " + asyncSeq, e);
            throw WeEventConnectionFactory.error2JMSException(ErrorCode.SDK_JMS_EXCEPTION_STOMP_EXECUTE);
        } catch (TimeoutException e) {
            log.error("stomp command invoke timeout, seq: " + asyncSeq, e);
            throw WeEventConnectionFactory.error2JMSException(ErrorCode.SDK_JMS_EXCEPTION_STOMP_TIMEOUT);
        }
    }


    public boolean stompConnect(String userName, String password) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String req = stompCommand.encodeConnect(userName, password);

        this.sequence2Id.put(Long.toString(0L), 0L);
        Message stompResponse = this.stompRequest(req, 0L);
        this.account = Pair.of(userName, password);

        // initialize connection context
        this.cleanup();
        this.receiptId2SubscriptionId.clear();
        this.subscriptionId2ReceiptId.clear();
        this.connected = !stompCommand.isError(stompResponse);
        return connected;
    }

    public boolean stompDisconnect() throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        Long asyncSeq = this.sequence.incrementAndGet();
        String req = stompCommand.encodeDisConnect(asyncSeq);
        this.sequence2Id.put(Long.toString(asyncSeq), asyncSeq);

        Message stompResponse = this.stompRequest(req, asyncSeq);
        boolean flag = stompCommand.isError(stompResponse);
        if (flag) {
            this.connected = false;
        }
        return flag;
    }

    // return receipt-id
    public String stompSend(WeEventTopic topic, BytesMessage bytesMessage) throws JMSException {
        if (bytesMessage instanceof WeEventBytesMessage) {
            WeEventBytesMessage message = (WeEventBytesMessage) bytesMessage;

            // read byte
            byte[] body = new byte[(int) message.getBodyLength()];
            message.readBytes(body);
            WeEvent weEvent = new WeEvent(topic.getTopicName(), body, message.getExtensions());

            // header id equal asyncSeq
            WeEventStompCommand stompCommand = new WeEventStompCommand();
            Long asyncSeq = (this.sequence.incrementAndGet());
            String req = stompCommand.encodeSend(asyncSeq, topic, weEvent);
            this.sequence2Id.put(Long.toString(asyncSeq), asyncSeq);

            Message stompResponse = this.stompRequest(req, asyncSeq);
            if (stompCommand.isError(stompResponse)) {
                log.info("STOMP ERROR received: {}", stompResponse.toString());

                if (stompResponse.getHeaders().containsKey("message")) {
                    throw new JMSException(stompResponse.getHeaders().get("message").toString());
                } else {
                    throw WeEventConnectionFactory.error2JMSException(ErrorCode.SDK_JMS_EXCEPTION_STOMP_EXECUTE);
                }
            }

            // handler stompResponse
            LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) stompResponse.getHeaders().get("nativeHeaders"));
            if (nativeHeaders == null) {
                log.error("unknown native header");
                throw WeEventConnectionFactory.error2JMSException(ErrorCode.SDK_JMS_EXCEPTION_STOMP_EXECUTE);
            }

            String eventID = nativeHeaders.get("eventId") == null ? "" : nativeHeaders.get("eventId").get(0).toString();
            bytesMessage.setJMSMessageID(eventID);
            return nativeHeaders.get("receipt-id") == null ? "" : nativeHeaders.get("receipt-id").get(0).toString();
        }

        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    // return subscriptionId
    public String stompSubscribe(WeEventTopic topic) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        Long asyncSeq = this.sequence.incrementAndGet();
        String req = stompCommand.encodeSubscribe(topic, topic.getOffset(), asyncSeq);
        this.sequence2Id.put(Long.toString(asyncSeq), asyncSeq);

        Message stompResponse = this.stompRequest(req, asyncSeq);
        if (stompCommand.isError(stompResponse)) {
            log.info("stomp request is fail");
            return "";
        } else {
            // cache the subscription id and the WeEventTopic,the subscription2EventCache which can use for reconnect
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
     * stompUnsubscribe stomp unSubscribe
     *
     * @param subscriptionId subscription id
     * @return true if success
     * @throws JMSException error
     */
    public boolean stompUnsubscribe(String subscriptionId) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String headerId = this.subscriptionId2ReceiptId.get(subscriptionId);
        String req = stompCommand.encodeUnSubscribe(subscriptionId, headerId);
        Long asyncSeq = this.sequence.incrementAndGet();
        this.sequence2Id.put(headerId, asyncSeq);

        Message stompResponse = this.stompRequest(req, asyncSeq);
        return !stompCommand.isError(stompResponse);
    }

    private void handleFrame(Message<byte[]> stompMsg) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(stompMsg);
        StompCommand command = accessor.getCommand();
        log.debug("STOMP command received: {}", command);

        switch (command.name()) {
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
                log.error("unknown STOMP command: {}", command);
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
        // add the map<receiptId2SubscriptionId>
        if (this.futures.containsKey(this.sequence2Id.get(receiptId))) {
            this.futures.get(this.sequence2Id.get(receiptId)).setResponse(stompMsg);
        } else {
            log.error("unknown receipt-id: {}", receiptId);
        }
    }

    /**
     * handle the message frame from the server
     *
     * @param stompMsg handle the message frame
     */
    @SuppressWarnings("unchecked")
    private void handleMessageFrame(Message<byte[]> stompMsg) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(stompMsg);
        log.info("accessor: {}", accessor.toString());

        String messageId = getHeadersValue(accessor, "message-id");
        String subscriptionId = getHeadersValue(accessor, "subscription-id");

        // custom properties, eventId is in native header
        Map<String, String> extensions = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : ((Map<String, List<String>>) stompMsg.getHeaders().get(NativeMessageHeaderAccessor.NATIVE_HEADERS)).entrySet()) {
            if (entry.getKey().startsWith("weevent-")) {
                extensions.put(entry.getKey(), entry.getValue().get(0));
            }
        }

        WeEvent event = new WeEvent(accessor.getDestination(), stompMsg.getPayload(), extensions);
        event.setEventId(accessor.getNativeHeader("eventId").get(0));

        log.info("received: {}", event);

        // update the cache eventId
        if (this.subscription2EventCache.containsKey(subscriptionId)) {
            this.subscription2EventCache.get(subscriptionId).setOffset(event.getEventId());
        }

        // dispatch to listener
        if (this.receiptId2SubscriptionId.containsKey(messageId)) {
            WeEventStompCommand weEventStompCommand = new WeEventStompCommand(event);
            weEventStompCommand.setSubscriptionId(subscriptionId);
            weEventStompCommand.setHeaderId(messageId);
            weEventStompCommand.setTopic(new WeEventTopic(event.getTopic()));

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
                this.futures.get(this.sequence2Id.get(receiptId)).setResponse(stompMsg);
            }
        } else {
            log.info("connect error");
        }
        throw new JMSException("message:" + accessor.getNativeHeader("message").get(0) + "code:" + code);
    }

    /**
     * use for get the headers
     *
     * @param accessor StompHeaderAccessor
     * @param key headers name
     * @return the headers value
     */
    private String getHeadersValue(StompHeaderAccessor accessor, String key) {
        Object idObject = accessor.getNativeHeader(key);
        if (idObject != null) {
            return ((List) idObject).get(0).toString();
        }

        return null;
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
                while (!this.webSocketTransport.stompConnect(this.webSocketTransport.account.getFirst(),
                        this.webSocketTransport.account.getSecond())) {
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
}

