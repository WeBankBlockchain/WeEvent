package com.webank.weevent.core.dto;

import lombok.Data;

@Data
public class ContractContext {

    private String chainId;

    private Long blockNumber;

    private Long blockLimit;

    private String topicAddress;

    private Long gasLimit;

    private Long gasPrice;

}
