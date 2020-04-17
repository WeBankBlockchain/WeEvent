package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.Optional;

import com.webank.weevent.client.BrokerException;

import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class DisConnect implements MqttCommand {
    @Override
    public Optional<MqttMessage> process(MqttMessage req, String clientId, String remoteIp) throws BrokerException {
        log.info("DISCONNECT, close {}", clientId);
        return Optional.empty();
    }
}
