package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.ProtocolProcess;
import com.webank.weevent.broker.protocol.mqtt.store.SessionStore;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
@Slf4j
public class Publish implements MqttCommand {
    private final SessionStore sessionStore;

    public Publish(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public Optional<MqttMessage> process(MqttMessage req, String clientId, String remoteIp) throws BrokerException {
        MqttPublishMessage msg = (MqttPublishMessage) req;
        log.info("PUBLISH, {} Qos: {}", msg.variableHeader().topicName(), msg.fixedHeader().qosLevel());

        switch (msg.fixedHeader().qosLevel()) {
            case AT_MOST_ONCE: {
                this.sessionStore.publishMessage(msg, false);
                return Optional.empty();
            }

            case AT_LEAST_ONCE: {
                boolean result = this.sessionStore.publishMessage(msg, false);
                MqttQoS qos = result ? MqttQoS.AT_LEAST_ONCE : MqttQoS.FAILURE;
                MqttMessage rsp = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PUBACK, false, qos, false, ProtocolProcess.fixLengthOfMessageId),
                        MqttMessageIdVariableHeader.from(msg.variableHeader().packetId()), null);
                return Optional.of(rsp);
            }

            case EXACTLY_ONCE:
            default: {
                log.error("DOT NOT support Qos=2, close");
                throw new BrokerException(ErrorCode.MQTT_NOT_SUPPORT_QOS2);
            }
        }
    }
}
