package com.webank.weevent.protocol.mqttbroker.store.dto;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
public class DupPublishMessageStore {
    private String clientId;

    private String topic;

    private int mqttQoS;

    private int messageId;

    private byte[] messageBytes;


    public String getClientId() {
        return clientId;
    }

    public DupPublishMessageStore setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public DupPublishMessageStore setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getMqttQoS() {
        return mqttQoS;
    }

    public DupPublishMessageStore setMqttQoS(int mqttQoS) {
        this.mqttQoS = mqttQoS;
        return this;
    }

    public int getMessageId() {
        return messageId;
    }

    public DupPublishMessageStore setMessageId(int messageId) {
        this.messageId = messageId;
        return this;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }

    public DupPublishMessageStore setMessageBytes(byte[] messageBytes) {
        this.messageBytes = messageBytes;
        return this;
    }
}
