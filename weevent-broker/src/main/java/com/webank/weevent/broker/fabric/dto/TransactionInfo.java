package com.webank.weevent.broker.fabric.dto;

import lombok.Data;

/**
 * @author websterchen
 * @version v1.1
 * @since 2019/8/9
 */
@Data
public class TransactionInfo {
    private String payLoad;
    private Long blockNumber;
}
