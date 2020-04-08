package com.webank.weevent.broker.protocol.mqtt.command;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.BrokerHandler;
import com.webank.weevent.broker.protocol.mqtt.store.AuthService;
import com.webank.weevent.broker.protocol.mqtt.store.SessionContext;
import com.webank.weevent.broker.protocol.mqtt.store.SessionStore;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
@Slf4j
public class Connect {
    private AuthService authService;
    private SessionStore sessionStore;
    private int keepalive;

    public Connect(AuthService authService, SessionStore sessionStore, int keepalive) {
        this.authService = authService;
        this.sessionStore = sessionStore;
        this.keepalive = keepalive;
    }

    // return clientId if accept
    public String processConnect(Channel channel, MqttConnectMessage msg) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_LEAST_ONCE, false, 0);

        String clientId = msg.payload().clientIdentifier();
        if (StringUtils.isBlank(clientId)) {
            log.error("clientId is empty, reject");

            MqttMessage rsp = MqttMessageFactory.newMessage(fixedHeader,
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
            BrokerHandler.sendRemote(channel, rsp);
            return "";
        }

        // clean flag
        if (!msg.variableHeader().isCleanSession()) {
            log.error("only support clean session, reject");

            MqttMessage rsp = MqttMessageFactory.newMessage(fixedHeader,
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
            BrokerHandler.sendRemote(channel, rsp);
            return "";
        }

        // verify userName and password
        String username = msg.payload().userName();
        String password = msg.payload().passwordInBytes() == null ? null : new String(msg.payload().passwordInBytes(), StandardCharsets.UTF_8);
        if (!this.authService.verifyUserName(username, password)) {
            log.error("verify account failed, reject");

            MqttMessage rsp = MqttMessageFactory.newMessage(fixedHeader,
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false), null);
            BrokerHandler.sendRemote(channel, rsp);
            return "";
        }

        // process heartbeat
        int heartbeat = (int) (msg.variableHeader().keepAliveTimeSeconds() * 1.5f);
        if (heartbeat > 0 && heartbeat < this.keepalive) {
            log.info("use heart beat from client, {}", msg.variableHeader().keepAliveTimeSeconds());

            if (channel.pipeline().names().contains("idle")) {
                channel.pipeline().remove("idle");
            }
            channel.pipeline().addFirst("idle", new IdleStateHandler(0, 0, Math.min(heartbeat, this.keepalive)));
        }

        Optional<SessionContext> sessionContext = this.sessionStore.getSession(clientId);
        if (sessionContext.isPresent()) {
            log.error("clientId is exist, close the older");
            sessionContext.get().getChannel().close();
        }

        // process will message
        SessionContext sessionData = new SessionContext(clientId, channel, msg.variableHeader().isCleanSession(), null);
        if (msg.variableHeader().isWillFlag()) {
            log.info("get will message from client");

            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(msg.variableHeader().willQos()), msg.variableHeader().isWillRetain(), 0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(), 0), Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes()));
            sessionData.setWillMessage(willMessage);
        }

        // store session
        this.sessionStore.addSession(clientId, sessionData);
        MqttMessage rsp = MqttMessageFactory.newMessage(fixedHeader,
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false), null);
        BrokerHandler.sendRemote(channel, rsp);

        return clientId;
    }
}
