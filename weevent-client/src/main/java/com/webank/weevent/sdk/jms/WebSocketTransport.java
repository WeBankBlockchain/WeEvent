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
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompDecoder;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

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

    // (receiptId,subcriptionId)
    private Map<String, String> receiptId2SubscriptionId;

    // (headerId in stomp <-> asyncSeq in biz )
    private Map<String, Long> sequence2Id;

    //(topic <-> eventId)
    public Map<String, String> subscriptionCache;

    public Map<WeEventTopicSubscriber, String> Subscriber;

   // private WSThread wSThread;

    public Boolean connectFlag = FALSE;

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


    public void stompConnect(String userName, String password) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String req = stompCommand.encodeConnect(userName, password);
        sequence2Id.put(Long.toString(0L), 0L);
        this.stompRequest(req, 0L);

        // initialize connection context
        this.cleanup();
        this.receiptId2SubscriptionId.clear();
        this.subscriptionId2ReceiptId.clear();
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
        }

        return stompCommand.getSubscriptionId(stompResponse);
    }


    public boolean stompUnsubscribe(String subscriptionId) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        Long headerId = Long.valueOf(this.subscriptionId2ReceiptId.get(subscriptionId));
        String req = stompCommand.encodeUnSubscribe(subscriptionId, Long.toString(headerId));

        Long asyncSeq = this.sequence.incrementAndGet();
        sequence2Id.put(Long.toString(headerId), asyncSeq);
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
        this.subscriptionCache = new ConcurrentHashMap<>();
        this.Subscriber = new ConcurrentHashMap<>();
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
        StompHeaderAccessor accessor;
        for (Message<byte[]> stompMsg : messages) {
            accessor = StompHeaderAccessor.wrap(stompMsg);
            StompCommand command = accessor.getCommand();
            if (command == null) {
                continue;
            }

            String cmd = command.name();
            String receiptId = null;
            if (cmd.equals("RECEIPT")) {
                if (accessor.getReceiptId() != null) {
                    receiptId = accessor.getReceiptId();
                }
            }

            String subscriptionId = null;
            String messageId = null;
            if (accessor.getNativeHeader("subscription-id") != null) {
                subscriptionId = accessor.getNativeHeader("subscription-id").get(0);
            }
            if (accessor.getNativeHeader("message-id") != null) {
                messageId = accessor.getNativeHeader("message-id").get(0);
            }
            log.debug("stomp command from server: {}", cmd);
            switch (cmd) {
                // connect response
                case "CONNECTED":
                    // connect command always 0

                    futures.get(0L).setResponse(stompMsg);
                    break;

                // disconnect/send/subscribe/unsubscribe response
                case "RECEIPT":
                    // add the map<receiptId2SubscriptionId>
                    if (futures.containsKey(sequence2Id.get(receiptId))) {
                        if (subscriptionId != null) {
                            log.info("subscriptionId {}", subscriptionId);
                            // receiptId2SubscriptionId length subscriptionId2ReceiptId length
                            this.receiptId2SubscriptionId.put(receiptId, subscriptionId);
                            this.subscriptionId2ReceiptId.put(subscriptionId, receiptId);
                        }
                        futures.get(sequence2Id.get(receiptId)).setResponse(stompMsg);
                    } else {
                        log.error("unknown receipt-id: {}", receiptId);
                    }
                    break;

                case "ERROR":
                    log.error("command from stomp server: {}", cmd);
                    String recepitId = "";
                    if (accessor.getNativeHeader("receipt-id") != null) {
                        recepitId = accessor.getNativeHeader("receipt-id").get(0);
                    }
                    futures.get(sequence2Id.get(receiptId)).setResponse(stompMsg);
                    break;
                case "MESSAGE":
                    WeEvent event = null;
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        event = mapper.readValue(stompMsg.getPayload(), WeEvent.class);
                    } catch (IOException e) {
                        log.error("jackson decode WeEvent failed", e);
                    }

                    // check SubscriptionId
                    log.info("messageId:{}", messageId);
                    this.subscriptionCache.put(event.getTopic(), event.getEventId());
                    if (this.receiptId2SubscriptionId.size() == this.subscriptionId2ReceiptId.size()) {
                        if (this.receiptId2SubscriptionId.containsKey(messageId)) {
                            WeEventStompCommand weEventStompCommand = new WeEventStompCommand(event);
                            weEventStompCommand.setSubscriptionId(subscriptionId);
                            weEventStompCommand.setHeaderId(messageId);
                            this.topicConnection.dispatch(weEventStompCommand);


                        } else {
                            log.error("unknown receipt-id: {}", receiptId);
                        }
                    } else {
                        for (Map.Entry<String, String> sub2reid : this.subscriptionId2ReceiptId.entrySet()) {
                            for (Map.Entry<String, String> reid2sub : this.receiptId2SubscriptionId.entrySet()) {

                                // messageId match two SubscriptionId
                                if (sub2reid.getValue().equals(messageId)) {
                                    if (!reid2sub.getValue().equals(sub2reid.getKey())) {
                                        WeEventStompCommand weEventStompCommand = new WeEventStompCommand(event);
                                        weEventStompCommand.setSubscriptionId(sub2reid.getKey());
                                        weEventStompCommand.setHeaderId(messageId);
                                        this.topicConnection.dispatch(weEventStompCommand);
                                        this.connectFlag = FALSE;
                                    }
                                }


                            }
                        }
                    }

                    break;

                case "HEARTBEAT":
                    break;

                default:
                    log.error("unknown command from stomp server: {}", cmd);
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket transport closed, code: {} reason: {} remote: {}", code, reason, remote);
        this.connected = false;
        this.cleanup();

        // reconnect if connection lost
        if (remote) {
            if (this.connectFlag.equals(FALSE)) {
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

@Slf4j
class WSThread extends Thread {
    private WebSocketTransport webSocketTransport;

    public WSThread(WebSocketTransport webSocketTransport) {
        this.webSocketTransport = webSocketTransport;
    }

    public void run() {
        log.info("thread running");

        try {
            while (!this.webSocketTransport.reconnectBlocking()) {
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        for (Map.Entry<String, String> subscription : this.webSocketTransport.subscriptionCache.entrySet()) {

            try {
                log.info("subscription cache:{}", subscription.toString());
                WeEventTopic weEventTopic = new WeEventTopic(subscription.getKey());
                weEventTopic.setOffset(subscription.getValue());
                this.webSocketTransport.stompSubscribe(weEventTopic);
                this.webSocketTransport.connectFlag = TRUE;

            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
