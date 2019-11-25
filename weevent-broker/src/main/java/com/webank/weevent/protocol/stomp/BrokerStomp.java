package com.webank.weevent.protocol.stomp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
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

    private void handleSingleMessage(Message<byte[]> msg, WebSocketSession session) {
        // get the frame type
        String frameType = "";
        Object frameTypeCommand = msg.getHeaders().get("stompCommand");
        if (frameTypeCommand != null) {
            frameType = frameTypeCommand.toString();

        }
        // for MESSAGE frame
        Object simpMessageType = msg.getHeaders().get("simpMessageType");
        if ((simpMessageType != null) && (simpMessageType.toString().equals("HEARTBEAT"))) {
            frameType = "HEARTBEAT";
        }

        if ("HEARTBEAT".equals(frameType)) {
            log.debug("HEARTBEAT from client:{}", session.getId());
            return;
        }

        log.info("remote: {} stomp header: {} payload.length: {}", session.getId(), msg.getHeaders(), msg.getPayload().length);
        // only one topic
        switch (frameType) {
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
                break;
        }
    }

    private void handleConnectMessage(Message<byte[]> msg, WebSocketSession session) {
        StompHeaderAccessor accessor;
        StompCommand command;
        command = checkConnect(msg);

        // package the return frame
        accessor = StompHeaderAccessor.create(command);
        accessor.setVersion("1.1");
        accessor.setHeartbeat(0, BrokerApplication.weEventConfig.getStompHeartbeats() * 1000);

        // if check the user login and password is wrong ,return that message
        if (command == StompCommand.ERROR) {
            accessor.setNativeHeader("message", "login or password is wrong");
        }
        sendSimpleMessage(session, accessor);
    }

    private void handleDisconnectMessage(Message<byte[]> msg, WebSocketSession session) {
        StompHeaderAccessor accessor;

        String headerReceiptIdStr = getHeadersValue("receipt", msg);
        clearSession(session);

        // package the return frame
        accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
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

    @SuppressWarnings("unchecked")
    private void handleSendMessage(Message<byte[]> msg, WebSocketSession session) {
        LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap<String, List<String>>) msg.getHeaders().get("nativeHeaders"));
        if (nativeHeaders == null) {
            log.error("assert nativeHeaders != null");
            return;
        }

        // send command receipt Id
        String headerReceiptIdStr = getHeadersValue("receipt", msg);

        Map<String, String> extensions = WeEventUtils.getExtend(nativeHeaders);

        // group id
        String groupId = "";
        Object eventGroupId = nativeHeaders.get(WeEventConstants.EVENT_GROUP_ID);
        if (nativeHeaders.containsKey(WeEventConstants.EVENT_GROUP_ID) && eventGroupId != null) {
            groupId = ((List) eventGroupId).get(0).toString();
        }

        try {
            String simpDestination = getSimpDestination(msg);
            SendResult sendResult = handleSend(new WeEvent(simpDestination, msg.getPayload(), extensions), groupId);

            // package the return frame
            StompCommand command = StompCommand.RECEIPT;
            StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
            accessor.setDestination(simpDestination);
            accessor.setReceiptId(headerReceiptIdStr);
            accessor.setNativeHeader("receipt-id", headerReceiptIdStr);
            accessor.setNativeHeader(WeEventConstants.EXTENSIONS_EVENT_ID, sendResult.getEventId());
            sendSimpleMessage(session, accessor);
        } catch (BrokerException e) {
            handleErrorMessage(session, e, headerReceiptIdStr);
        }
    }

    private void handleSubscribeMessage(Message<byte[]> msg, WebSocketSession session) {
        LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));
        if (nativeHeaders == null) {
            log.error("assert nativeHeaders != null");
            return;
        }

        // subscribe command id
        String headerIdStr = getHeadersValue("id", msg);

        String subEventId = getHeadersValue(WeEventConstants.EXTENSIONS_EVENT_ID, msg);
        // check if there hasn't the event, need use the last id
        if (StringUtils.isBlank(subEventId)) {
            subEventId = WeEvent.OFFSET_LAST;
        }

        //subscription
        String continueSubscriptionIdStr = getHeadersValue(WeEvent.WeEvent_SubscriptionId, msg);

        String groupId = "";
        if (nativeHeaders.containsKey(WeEventConstants.EVENT_GROUP_ID)) {
            Object eventGroupId = nativeHeaders.get(WeEventConstants.EVENT_GROUP_ID);
            if (eventGroupId != null) {
                groupId = ((List) eventGroupId).get(0).toString();
            }
        }

        String tag = null;
        if (nativeHeaders.containsKey(WeEvent.WeEvent_TAG)) {
            Object value = nativeHeaders.get(WeEvent.WeEvent_TAG);
            if (value != null) {
                tag = ((List) value).get(0).toString();
            }
        }

        try {
            String simpDestination = getSimpDestination(msg);
            String subscriptionId = handleSubscribe(session, simpDestination, groupId, headerIdStr, subEventId, continueSubscriptionIdStr, tag);

            // package the return frame
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
            accessor.setDestination(simpDestination);
            // a unique identifier for that message and a subscription header matching the identifier of the subscription that is receiving the message.
            accessor.setReceiptId(headerIdStr);
            accessor.setSubscriptionId(subscriptionId);
            accessor.setNativeHeader("subscription-id", subscriptionId);
            accessor.setNativeHeader("receipt-id", headerIdStr);
            sendSimpleMessage(session, accessor);
        } catch (BrokerException e) {
            log.info(e.toString());
            handleErrorMessage(session, e, headerIdStr);
        }
    }


    private void handleUnsubscribeMessage(Message<byte[]> msg, WebSocketSession session) {
        String simpDestination = getSimpDestination(msg);
        String headerIdStr = getHeadersValue("id", msg);

        try {
            boolean retUnSubscribe = handleUnSubscribe(session, headerIdStr);
            StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.RECEIPT);
            // package the return frame
            accessor.setDestination(simpDestination);
            // a unique identifier for that message and a subscription header matching the identifier of the subscription that is receiving the message.
            accessor.setReceiptId(headerIdStr);
            accessor.setNativeHeader("receipt-id", headerIdStr);
            accessor.setNativeHeader("message", String.valueOf(retUnSubscribe));
            sendSimpleMessage(session, accessor);
        } catch (BrokerException e) {
            handleErrorMessage(session, e, headerIdStr);
        }
    }

    private void handleDefaultMessage(Message<byte[]> msg, WebSocketSession session) {
        String simpDestination = getSimpDestination(msg);
        String headerIdStr = getHeadersValue("id", msg);

        // package the return frame
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setDestination(simpDestination);
        accessor.setMessage("NOT SUPPORT COMMAND");
        accessor.setNativeHeader("receipt-id", headerIdStr);
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

        String authAccount = BrokerApplication.environment.getProperty("spring.security.user.name");
        String authPassword = BrokerApplication.environment.getProperty("spring.security.user.password");
        if (StringUtils.isBlank(authAccount) || StringUtils.isBlank(authPassword)) {
            return command;
        }

        // check login/password if needed
        try {
            LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));
            if (nativeHeaders != null) {
                Object login = nativeHeaders.get("login");
                if (login != null) {
                    String loginName = ((List) login).get(0).toString();
                    // get the client's passcode
                    String passcode = StompHeaderAccessor.getPasscode(msg.getHeaders());
                    if (!authAccount.equals(loginName) || !authPassword.equals(passcode)) {
                        command = StompCommand.ERROR;
                    }
                }
            }
        } catch (Exception e) {
            log.error("authorize failed");
            command = StompCommand.ERROR;
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
            String subscriptionId = topicPair.getValue().getFirst();
            try {
                boolean result = this.iconsumer.unSubscribe(subscriptionId);
                log.info("consumer unSubscribe result, subscriptionId: {}, result: {}", subscriptionId, result);
            } catch (BrokerException e) {
                log.error("exception in consumer unSubscribe", e);
            }
        }

        sessionContext.remove(session.getId());
    }

    private void handleErrorMessage(WebSocketSession session, BrokerException e, String receiptId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        // package the return frame ,include the error message and error code
        accessor.setNativeHeader("message", e.getMessage());
        accessor.setNativeHeader("code", String.valueOf(e.getCode()));
        accessor.setReceiptId(receiptId);
        accessor.setNativeHeader("receipt-id", receiptId);
        sendSimpleMessage(session, accessor);
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
     * publish event into block
     *
     * @param event event
     * @param groupId groupId
     * @return send result
     * @throws BrokerException broker exception
     */
    private SendResult handleSend(WeEvent event, String groupId) throws BrokerException {
        if (!this.iproducer.startProducer()) {
            log.error("producer start failed");
        }

        SendResult sendResult;
        try {
            sendResult = this.iproducer.publish(event, groupId).get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            log.error("publishWeEvent failed due to transaction execution error.", e);
            sendResult = new SendResult();
            sendResult.setTopic(event.getTopic());
            sendResult.setStatus(SendResult.SendResultStatus.ERROR);
        } catch (TimeoutException e) {
            log.error("publishWeEvent failed due to transaction execution timeout.", e);
            sendResult = new SendResult();
            sendResult.setTopic(event.getTopic());
            sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
        }

        log.info("publish result, {}", sendResult);
        if (sendResult.getStatus() != SendResult.SendResultStatus.SUCCESS) {
            log.error("producer publish failed");
        }
        return sendResult;
    }

    /**
     * @param session stomp session
     * @param simpDestination topic name
     * @param groupId group id
     * @param subEventId event id
     * @param continueSubscriptionId subscription id
     * @param tag weevent-tag
     * @return String consumer subscription id, return "" if error
     * @throws BrokerException Exception
     */
    private String handleSubscribe(WebSocketSession session,
                                   String simpDestination,
                                   String groupId,
                                   String headerIdStr,
                                   String subEventId,
                                   String continueSubscriptionId,
                                   String tag) throws BrokerException {
        log.info("destination: {} header subscribe id: {} group id: {}", simpDestination, headerIdStr, groupId);

        String[] curTopicList;
        if (simpDestination.contains(WeEvent.MULTIPLE_TOPIC_SEPARATOR)) {
            log.info("subscribe topic list");
            curTopicList = simpDestination.split(WeEvent.MULTIPLE_TOPIC_SEPARATOR);
        } else {
            curTopicList = new String[]{simpDestination};
        }

        if (!this.iconsumer.isStarted()) {
            log.info("start consumer");
            this.iconsumer.startConsumer();
        }

        // external params
        Map<IConsumer.SubscribeExt, String> ext = new HashMap<>();
        ext.put(IConsumer.SubscribeExt.InterfaceType, WeEventConstants.STOMPTYPE);
        String remoteIp = session.getRemoteAddress().getAddress().getHostAddress();
        ext.put(IConsumer.SubscribeExt.RemoteIP, remoteIp);
        if (!StringUtils.isBlank(continueSubscriptionId)) {
            log.info("continueSubscriptionId:{}", continueSubscriptionId);
            ext.put(IConsumer.SubscribeExt.SubscriptionId, continueSubscriptionId);
        }
        if (!StringUtils.isBlank(tag)) {
            ext.put(IConsumer.SubscribeExt.TopicTag, tag);
        }

        // support both single/multiple topic
        String subscriptionId = this.iconsumer.subscribe(curTopicList,
                groupId,
                subEventId,
                ext,
                new IConsumer.ConsumerListener() {
                    @Override
                    public void onEvent(String subscriptionId, WeEvent event) {
                        log.info("consumer onEvent, subscriptionId: {} event: {}", subscriptionId, event);
                        try {
                            handleOnEvent(headerIdStr, subscriptionId, event, session);
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
                session.getId(), headerIdStr, subscriptionId, Arrays.toString(curTopicList));
        sessionContext.get(session.getId())
                .put(headerIdStr, Pair.of(subscriptionId, StringUtils.join(curTopicList, WeEvent.MULTIPLE_TOPIC_SEPARATOR)));

        log.info("consumer subscribe success, consumer subscriptionId: {}", subscriptionId);
        return subscriptionId;
    }

    private void handleOnEvent(String headerIdStr,
                               String subscriptionId,
                               WeEvent event,
                               WebSocketSession session) throws IOException {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.MESSAGE);
        // package the return frame
        accessor.setSubscriptionId(headerIdStr);
        accessor.setNativeHeader("subscription-id", subscriptionId);
        accessor.setNativeHeader("message-id", headerIdStr);
        accessor.setMessageId(headerIdStr);
        accessor.setDestination(event.getTopic());
        accessor.setContentType(new MimeType("text", "plain", StandardCharsets.UTF_8));

        // set custom properties in header
        for (Map.Entry<String, String> custom : event.getExtensions().entrySet()) {
            accessor.setNativeHeader(custom.getKey(), custom.getValue());
        }

        // set eventId in header
        accessor.setNativeHeader("eventId", event.getEventId());

        // payload == content
        MessageHeaders headers = accessor.getMessageHeaders();
        Message<byte[]> message = MessageBuilder.createMessage(event.getContent(), headers);
        byte[] bytes = new StompEncoder().encode(message);

        // send to remote
        send2Remote(session, new TextMessage(bytes));
    }

    /**
     * @param session stomp session
     * @param headerIdStr subscription id on stomp
     * @return boolean true if ok
     */
    private boolean handleUnSubscribe(WebSocketSession session, String headerIdStr) throws BrokerException {
        if (!sessionContext.get(session.getId()).containsKey(headerIdStr)) {
            log.info("unknown subscription id, {}", headerIdStr);
            return false;
        }

        String subscriptionId = sessionContext.get(session.getId()).get(headerIdStr).getFirst();
        log.info("session id: {} header id: {} subscriptionId: {}", session.getId(), headerIdStr, subscriptionId);

        // unSubscribe
        boolean result = this.iconsumer.unSubscribe(subscriptionId);
        log.info("consumer unSubscribe, subscriptionId: {} result: {}", subscriptionId, result);
        if (result) {
            // at the same session, remove subscription id in stomp
            sessionContext.get(session.getId()).remove(headerIdStr);
        }

        return result;
    }

    /**
     * use for get the headers
     *
     * @param key headers name
     * @return the headers value
     */
    private String getHeadersValue(String key, Message<byte[]> msg) {
        String id = null;
        LinkedMultiValueMap nativeHeaders = ((LinkedMultiValueMap) msg.getHeaders().get("nativeHeaders"));
        if (nativeHeaders != null) {
            // send command receipt Id
            Object idObject = nativeHeaders.get(key);
            if (idObject != null) {
                id = ((List) idObject).get(0).toString();
            }
        }
        return id;
    }


    private String getSimpDestination(Message<byte[]> msg) {
        String simpDestination = "";
        Object simpDestinationObj = msg.getHeaders().get("simpDestination");
        if (simpDestinationObj != null) {
            simpDestination = simpDestinationObj.toString();
        }

        return simpDestination;
    }

    // methods from super class

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("afterConnectionEstablished, {} {}", session.getId(), session.getRemoteAddress());

        sessionContext.put(session.getId(), new HashMap<>());
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.debug("handle pong message, {}", session.getId());

        super.handlePongMessage(session, message);
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

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (!sessionContext.containsKey(session.getId())) {
            log.error("unknown session id, skip it");
            return;
        }

        StompDecoder decoder = new StompDecoder();
        List<Message<byte[]>> stompMsg = decoder.decode(ByteBuffer.wrap(message.getPayload().getBytes(StandardCharsets.UTF_8)));
        for (Message<byte[]> msg : stompMsg) {
            handleSingleMessage(msg, session);
        }
    }
}
