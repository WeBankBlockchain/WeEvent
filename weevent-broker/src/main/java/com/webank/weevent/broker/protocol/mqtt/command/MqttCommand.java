package com.webank.weevent.broker.protocol.mqtt.command;


import java.util.Optional;

import com.webank.weevent.client.BrokerException;

import io.netty.handler.codec.mqtt.MqttMessage;

/**
 * Mqtt command handler.
 *
 * @author matthewliu
 * @since 2020/04/15
 */
public interface MqttCommand {
    /**
     * @param req message from client
     * @param clientId client id
     * @param remoteIp remote ip
     * @return MqttMessage response message need to send to remote if have
     * @throws BrokerException throw exception if need close channel
     */
    Optional<MqttMessage> process(MqttMessage req, String clientId, String remoteIp) throws BrokerException;
}
