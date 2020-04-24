package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.ProtocolProcess;
import com.webank.weevent.broker.protocol.mqtt.store.SessionStore;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class UnSubscribe implements MqttCommand {
    private final SessionStore sessionStore;

    public UnSubscribe(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public Optional<MqttMessage> process(MqttMessage req, String clientId, String remoteIp) {
        MqttUnsubscribeMessage msg = (MqttUnsubscribeMessage) req;
        log.info("UNSUBSCRIBE, {}", msg.payload().topics());

        if (msg.payload().topics().isEmpty()) {
            log.error("empty topic, skip it");
            return Optional.empty();
        }

        this.sessionStore.unSubscribe(clientId, msg.payload().topics());

        MqttMessage rsp = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_LEAST_ONCE, false, ProtocolProcess.fixLengthOfMessageId),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()), null);
        return Optional.of(rsp);
    }
}
