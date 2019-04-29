package com.webank.weevent.broker.fisco.dto;

import java.util.List;

import lombok.Data;

/**
 * @author websterchen
 * @version 1.0
 * @since 2018/12/14
 */
@Data
public class FiscoBlockChainInfo {
    private Long blockNumber;
    private List<String> nodeIdList;
    private List<String> nodeIpList;
}
