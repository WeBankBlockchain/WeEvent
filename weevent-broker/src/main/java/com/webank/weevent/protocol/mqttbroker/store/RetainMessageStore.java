package com.webank.weevent.protocol.mqttbroker.store;

/**
 *@ClassName RetainMessageStore
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 15:29
 *@Version 1.0
 **/
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
