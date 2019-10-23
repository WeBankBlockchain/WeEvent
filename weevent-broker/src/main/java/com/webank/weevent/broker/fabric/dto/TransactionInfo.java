package com.webank.weevent.broker.fabric.dto;

import lombok.Data;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/9
 */
@Data
public class TransactionInfo {
    /**
     * The payLoad
     */
    private String payLoad;

    /**
     * blockNumber
     */
    private Long blockNumber;

    /**
     * The error code.
     */
    private int code;

    /**
     * The error message.
     */
    private String message;
}
