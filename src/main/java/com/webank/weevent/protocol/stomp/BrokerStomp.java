package com.webank.weevent.protocol.stomp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.util.StringUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompDecoder;
import org.springframework.messaging.simp.stomp.StompEncoder;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeType;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * @since 2018/12/20.
 */
@Slf4j
@Component
public class BrokerStomp extends TextWebSocketHandler {
    private IProducer iproducer;
    private IConsumer iconsumer;

    // session id <-> [header subscription id in stomp <-> (subscription id in consumer, topic)]
    private static Map<String, Map<String, Pair<String, String>>> sessionContext;


    @Autowired
    public void setProducer(IProducer producer) {
        this.iproducer = producer;
    }

    @Autowired
    public void setConsumer(IConsumer consumer) {
        this.iconsumer = consumer;
    }

    static {
        sessionContext = new HashMap<>();
    }

    public BrokerStomp() {
        super();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("afterConnectionEstablished, {} {}", session.getId(), session.getRemoteAddress());
        sessionContext.put(session.getId(), new HashMap<>());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("remote: {} payload:\n{}", session.getId(), message.getPayload());

        if (!sessionContext.containsKey(session.getId())) {
            log.error("unknown session id, skip it");
            return;
        }

        StompDecoder decoder = new StompDecoder();
        List<Message<byte[]>> stompMsg = decoder.decode(ByteBuffer.wrap(message.getPayload().getBytes(StandardCharsets.UTF_8)));

        StompHeaderAccessor accessor;
        for (Message<byte[]> msg : stompMsg) {
            log.info("stomp header: {}", msg.getHeaders());

            String frameType = "";
            Object frameTypeCommand = msg.getHeaders().get("stompCommand");
            Object simpMessageType = msg.getHeaders().get("simpMessageType");

            if (frameTypeCommand != null) {
                frameType = frameTypeCommand.toString();

            }
            // for special MESSAGE type
            if (simpMessageType != null) {
                if (simpMessageType.toString().equals("HEARTBEAT")) {
                    frameType = "HEARTBEAT";
                }
            }
            String simpDestination = "";
            Object simpDestinationObj = msg.getHeaders().get("simpDestination");
            if (simpDestinationObj != null) {
                simpDestination = simpDestinationObj.toString();
            }

            String headerReceiptIdStr = "";
            String headerIdStr = "";
            String subEventId = "";


            StompCommand command;

            LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));

            if (nativeHeaders != null) {
                // send command receipt Id
                Object headerReceiptId = nativeHeaders.get("receipt");
                if (headerReceiptId != null) {
                    headerReceiptIdStr = ((List) headerReceiptId).get(0).toString();
                }

                // subscribe command id
                Object headerId = nativeHeaders.get("id");
                // client send event id
                Object headerEventId;
                headerEventId = nativeHeaders.get("eventId");

                if (headerId != null) {
                    headerIdStr = ((List) headerId).get(0).toString();
                }

                if (headerEventId != null) {
                    subEventId = ((List) headerEventId).get(0).toString();
                    log.info("subEventId:{}", subEventId);
                }


            }

            // only one topic
            switch (frameType) {
                case "HEARTBEAT":
                    log.info("HEARTBEAT from client:{}", session.getId());
                    break;

                case "CONNECT":
                    command = checkConnect(msg);
                    accessor = StompHeaderAccessor.create(command);
                    accessor.setVersion("1.1");
                    accessor.setHeartbeat(0, BrokerApplication.weEventConfig.getStompHeartbeats() * 1000);
                    sendSimpleMessage(session, accessor);
                    break;

                case "DISCONNECT":
                    accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
                    clearSession(session);
                    accessor.setReceiptId(headerReceiptIdStr);
                    sendSimpleMessage(session, accessor);

                    // close session after reply to client
                    session.close(CloseStatus.NORMAL);
                    break;

                case "SEND":
                    String eventId = handleSend(msg, simpDestination);
                    if (eventId.isEmpty()) {
                        command = StompCommand.ERROR;
                    } else {
                        command = StompCommand.RECEIPT;
                    }
                    accessor = StompHeaderAccessor.create(command);
                    accessor.setDestination(simpDestination);
                    accessor.setReceiptId(headerReceiptIdStr);
                    sendSimpleMessage(session, accessor);
                    break;

                case "SUBSCRIBE":
                    String subscriptionId;
                    log.info("SUBSCRIBE subEventId:{}", subEventId);
                    if (null == subEventId || "".equals(subEventId)) {
                        subscriptionId = handleSubscribe(session, simpDestination, headerIdStr, WeEvent.OFFSET_LAST);
                    } else {
                        subscriptionId = handleSubscribe(session, simpDestination, headerIdStr, subEventId);
                    }


                    if (subscriptionId.isEmpty()) {
                        accessor = StompHeaderAccessor.create(StompCommand.ERROR);
                    } else {
                        accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
                        accessor.setDestination(simpDestination);
                    }
                    // a unique identifier for that message and a subscription header matching the identifier of the subscription that is receiving the message.
                    accessor.setReceiptId(headerIdStr);
                    accessor.setSubscriptionId(subscriptionId);
                    sendSimpleMessage(session, accessor);
                    break;

                case "UNSUBSCRIBE":
                    boolean result = handleUnSubscribe(session, headerIdStr);
                    if (result) {
                        accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
                    } else {
                        accessor = StompHeaderAccessor.create(StompCommand.ERROR);
                    }

                    accessor.setDestination(simpDestination);

                    // a unique identifier for that message and a subscription header matching the identifier of the subscription that is receiving the message.
                    accessor.setReceiptId(headerIdStr);
                    sendSimpleMessage(session, accessor);
                    break;
                default:
                    log.info("unknown command, {}", frameType);

                    accessor = StompHeaderAccessor.create(StompCommand.ERROR);
                    accessor.setDestination(simpDestination);
                    accessor.setMessage("NOT SUPPORT COMMAND");
                    // a unique identifier for that message and a subscription header matching the identifier of the subscription that is receiving the message.
                    sendSimpleMessage(session, accessor);
                    super.handleTransportError(session, new Exception("unknown command"));

                    // follow protocol 1.2 to close connection
                    clearSession(session);
                    session.close();
            }
        }

        super.handleTextMessage(session, message);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("handlePongMessage, {}", session.getId());

        super.handlePongMessage(session, message);
    }

    private StompCommand checkConnect(Message<?> msg) {
        Object passcode = "";
        LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));
        if (nativeHeaders != null) {
            // get the client's passcode
            if (msg.getHeaders().get("stompCredentials") != null) {
                passcode = StompHeaderAccessor.getPasscode(msg.getHeaders());
            }
        }
        Object login;
        String loginName = "";

        if (nativeHeaders != null) if (nativeHeaders.get("login") != null) {
            login = nativeHeaders.get("login");
            if (login != null) loginName = ((List) login).get(0).toString();
        }
        StompCommand command;
        if (StringUtils.isBlank(BrokerApplication.weEventConfig.getStompLogin()) || StringUtils.isBlank(BrokerApplication.weEventConfig.getStompPasscode())) {
            command = StompCommand.CONNECTED;
        } else {
            if (BrokerApplication.weEventConfig.getStompLogin().equals(loginName) && BrokerApplication.weEventConfig.getStompPasscode().equals(passcode)) {
                command = StompCommand.CONNECTED;
            } else {
                command = StompCommand.ERROR;
            }
        }


        return command;
    }

    private void clearSession(WebSocketSession session) {
        log.info("cleanup session: {}", session.getId());

        // remove the Consumer subscribe and the session id
        Map<String, Pair<String, String>> topicMap = sessionContext.get(session.getId());
        if (topicMap.isEmpty()) {
            log.error("not found topic, session: {}", session.getId());
            return;
        }

        log.info("find topic num: {}, try to unSubscribe one by one", topicMap.size());
        for (Map.Entry<String, Pair<String, String>> topicPair : topicMap.entrySet()) {
            String subscriptionId = topicPair.getValue().getKey();
            try {
                boolean result = this.iconsumer.unSubscribe(subscriptionId);
                log.info("consumer unSubscribe result, subscriptionId: {}, result: {}", subscriptionId, result);
            } catch (BrokerException e) {
                log.error("exception in consumer unSubscribe", e);
            }
        }

        sessionContext.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("message decode error, {}", session.getId());
        log.info("message decode exception, {}", exception);
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("connection closed, {} CloseStatus: {}", session.getId(), status);

        clearSession(session);
        super.afterConnectionClosed(session, status);
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }

    private void send2Remote(WebSocketSession session, TextMessage textMessage) {
        try {
            if (!session.isOpen()) {
                log.warn("session is closed, skip sending to {}", session.getId());
                return;
            }

            log.info("send message to remote, {}", session.getId());
            session.sendMessage(textMessage);
        } catch (IOException e) {
            log.error("exception in send simple message to remote", e);
        }
    }

    private void sendSimpleMessage(WebSocketSession session, StompHeaderAccessor accessor) {
        MessageHeaders headers = accessor.getMessageHeaders();
        Message<byte[]> message1 = MessageBuilder.createMessage("".getBytes(StandardCharsets.UTF_8), headers);
        byte[] bytes = new StompEncoder().encode(message1);
        TextMessage textMessage = new TextMessage(bytes);
        send2Remote(session, textMessage);
    }

    /**
     * @param msg message
     * @param simpDestination topic name
     * @return String return event id if publish ok, else ""
     */
    private String handleSend(Message<byte[]> msg, String simpDestination) {
        try {
            if (!this.iproducer.open(simpDestination)) {
                log.error("producer open failed");
                return "";
            }
            if (!this.iproducer.startProducer()) {
                log.error("producer start failed");
                return "";
            }

            SendResult sendResult = this.iproducer.publish(new WeEvent(simpDestination, msg.getPayload()));
            log.info("publish result, {}", sendResult);
            if (sendResult.getStatus() != SendResult.SendResultStatus.SUCCESS) {
                log.error("producer publish failed");
                return "";
            }

            return sendResult.getEventId();
        } catch (
                BrokerException e) {
            log.error("exception in send", e);
            return "";
        }
    }

    /**
     * @param session stomp session
     * @param simpDestination topic name
     * @param headerIdStr header id
     * @return String consumer subscription id, return "" if error
     * @throws Exception Exception
     */
    private String handleSubscribe(WebSocketSession session, String simpDestination, String headerIdStr, String subEventId) throws Exception {
        log.info("destination: {} header subscribe id: {}", simpDestination, headerIdStr);

        String[] curTopicList;
        if (simpDestination.contains(",")) {
            // NOT support
            log.info("subscribe topic list");
            curTopicList = simpDestination.split(",");
        } else {
            curTopicList = new String[]{simpDestination};
        }

        if (!this.iconsumer.isStarted()) {
            log.info("start consumer");
            this.iconsumer.startConsumer();
        }

        // support only one topic
        try {
            String subscriptionId = this.iconsumer.subscribe(curTopicList[0],
                    subEventId,
                    "stomp",
                    new IConsumer.ConsumerListener() {
                        @Override
                        public void onEvent(String subscriptionId, WeEvent event) {
                            log.info("consumer onEvent, subscriptionId: {} event: {}", subscriptionId, event);

                            if (!sessionContext.get(session.getId()).containsKey(headerIdStr)) {
                                log.error("unknown topic on session, {}", event.getTopic());
                                return;
                            }
                            if (!sessionContext.get(session.getId()).get(headerIdStr).getValue().equals(event.getTopic())) {
                                log.error("unknown topic on session, {}", event.getTopic());
                                return;
                            }

                            try {
                                StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
                                accessor.setSubscriptionId(headerIdStr);
                                accessor.setContentType(new MimeType("text", "plain", StandardCharsets.UTF_8));
                                ObjectMapper mapper = new ObjectMapper();
                                MessageHeaders headers = accessor.getMessageHeaders();
                                Message<byte[]> message1 = MessageBuilder.createMessage(mapper.writeValueAsBytes(event), headers);
                                byte[] bytes = new StompEncoder().encode(message1);
                                TextMessage textMessage = new TextMessage(bytes);

                                send2Remote(session, textMessage);
                            } catch (IOException e) {
                                log.error("exception in session.sendMessage", e);
                            }
                        }

                        @Override
                        public void onException(Throwable e) {
                            log.error("consumer onException", e);
                        }
                    });

            log.info("bind context, session id: {} header subscription id: {} consumer subscription id: {} topic: {}",
                    session.getId(), headerIdStr, subscriptionId, curTopicList[0]);
            sessionContext.get(session.getId()).put(headerIdStr, new Pair<>(subscriptionId, curTopicList[0]));

            log.info("consumer subscribe success, consumer subscriptionId: {}", subscriptionId);
            return subscriptionId;
        } catch (BrokerException e) {
            log.error("exception in consumer subscribe", e);
            return "";
        }
    }

    /**
     * @param session stomp session
     * @param headerIdStr subscription id on stomp
     * @return boolean true if ok
     */
    private boolean handleUnSubscribe(WebSocketSession session, String headerIdStr) {
        log.info("session id: {} header subscription id: {}", session.getId(), headerIdStr);

        if (!sessionContext.get(session.getId()).containsKey(headerIdStr)) {
            log.info("unknown subscription id, {}", headerIdStr);
            return false;
        }

        try {
            String subscriptionId = sessionContext.get(session.getId()).get(headerIdStr).getKey();
            // unSubscribe
            boolean result = this.iconsumer.unSubscribe(subscriptionId);
            log.info("consumer unSubscribe, subscriptionId: {} result: {}", subscriptionId, result);
            if (result) {
                // at the same session, remove subscription id in stomp
                sessionContext.get(session.getId()).remove(headerIdStr);
            }

            return result;
        } catch (BrokerException e) {
            log.error("exception in consumer unSubscribe", e);
            return false;
        }
    }
}
