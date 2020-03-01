package com.webank.weevent.core.dto;

import java.math.BigInteger;

import lombok.Data;

@Data
public class GroupGeneral {

    private int nodeCount;
    private BigInteger transactionCount = BigInteger.ZERO;
    private BigInteger latestBlock = BigInteger.ZERO;

}
