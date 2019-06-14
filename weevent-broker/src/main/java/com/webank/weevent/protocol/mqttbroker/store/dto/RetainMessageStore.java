package com.webank.weevent.protocol.mqttbroker.store.dto;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
public class RetainMessageStore {
    private String topic;

    private byte[] messageBytes;

    private int mqttQoS;

    public String getTopic() {
        return topic;
    }

    public RetainMessageStore setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }

    public RetainMessageStore setMessageBytes(byte[] messageBytes) {
        this.messageBytes = messageBytes;
        return this;
    }

    public int getMqttQoS() {
        return mqttQoS;
    }

    public RetainMessageStore setMqttQoS(int mqttQoS) {
        this.mqttQoS = mqttQoS;
        return this;
    }
}
