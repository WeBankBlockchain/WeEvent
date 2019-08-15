package com.webank.weevent.sdk;


import java.io.Serializable;
import java.util.Map;

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
    private static final long serialVersionUID = 2026046567802960173L;

    public static final String OFFSET_FIRST = "OFFSET_FIRST";
    public static final String OFFSET_LAST = "OFFSET_LAST";

    /**
     * custom header is prefixed with "weevent-", and the following key is reserved in WeEvent.
     */
    public static final String WeEvent_FORMAT = "weevent-format";
    public static final String WeEvent_SubscriptionId = "weevent-subscriptionId";
    public static final String WeEvent_TAG = "weevent-tag";

    /**
     * Binding topic, like "com.weevent.test".
     */
    private String topic;

    /**
     * Custom business data, everything as you like.
     * 10k bytes limit.
     */
    private byte[] content;

    /**
     * event's custom header.
     * null if not set. all data length in total must be less then 1k.
     */
    private Map<String, String> extensions;

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

    /**
     * Construction.
     *
     * @param topic the topic
     * @param content the content, character utf8
     * @param extensions event's custom header
     */
    public WeEvent(String topic, byte[] content, Map<String, String> extensions) {
        this.topic = topic;
        this.content = content;
        this.extensions = extensions;
    }

    @Override
    public String toString() {
        return "WeEvent{" +
                "topic='" + topic + '\'' +
                ", content.length=" + content.length +
                ", eventID='" + eventId + '\'' +
                ", extensions=" + extensions +
                '}';
    }
}
