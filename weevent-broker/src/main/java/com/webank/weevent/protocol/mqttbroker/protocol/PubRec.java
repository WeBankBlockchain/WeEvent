package com.webank.weevent.protocol.mqttbroker.protocol;

import com.webank.weevent.protocol.mqttbroker.store.DupPubRelMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPubRelMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPublishMessageStore;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 *@ClassName PubRec
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 14:55
 *@Version 1.0
 **/
@Slf4j
public class PubRec {
    private IDupPublishMessageStore iDupPublishMessageStore;

    private IDupPubRelMessageStore iDupPubRelMessageStore;

    public PubRec(IDupPublishMessageStore iDupPublishMessageStore, IDupPubRelMessageStore iDupPubRelMessageStore) {
        this.iDupPublishMessageStore = iDupPublishMessageStore;
        this.iDupPubRelMessageStore = iDupPubRelMessageStore;
    }

    public void processPubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(variableHeader.messageId()), null);
        log.debug("PUBREC - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        iDupPublishMessageStore.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        DupPubRelMessageStore dupPubRelMessageStore = new DupPubRelMessageStore().setClientId((String) channel.attr(AttributeKey.valueOf("clientId")).get())
                .setMessageId(variableHeader.messageId());
        iDupPubRelMessageStore.put((String) channel.attr(AttributeKey.valueOf("clientId")).get(), dupPubRelMessageStore);
        channel.writeAndFlush(pubRelMessage);
    }
}
