package com.webank.weevent.broker.protocol.mqtt.command;

import com.webank.weevent.broker.protocol.mqtt.store.MessageIdStore;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class PubAck {
    private MessageIdStore messageIdStore;

    public PubAck(MessageIdStore messageIdStore) {
        this.messageIdStore = messageIdStore;
    }

    public void processPubAck(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        log.debug("PUBACK {}", messageId);
        this.messageIdStore.releaseMessageId(messageId);
    }
}
