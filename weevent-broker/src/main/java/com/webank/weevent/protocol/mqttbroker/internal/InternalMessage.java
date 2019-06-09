package com.webank.weevent.protocol.mqttbroker.internal;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/9
 */
public class InternalMessage {
    private String topic;

    private int mqttQoS;

    private byte[] messageBytes;

    private boolean retain;

    private boolean dup;

    public String getTopic() {
        return topic;
    }

    public InternalMessage setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getMqttQoS() {
        return mqttQoS;
    }

    public InternalMessage setMqttQoS(int mqttQoS) {
        this.mqttQoS = mqttQoS;
        return this;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }

    public InternalMessage setMessageBytes(byte[] messageBytes) {
        this.messageBytes = messageBytes;
        return this;
    }

    public boolean isRetain() {
        return retain;
    }

    public InternalMessage setRetain(boolean retain) {
        this.retain = retain;
        return this;
    }

    public boolean isDup() {
        return dup;
    }

    public InternalMessage setDup(boolean dup) {
        this.dup = dup;
        return this;
    }
}
