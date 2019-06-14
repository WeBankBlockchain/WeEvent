package com.webank.weevent.protocol.mqttbroker.store.dto;

import lombok.Data;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
public class SubscribeStore {
    private String clientId;
    private String subscriptionId;
    private String topicFilter;

    private int mqttQoS;

    public SubscribeStore(String clientId, String subscriptionId, String topicFilter, int mqttQoS) {
        this.clientId = clientId;
        this.subscriptionId = subscriptionId;
        this.topicFilter = topicFilter;
        this.mqttQoS = mqttQoS;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public SubscribeStore setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getTopicFilter() {
        return topicFilter;
    }

    public SubscribeStore setTopicFilter(String topicFilter) {
        this.topicFilter = topicFilter;
        return this;
    }

    public int getMqttQoS() {
        return mqttQoS;
    }

    public SubscribeStore setMqttQoS(int mqttQoS) {
        this.mqttQoS = mqttQoS;
        return this;
    }
}
