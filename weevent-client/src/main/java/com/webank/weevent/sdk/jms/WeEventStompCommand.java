package com.webank.weevent.sdk.jms;


import java.nio.charset.StandardCharsets;

import javax.jms.JMSException;

import com.webank.weevent.sdk.WeEvent;

import lombok.Data;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompEncoder;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeType;

/**
 * Stomp command.
 *
 * @author matthewliu
 * @since 2019/04/11
 */
@Data
public class WeEventStompCommand {
    private final static String stompVersion = "1.1";
    private final static int stompHeartBeat = 30;

    private String subscriptionId;
    private WeEvent event;

    public WeEventStompCommand() {

    }

    public WeEventStompCommand(WeEvent event) {
        this.event = event;
    }

    public String encodeRaw(StompHeaderAccessor accessor) {
        return encodeRaw(accessor, null);
    }

    public String encodeRaw(StompHeaderAccessor accessor, byte[] payload) {
        MessageHeaders headers = accessor.getMessageHeaders();
        Message<byte[]> message = MessageBuilder.createMessage(payload != null ? payload : "".getBytes(StandardCharsets.UTF_8), headers);
        byte[] bytes = new StompEncoder().encode(message);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String encodeConnect(String userName, String password) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setVersion(stompVersion);
        accessor.setAcceptVersion(stompVersion);
        accessor.setHeartbeat(stompHeartBeat, 0);
        if (!userName.isEmpty()) {
            accessor.setLogin(userName);
        }
        if (!password.isEmpty()) {
            accessor.setPasscode(password);
        }

        return encodeRaw(accessor);
    }

    public String encodeDisConnect() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);

        return encodeRaw(accessor);
    }

    public String encodeSubscribe(WeEventTopic topic, String offset) throws JMSException {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setDestination(topic.getTopicName());
        accessor.setNativeHeader("eventId", offset);

        return encodeRaw(accessor);
    }

    public String encodeUnSubscribe(String subscriptionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.UNSUBSCRIBE);
        accessor.setNativeHeader(StompHeaderAccessor.STOMP_ID_HEADER, subscriptionId);

        return encodeRaw(accessor);
    }

    // payload is WeEvent
    public String encodeSend(WeEventTopic topic, byte[] payload) throws JMSException {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.UNSUBSCRIBE);
        accessor.setDestination(topic.getTopicName());
        accessor.setContentType(new MimeType("application", "json", StandardCharsets.UTF_8));
        accessor.setContentLength(payload.length);

        return encodeRaw(accessor, payload);
    }

    public boolean isError(Message message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();
        if (command == null) {
            return false;
        }

        return command.getMessageType().toString().equals("ERROR");
    }

    public String getReceipt(Message message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        return accessor.getReceipt();
    }

    public String getSubscriptionId(Message message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        return accessor.getSubscriptionId();
    }
}
