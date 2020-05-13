package com.webank.weevent.broker.protocol.mqtt.store;

import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
@Getter
@Setter
@ToString
public class SubscribeData {
    private String clientId;
    private MqttQoS mqttQoS;
    private String topic;
    // always empty
    private String groupId;

    private String subscriptionId = "";
    private String offset = "";

    public SubscribeData(String clientId, String topic, MqttQoS mqttQoS) {
        this.clientId = clientId;
        this.topic = topic;
        this.groupId = "";
        this.mqttQoS = mqttQoS;
    }

    // jackson need
    private SubscribeData() {
    }

}
