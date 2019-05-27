package com.webank.weevent.protocol.mqttbroker.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 *@ClassName PingReq
 *@Description mqttbroker PingReq
 *@Author websterchen
 *@Date 2019/5/21 20:10
 *@Version 1.0
 **/
@Slf4j
public class PingReq {
    public void processPingReq(Channel channel, MqttMessage msg) {
        MqttMessage pingRespMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0), null, null);
        log.debug("PINGREQ - clientId: {}", (String) channel.attr(AttributeKey.valueOf("clientId")).get());
        channel.writeAndFlush(pingRespMessage);
    }

}
