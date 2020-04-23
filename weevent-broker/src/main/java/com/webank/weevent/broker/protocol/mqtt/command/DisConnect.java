package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.store.SessionStore;

import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class DisConnect implements MqttCommand {
    SessionStore sessionStore;

    public DisConnect(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public Optional<MqttMessage> process(MqttMessage req, String clientId, String remoteIp) {
        log.info("DISCONNECT, client id: {}", clientId);

        // clean will message
        this.sessionStore.discardWillMessage(clientId);

        // close connection
        this.sessionStore.removeSession(clientId);

        return Optional.empty();
    }
}
