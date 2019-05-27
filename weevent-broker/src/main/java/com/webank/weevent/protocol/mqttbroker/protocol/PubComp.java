package com.webank.weevent.protocol.mqttbroker.protocol;

import com.webank.weevent.protocol.mqttbroker.common.IMessageId;
import com.webank.weevent.protocol.mqttbroker.store.IDupPubRelMessageStore;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 *@ClassName PubComp
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 14:47
 *@Version 1.0
 **/
@Slf4j
public class PubComp {
    private IMessageId iMessageId;

    private IDupPubRelMessageStore iDupPubRelMessageStore;

    public PubComp(IMessageId iMessageId, IDupPubRelMessageStore iDupPubRelMessageStore) {
        this.iMessageId = iMessageId;
        this.iDupPubRelMessageStore = iDupPubRelMessageStore;
    }

    public void processPubComp(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        log.debug("PUBCOMP - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        iDupPubRelMessageStore.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        iMessageId.releaseMessageId(messageId);
    }
}
