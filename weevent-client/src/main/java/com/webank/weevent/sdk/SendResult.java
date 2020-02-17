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
        NO_PERMISSION,
        FILE_UPLOAD_NOT_YET,
    }

    /**
     * Result status.
     */
    private SendResultStatus status;

    /**
     * Topic name.
     */
    private String topic;

    /**
     * Event ID.
     */
    private String eventId;

    /**
     * FileChunksMeta.
     */
    private FileChunksMeta fileChunksMeta;

    /**
     * File upload complete.
     */
    private boolean finish;

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
}
