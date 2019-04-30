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
public class TopicInfo {
    /**
     * Topic name.
     */
    private String topicName;

    /**
     * Contract address in block chain.
     */
    private String topicAddress;

    /**
     * Creator's address.
     */
    private String senderAddress;

    /**
     * Create time.
     */
    private Long createdTimestamp;
}
