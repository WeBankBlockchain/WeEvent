package com.webank.weevent.protocol.mqttbroker.protocol;

import com.webank.weevent.protocol.mqttbroker.common.IMessageId;
import com.webank.weevent.protocol.mqttbroker.store.IDupPublishMessageStore;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 *@ClassName PubAck
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 14:46
 *@Version 1.0
 **/
@Slf4j
public class PubAck {
    private IMessageId iMessageId;

    private IDupPublishMessageStore iDupPublishMessageStore;

    public PubAck(IMessageId iMessageId, IDupPublishMessageStore iDupPublishMessageStore) {
        this.iMessageId = iMessageId;
        this.iDupPublishMessageStore = iDupPublishMessageStore;
    }

    public void processPubAck(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        log.debug("PUBACK - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        iDupPublishMessageStore.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        iMessageId.releaseMessageId(messageId);
    }
}
