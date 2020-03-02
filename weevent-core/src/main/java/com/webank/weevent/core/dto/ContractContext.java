package com.webank.weevent.core.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractContext {

    private String chainId;

    private Long blockNumber;

    private Long blockLimit;

    private String topicAddress;

    private Long gasLimit;

    private Long gasPrice;

}
