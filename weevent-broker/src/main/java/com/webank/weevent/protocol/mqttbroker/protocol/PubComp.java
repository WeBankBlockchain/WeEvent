package com.webank.weevent.protocol.mqttbroker.protocol;

import com.webank.weevent.protocol.mqttbroker.store.IDupPubRelMessageStore;
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
public class PubComp {
    private IMessageIdStore iMessageIdStore;
    private IDupPubRelMessageStore iDupPubRelMessageStore;

    public PubComp(IDupPubRelMessageStore iDupPubRelMessageStore, IMessageIdStore iMessageIdStore) {
        this.iDupPubRelMessageStore = iDupPubRelMessageStore;
        this.iMessageIdStore = iMessageIdStore;
    }

    public void processPubComp(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        log.debug("PUBCOMP - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        iDupPubRelMessageStore.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        iMessageIdStore.releaseMessageId(messageId);
    }
}
