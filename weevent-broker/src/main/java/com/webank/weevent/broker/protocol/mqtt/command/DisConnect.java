package com.webank.weevent.broker.protocol.mqtt.command;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class DisConnect {
    public void processDisConnect(Channel channel, String clientId, MqttMessage msg) {
        log.info("DISCONNECT, close {}", clientId);
        channel.close();
    }
}
