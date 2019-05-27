package com.webank.weevent.protocol.mqttbroker.store;

import java.io.Serializable;

/**
 *@ClassName SubscribeStore
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 21:44
 *@Version 1.0
 **/
public class SubscribeStore{
    private String clientId;

    private String topicFilter;

    private int mqttQoS;

    public SubscribeStore(String clientId, String topicFilter, int mqttQoS) {
        this.clientId = clientId;
        this.topicFilter = topicFilter;
        this.mqttQoS = mqttQoS;
    }

    public String getClientId() {
        return clientId;
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
