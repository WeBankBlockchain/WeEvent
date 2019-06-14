package com.webank.weevent.protocol.mqttbroker.mqttprotocol;

import com.webank.weevent.protocol.mqttbroker.store.IMessageIdStore;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class PubAck {
    private IMessageIdStore iMessageIdStore;

    public PubAck(IMessageIdStore iMessageIdStore) {
        this.iMessageIdStore = iMessageIdStore;
    }

    public void processPubAck(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        log.debug("PUBACK - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        iMessageIdStore.releaseMessageId(messageId);
    }
}
