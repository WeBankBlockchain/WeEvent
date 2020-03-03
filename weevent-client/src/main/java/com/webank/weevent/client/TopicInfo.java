package com.webank.weevent.client;

import lombok.Getter;
import lombok.Setter;

/**
 * Publish function result.
 * <p>
 *
 * @author matthewliu
 * @since 2018/11/02
 */
@Getter
@Setter
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

    /**
     * Sequence number.
     */
    private Long sequenceNumber;

    /**
     * block number.
     */
    private Long blockNumber;

    /**
     * last publish time.
     */
    private Long lastTimestamp;

    /**
     * last publish sender.
     */
    private String lastSender;

    /**
     * last publish sender.
     */
    private Long lastBlock;
}
