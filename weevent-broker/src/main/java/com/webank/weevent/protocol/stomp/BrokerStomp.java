package com.webank.weevent.protocol.stomp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        for (Message<byte[]> msg : stompMsg) {
            handleSingleMessage(msg, session);
        }

        super.handleTextMessage(session, message);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.info("handlePongMessage, {}", session.getId());

        super.handlePongMessage(session, message);
    }

    private void handleSingleMessage(Message<byte[]> msg, WebSocketSession session) throws BrokerException {
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
        // only one topic
        switch (frameType) {
            case "HEARTBEAT":
                log.info("HEARTBEAT from client:{}", session.getId());
                break;

            case "CONNECT":
                handleConnectMessage(msg, session);
                break;

            case "DISCONNECT":
                handleDisconnectMessage(msg, session);
                break;

            case "SEND":
                handleSendMessage(msg, session);
                break;

            case "SUBSCRIBE":
                handleSubscribeMessage(msg, session);
                break;

            case "UNSUBSCRIBE":
                handleUnsubscribeMessage(msg, session);
                break;
            default:
                handleDefaultMessage(msg, session);
        }
    }

    private void handleConnectMessage(Message<byte[]> msg, WebSocketSession session) {
        StompHeaderAccessor accessor;
        StompCommand command;
        command = checkConnect(msg);
        accessor = StompHeaderAccessor.create(command);
        accessor.setVersion("1.1");
        accessor.setHeartbeat(0, BrokerApplication.weEventConfig.getStompHeartbeats() * 1000);
        sendSimpleMessage(session, accessor);
    }

    private void handleDisconnectMessage(Message<byte[]> msg, WebSocketSession session) {
        StompHeaderAccessor accessor;
        String headerReceiptIdStr = "";
        LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));

        if (nativeHeaders != null) {
            // send command receipt Id
            Object headerReceiptId = nativeHeaders.get("receipt");
            if (headerReceiptId != null) {
                headerReceiptIdStr = ((List) headerReceiptId).get(0).toString();
            }
        }
        accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
        clearSession(session);
        accessor.setReceiptId(headerReceiptIdStr);
        sendSimpleMessage(session, accessor);
        accessor.setNativeHeader("receipt-id", headerReceiptIdStr);
        // close session after reply to client

        try {
            session.close(CloseStatus.NORMAL);
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    private void handleSendMessage(Message<byte[]> msg, WebSocketSession session) {
        StompHeaderAccessor accessor = null;
        log.info("stomp header: {}", msg.getHeaders());

        String simpDestination = "";
        Object simpDestinationObj = msg.getHeaders().get("simpDestination");
        if (simpDestinationObj != null) {
            simpDestination = simpDestinationObj.toString();
        }

        String headerReceiptIdStr = "";
        String subEventId = "";
        StompCommand command;
        LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));
        Map<String, String> extensions = new HashMap<>();
        String groupId = WeEventConstants.DEFAULT_GROUP_ID;
        if (nativeHeaders != null) {
            // send command receipt Id
            Object headerReceiptId = nativeHeaders.get("receipt");
            if (headerReceiptId != null) {
                headerReceiptIdStr = ((List) headerReceiptId).get(0).toString();
            }

            try {
                //extensions
                extensions = WeEventUtils.getExtensions(nativeHeaders);
                if (nativeHeaders.containsKey(WeEventConstants.EVENT_GROUP_ID)) {
                    groupId = nativeHeaders.get(WeEventConstants.EVENT_GROUP_ID).get(0).toString();
                    ParamCheckUtils.validateGroupId(groupId);

                }
            } catch (BrokerException e) {
                log.error(e.toString());
            }

            // client send event id
            Object headerEventId = nativeHeaders.get(WeEventConstants.EXTENSIONS_EVENT_ID);
            if (headerEventId != null) {
                subEventId = ((List) headerEventId).get(0).toString();
                log.info("subEventId:{}", subEventId);
            }
        }

        String eventId;

        try {
            eventId = handleSend(msg, simpDestination, extensions, groupId);
        } catch (BrokerException e) {
            eventId = "";
            handleErrorMessage(accessor,e);
        }
        if (!eventId.isEmpty()) {
            command = StompCommand.RECEIPT;
            accessor = StompHeaderAccessor.create(command);
        }
        accessor.setDestination(simpDestination);
        accessor.setReceiptId(headerReceiptIdStr);
        accessor.setNativeHeader("receipt-id", headerReceiptIdStr);
        sendSimpleMessage(session, accessor);
    }

    private void handleSubscribeMessage(Message<byte[]> msg, WebSocketSession session) {
        StompHeaderAccessor accessor = null;
        log.info("stomp header: {}", msg.getHeaders());
        String simpDestination = "";
        Object simpDestinationObj = msg.getHeaders().get("simpDestination");
        if (simpDestinationObj != null) {
            simpDestination = simpDestinationObj.toString();
        }

        String headerIdStr = "";
        String subEventId = "";
        LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));
        String groupId = WeEventConstants.DEFAULT_GROUP_ID;
        if (nativeHeaders != null) {
            // subscribe command id
            Object headerId = nativeHeaders.get("id");
            if (headerId != null) {
                headerIdStr = ((List) headerId).get(0).toString();
            }
            // client send event id
            Object headerEventId = nativeHeaders.get(WeEventConstants.EXTENSIONS_EVENT_ID);
            if (headerEventId != null) {
                subEventId = ((List) headerEventId).get(0).toString();
                log.info("subEventId:{}", subEventId);
            }
        }

        String subscriptionId;
        log.info("SUBSCRIBE subEventId:{}", subEventId);

        try {
            if (null == subEventId || "".equals(subEventId)) {
                subscriptionId = handleSubscribe(session, simpDestination, groupId, headerIdStr, WeEvent.OFFSET_LAST);
            } else {
                subscriptionId = handleSubscribe(session, simpDestination, groupId, headerIdStr, subEventId);
                accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
                accessor.setDestination(simpDestination);
            }
        } catch (BrokerException e) {
            handleErrorMessage(accessor,e);
            subscriptionId="";
        }

        // a unique identifier for that message and a subscription header matching the identifier of the subscription that is receiving the message.
        accessor.setReceiptId(headerIdStr);
        accessor.setSubscriptionId(subscriptionId);
        accessor.setNativeHeader("subscription-id", subscriptionId);
        accessor.setNativeHeader("receipt-id", headerIdStr);
        sendSimpleMessage(session, accessor);
    }


    private void handleUnsubscribeMessage(Message<byte[]> msg, WebSocketSession session) {
        StompHeaderAccessor accessor = null;
        log.info("stomp header: {}", msg.getHeaders());

        String simpDestination = "";
        Object simpDestinationObj = msg.getHeaders().get("simpDestination");
        if (simpDestinationObj != null) {
            simpDestination = simpDestinationObj.toString();
        }

        String headerIdStr = "";
        LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));
        if (nativeHeaders != null) {
            // subscribe command id
            Object headerId = nativeHeaders.get("id");
            if (headerId != null) {
                headerIdStr = ((List) headerId).get(0).toString();
            }
        }

        boolean result = false;
        try {
            result = handleUnSubscribe(session, headerIdStr);
        } catch (BrokerException e) {
            handleErrorMessage(accessor,e);
        }
        if (result) {
            accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
        }

        accessor.setDestination(simpDestination);

        // a unique identifier for that message and a subscription header matching the identifier of the subscription that is receiving the message.
        accessor.setReceiptId(headerIdStr);
        accessor.setNativeHeader("receipt-id", headerIdStr);
        sendSimpleMessage(session, accessor);
    }

    private void handleDefaultMessage(Message<byte[]> msg, WebSocketSession session) {
        StompHeaderAccessor accessor;
        String simpDestination = "";
        Object simpDestinationObj = msg.getHeaders().get("simpDestination");
        if (simpDestinationObj != null) {
            simpDestination = simpDestinationObj.toString();
        }
        accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setDestination(simpDestination);
        accessor.setMessage("NOT SUPPORT COMMAND");
        accessor.setNativeHeader("message", "NOT SUPPORT COMMAND");
        // a unique identifier for that message and a subscription header matching the identifier of the subscription that is receiving the message.
        sendSimpleMessage(session, accessor);
        try {
            super.handleTransportError(session, new Exception("unknown command"));
            // follow protocol 1.2 to close connection
            clearSession(session);
            session.close();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    private StompCommand checkConnect(Message<byte[]> msg) {
        StompCommand command = StompCommand.CONNECTED;

        String authAccount = BrokerApplication.weEventConfig.getStompLogin();
        String authPassword = BrokerApplication.weEventConfig.getStompPasscode();
        // check login/password if needed
        if (!authAccount.isEmpty() && !authPassword.isEmpty()) {
            try {
                LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));
                String loginName = nativeHeaders.get("login").get(0).toString();
                // get the client's passcode
                String passcode = StompHeaderAccessor.getPasscode(msg.getHeaders());
                if (loginName.equals(authAccount) && passcode.equals(authPassword)) {
                    command = StompCommand.CONNECTED;
                } else {
                    command = StompCommand.ERROR;
                }
            } catch (Exception e) {
                log.error("authorize failed");
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
    private void handleErrorMessage(StompHeaderAccessor accessor,BrokerException e){
        accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        // return the error message and error code
        accessor.setNativeHeader("message", e.getMessage());
        accessor.setNativeHeader("code", String.valueOf(e.getCode()));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("message decode error, {} message decode exception: {}", session.getId(), exception);
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
    private String handleSend(Message<byte[]> msg, String simpDestination, Map<String, String> extensions, String groupId) throws BrokerException {
        if (!this.iproducer.startProducer()) {
            log.error("producer start failed");
            return "";
        }

        SendResult sendResult = this.iproducer.publish(new WeEvent(simpDestination, msg.getPayload(), extensions), groupId);
        log.info("publish result, {}", sendResult);
        if (sendResult.getStatus() != SendResult.SendResultStatus.SUCCESS) {
            log.error("producer publish failed");
            return "";
        }
        return sendResult.getEventId();
    }

    /**
     * @param session stomp session
     * @param simpDestination topic name
     * @param headerIdStr header id
     * @return String consumer subscription id, return "" if error
     * @throws BrokerException Exception
     */
    private String handleSubscribe(WebSocketSession session, String simpDestination, String groupId, String headerIdStr, String subEventId) throws BrokerException {
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
        String subscriptionId = this.iconsumer.subscribe(curTopicList[0],
                groupId,
                subEventId,
                WeEventConstants.STOMPTYPE,
                new IConsumer.ConsumerListener() {
                    @Override
                    public void onEvent(String subscriptionId, WeEvent event) {
                        log.info("consumer onEvent, subscriptionId: {} event: {}", subscriptionId, event);

                        try {
                            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
                            accessor.setSubscriptionId(headerIdStr);
                            accessor.setNativeHeader("subscription-id", subscriptionId);
                            accessor.setNativeHeader("message-id", headerIdStr);
                            accessor.setMessageId(headerIdStr);
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
    }

    /**
     * @param session stomp session
     * @param headerIdStr subscription id on stomp
     * @return boolean true if ok
     */
    private boolean handleUnSubscribe(WebSocketSession session, String headerIdStr) throws BrokerException {
        log.info("session id: {} header id: {} subscription id: {}", session.getId(), headerIdStr);
        if (!sessionContext.get(session.getId()).containsKey(headerIdStr)) {
            log.info("unknown subscription id, {}", headerIdStr);
            return false;
        }

        String subscriptionId = sessionContext.get(session.getId()).get(headerIdStr).getKey();
        // unSubscribe
        boolean result = this.iconsumer.unSubscribe(subscriptionId);
        log.info("consumer unSubscribe, subscriptionId: {} result: {}", subscriptionId, result);
        if (result) {
            // at the same session, remove subscription id in stomp
            sessionContext.get(session.getId()).remove(headerIdStr);
        }

        return result;
    }
}
