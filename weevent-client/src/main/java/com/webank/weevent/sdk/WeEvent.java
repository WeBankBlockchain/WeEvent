package com.webank.weevent.sdk;


import java.io.Serializable;

import lombok.Data;

/**
 * Event entity, like a message in traditional MQ.
 * <p>
 *
 * @author matthewliu
 * @since 2018/11/02
 */
@Data
public class WeEvent implements Serializable {
    public static final String OFFSET_FIRST = "OFFSET_FIRST";
    public static final String OFFSET_LAST = "OFFSET_LAST";

    /**
     * serialID
     */
    private static final long serialVersionUID = 2026046567802960173L;

    /**
     * Binding topic, like "com.webank.mytopicname".
     * 32 bytes limit.
     */
    private String topic;

    /**
     * Custom business data, everything as you like.
     * 10k bytes limit.
     */
    private byte[] content;

    /**
     * Event id in block chain, it's unique under one topic.
     * It is assigned by system default.
     */
    private String eventId = "";

    /**
     * Default construction needed by jackson marshall.
     */
    public WeEvent() {
    }

    /**
     * Construction.
     *
     * @param topic the topic
     * @param content the content, character utf8
     */
    public WeEvent(String topic, byte[] content) {
        this.topic = topic;
        this.content = content;
    }

    @Override
    public String toString() {
        return "WeEvent{" +
                "topic='" + topic + '\'' +
                ", content.length=" + content.length +
                ", eventID='" + eventId + '\'' +
                '}';
    }

}
