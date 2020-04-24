package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.store.MessageIdStore;

import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class PubAck implements MqttCommand {
    private final MessageIdStore messageIdStore;

    public PubAck(MessageIdStore messageIdStore) {
        this.messageIdStore = messageIdStore;
    }

    @Override
    public Optional<MqttMessage> process(MqttMessage req, String clientId, String remoteIp) {
        MqttPubAckMessage msg = (MqttPubAckMessage) req;
        int messageId = msg.variableHeader().messageId();
        log.info("PUBACK, message Id: {}", messageId);

        this.messageIdStore.releaseMessageId(messageId);
        return Optional.empty();
    }
}
