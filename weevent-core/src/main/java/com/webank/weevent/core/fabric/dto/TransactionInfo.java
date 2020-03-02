package com.webank.weevent.core.fabric.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/9
 */
@Getter
@Setter
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
