package com.webank.weevent.broker.protocol.mqtt.store;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Getter;
import lombok.Setter;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
@Getter
@Setter
public class SubscribeData {
    private String clientId;
    private String subscriptionId;
    private String topic;
    private MqttQoS mqttQoS;

    public SubscribeData(String clientId, String subscriptionId, String topic, MqttQoS mqttQoS) {
        this.clientId = clientId;
        this.subscriptionId = subscriptionId;
        this.topic = topic;
        this.mqttQoS = mqttQoS;
    }
}
