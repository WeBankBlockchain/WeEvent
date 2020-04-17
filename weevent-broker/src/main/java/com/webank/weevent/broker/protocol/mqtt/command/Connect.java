package com.webank.weevent.broker.protocol.mqtt.command;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.store.AuthService;
import com.webank.weevent.broker.protocol.mqtt.store.SessionContext;
import com.webank.weevent.broker.protocol.mqtt.store.SessionStore;

import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
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

    public Connect(AuthService authService, SessionStore sessionStore) {
        this.authService = authService;
        this.sessionStore = sessionStore;
    }

    public MqttMessage processConnect(MqttConnectMessage msg, SessionContext sessionData) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_LEAST_ONCE, false, 0);

        String clientId = sessionData.getClientId();
        if (StringUtils.isBlank(clientId)) {
            log.error("clientId is empty, reject");
            return MqttMessageFactory.newMessage(fixedHeader,
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
        }

        // clean flag
        if (!msg.variableHeader().isCleanSession()) {
            log.error("only support clean session, reject");
            return MqttMessageFactory.newMessage(fixedHeader,
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
        }

        // verify userName and password
        String username = msg.payload().userName();
        String password = msg.payload().passwordInBytes() == null ? null : new String(msg.payload().passwordInBytes(), StandardCharsets.UTF_8);
        if (!this.authService.verifyUserName(username, password)) {
            log.error("verify account failed, reject");
            return MqttMessageFactory.newMessage(fixedHeader,
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false), null);
        }

        Optional<SessionContext> sessionContext = this.sessionStore.getSession(clientId);
        if (sessionContext.isPresent()) {
            log.info("clientId is exist, close the older");
            sessionContext.get().closeSession();
        }

        // store session
        this.sessionStore.addSession(sessionData.getClientId(), sessionData);

        log.info("MQTT connected, clientId: {}", clientId);
        return MqttMessageFactory.newMessage(fixedHeader,
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false), null);
    }
}
