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

/**
 * stomp transport over web socket.
 *
 * @author matthewliu
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
    private Map<Long, String> receiptId2SubscriptionId;

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
                return this.response;
            } else {
                throw new TimeoutException();
            }
        }
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
    public Message stompRequest(String req) throws JMSException {
        log.info("stomp request, size: {}", req.length());

        try {
            ResponseFuture response = new ResponseFuture(this.sequence.longValue());
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

        this.stompRequest(req);

        // initialize connection context
        this.sequence.set(0L);
        this.futures.clear();
        this.receiptId2SubscriptionId.clear();
    }

    public boolean stompDisconnect() throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String req = stompCommand.encodeDisConnect();

        Message stompResponse = this.stompRequest(req);
        return !stompCommand.isError(stompResponse);
    }

    // return eventId
    public String stompSend(WeEventTopic topic, BytesMessage bytesMessage) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        byte[] body = new byte[(int) bytesMessage.getBodyLength()];
        bytesMessage.readBytes(body);
        String req = stompCommand.encodeSend(topic, body);

        Message stompResponse = this.stompRequest(req);
        return stompCommand.getReceipt(stompResponse);
    }

    // return subscriptionId
    public String stompSubscribe(WeEventTopic topic, String offset) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String req = stompCommand.encodeSubscribe(topic, offset);

        Message stompResponse = this.stompRequest(req);
        return stompCommand.getSubscriptionId(stompResponse);
    }

    public boolean stompUnsubscribe(String subscriptionId) throws JMSException {
        WeEventStompCommand stompCommand = new WeEventStompCommand();
        String req = stompCommand.encodeUnSubscribe(subscriptionId);

        Message stompResponse = this.stompRequest(req);
        return !stompCommand.isError(stompResponse);
    }

    // overwrite method from WebSocketClient

    public WebSocketTransport(URI server) {
        super(server, new Draft_6455());

        this.connected = false;
        this.sequence = new AtomicLong(0);
        this.futures = new ConcurrentHashMap<>();
        this.receiptId2SubscriptionId = new ConcurrentHashMap<>();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("WebSocket transport opened, remote address: {}", this.getRemoteSocketAddress().toString());
    }

    @Override
    public void onMessage(String message) {
        if (this.topicConnection == null) {
            return;
        }

        //decode from message
        StompDecoder decoder = new StompDecoder();
        List<Message<byte[]>> messages = decoder.decode(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
        for (Message<byte[]> stompMsg : messages) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(stompMsg);
            StompCommand command = accessor.getCommand();
            if (command == null) {
                continue;
            }

            String cmd = command.getMessageType().toString();
            Long receiptId = -1L;
            if (accessor.getReceiptId() != null) {
                receiptId = Long.valueOf(accessor.getReceiptId());
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
                case "ERROR":
                    if (futures.containsKey(receiptId)) {
                        futures.get(receiptId).setResponse(stompMsg);
                    } else {
                        log.error("unknown receipt-id: {}", receiptId);
                    }
                    break;

                // subscribe push
                case "MESSAGE":
                    WeEvent event = null;
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        event = mapper.readValue(stompMsg.getPayload(), WeEvent.class);
                    } catch (IOException e) {
                        log.error("jackson decode WeEvent failed", e);
                    }

                    if (this.receiptId2SubscriptionId.containsKey(receiptId)) {
                        WeEventStompCommand weEventStompCommand = new WeEventStompCommand(event);
                        weEventStompCommand.setSubscriptionId(this.receiptId2SubscriptionId.get(receiptId));
                        this.topicConnection.dispatch(weEventStompCommand);
                    } else {
                        log.error("unknown receipt-id: {}", receiptId);
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
        if (remote) {
            // reconnect if connection lost
            this.reconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket transport error", ex);

        this.connected = false;
    }
}
