package com.webank.weevent.broker.protocol.mqtt;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.store.SessionContext;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

/**
 * Mqtt 3.1.1 protocol.
 * see at http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html.
 * or https://mcxiaoke.gitbooks.io/mqtt-cn/content.
 * It's different from stomp:
 * 1. mqtt is binary protocol.
 * 2. stomp is supported by spring core originally.
 * 3. mqtt need deal with encode/decode carefully over spring websocket.
 * more websocket protocol see at https://tools.ietf.org/html/rfc6455.
 *
 * @author matthewliu
 * @since 2018/04/09
 */
@Slf4j
@Component
public class WebSocketMqtt extends BinaryWebSocketHandler implements SubProtocolCapable {
    private ProtocolProcess protocolProcess;
    private final static ByteBufAllocator ALLOCATOR = new UnpooledByteBufAllocator(false);

    @Autowired
    public void setProtocolProcess(ProtocolProcess protocolProcess) {
        this.protocolProcess = protocolProcess;
    }

    private void closeSession(WebSocketSession session) {
        try {
            this.cleanSession(session.getId());
            session.close();
        } catch (Exception e) {
            log.error("exception in close session", e);
        }
    }

    private void cleanSession(String sessionId) {
        this.protocolProcess.cleanSession(sessionId);
    }

    public static void send2Remote(WebSocketSession session, MqttMessage mqttMessage) {
        try {
            BinaryMessage binaryMessage = encode(mqttMessage);
            if (session.isOpen()) {
                log.info("send message to remote, {}", session.getId());
                session.sendMessage(binaryMessage);
                return;
            }

            log.warn("session is closed, skip sending to {}", session.getId());
        } catch (BrokerException | IOException e) {
            log.error("exception in send simple message to remote, {}", e.getMessage());
        }
    }

    private void handleSingleMessage(MqttMessage msg, WebSocketSession session) throws BrokerException {
        // process connect
        if (msg.fixedHeader().messageType() == MqttMessageType.CONNECT) {
            MqttConnectMessage connectMessage = (MqttConnectMessage) msg;
            MqttPublishMessage willMessage = this.protocolProcess.genWillMessage(connectMessage);
            SessionContext sessionData = new SessionContext(session.getId(),
                    connectMessage.payload().clientIdentifier(),
                    session,
                    connectMessage.variableHeader().isCleanSession(),
                    willMessage);

            MqttConnAckMessage rsp = this.protocolProcess.processConnect((MqttConnectMessage) msg, sessionData);
            send2Remote(session, rsp);
            if (rsp.variableHeader().connectReturnCode() != MqttConnectReturnCode.CONNECTION_ACCEPTED) {
                this.closeSession(session);
            }
            return;
        }

        String remoteIp = "";
        if (session.getRemoteAddress() != null) {
            remoteIp = session.getRemoteAddress().getAddress().getHostAddress();
        }

        // process the other commands
        Optional<MqttMessage> rsp = this.protocolProcess.process(msg, session.getId(), remoteIp);
        rsp.ifPresent(mqttMessage -> send2Remote(session, mqttMessage));
    }

    // encode mqtt message into websocket BinaryMessage
    private static BinaryMessage encode(MqttMessage message) throws BrokerException {
        ByteBuf byteBuf;
        try {
            //doEncode is not public access
            Class<?> mqttEncoder = MqttEncoder.class;
            Method doEncode = mqttEncoder.getDeclaredMethod("doEncode", ByteBufAllocator.class, MqttMessage.class);
            doEncode.setAccessible(true);
            byteBuf = (ByteBuf) doEncode.invoke(null, ALLOCATOR, message);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("invoke MqttEncoder.doEncode failed, {}", e.getMessage());
            throw new BrokerException(ErrorCode.MQTT_ENCODE_FAILED);
        }

        return new BinaryMessage(byteBuf.array());
    }

    // decode mqtt message from websocket BinaryMessage
    private static MqttMessage decode(BinaryMessage message) throws BrokerException {
        try {
            ByteBufAllocator allocator = new UnpooledByteBufAllocator(false);
            ByteBuf byteBuf = allocator.buffer(message.getPayloadLength());
            byteBuf.writeBytes(message.getPayload().array());

            Class<?> mqttDecoderClz = MqttDecoder.class;
            Method decode = mqttDecoderClz.getDeclaredMethod("decode", ChannelHandlerContext.class, ByteBuf.class, List.class);
            decode.setAccessible(true);

            MqttDecoder mqttDecoder = new MqttDecoder();
            List<Object> out = new ArrayList<>();
            decode.invoke(mqttDecoder, null, byteBuf, out);

            if (out.isEmpty()) {
                log.error("decode mqtt message failed");
                throw new BrokerException(ErrorCode.MQTT_DECODE_FAILED);
            }

            return (MqttMessage) out.get(0);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("decode mqtt message failed, {}", e.getMessage());
            throw new BrokerException(ErrorCode.MQTT_DECODE_FAILED);
        }
    }

    // the following's methods from super class

    @Override
    public List<String> getSubProtocols() {
        // Sec-WebSocket-Version must be 13
        return Collections.singletonList("mqtt");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("mqtt connection in, session id: {} remote: {}", session.getId(), session.getRemoteAddress());
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.debug("handle pong message, {}", session.getId());

        super.handlePongMessage(session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.info("exception on transport, {} {}", session.getId(), exception);

        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("connection closed, {} CloseStatus: {}", session.getId(), status);
        this.cleanSession(session.getId());
        super.afterConnectionClosed(session, status);
    }

    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            MqttMessage msg = decode(message);
            if (msg.decoderResult().isFailure()) {
                Optional<MqttMessage> rsp = this.protocolProcess.getDecodeFailureRsp(msg);
                rsp.ifPresent(mqttMessage -> send2Remote(session, mqttMessage));

                log.error("decode message failed, close session");
                this.closeSession(session);
                return;
            }

            this.handleSingleMessage(msg, session);
        } catch (BrokerException e) {
            log.error("close session, {}", session.getId());
            this.closeSession(session);
        }
    }
}
