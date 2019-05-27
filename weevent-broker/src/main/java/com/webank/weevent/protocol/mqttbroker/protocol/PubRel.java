package com.webank.weevent.protocol.mqttbroker.protocol;

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
 *@ClassName PubRel
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 15:55
 *@Version 1.0
 **/
@Slf4j
public class PubRel {
    public void processPubRel(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        MqttMessage pubCompMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(variableHeader.messageId()), null);
        log.debug("PUBREL - clientId: {}, messageId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        channel.writeAndFlush(pubCompMessage);
    }

}
