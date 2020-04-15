package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.Optional;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
public class PingReq implements MqttCommand {
    @Override
    public Optional<MqttMessage> process(MqttMessage req, String clientId, String remoteIp) {
        MqttMessage rsp = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_LEAST_ONCE, false, 0), null, null);
        return Optional.of(rsp);
    }
}
