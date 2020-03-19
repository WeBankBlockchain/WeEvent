package com.webank.weevent.broker.protocol.mqtt.command;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class PingReq {
    public void processPingReq(Channel channel, MqttMessage msg) {
        MqttMessage rsp = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_LEAST_ONCE, false, 0), null, null);
        channel.writeAndFlush(rsp);
    }
}
