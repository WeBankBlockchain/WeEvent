package com.webank.weevent.sdk;

import lombok.Data;

/**
 * Publish function result.
 * <p>
 *
 * @author matthewliu
 * @since 2018/11/02
 */
@Data
public class SendResult {
    public enum SendResultStatus {
        SUCCESS,
        TIMEOUT,
        ERROR,
    }

    /**
     * Default construction needed by jackson marshall.
     */
    public SendResult() {
    }

    public SendResult(SendResultStatus status) {
        this.status = status;
    }

    public SendResult(SendResultStatus status, String topic, String eventId) {
        this.status = status;
        this.topic = topic;
        this.eventId = eventId;
    }

    /**
     * Topic name.
     */
    String topic;

    /**
     * Event ID.
     */
    String eventId;

    /**
     * Result status.
     */
    SendResultStatus status;
}
