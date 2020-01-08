package com.webank.weevent.broker.fisco.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class ContractContext {

    private String chainId;

    private BigInteger blockNumber;

    private String topicAddress;

    private String gasLimit;

    private String gasPrice;
}
