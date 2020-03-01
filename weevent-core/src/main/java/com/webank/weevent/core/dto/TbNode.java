package com.webank.weevent.core.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * Entity class of table tb_node.
 */
@Data
public class TbNode {

    private String nodeId;
    private String nodeName;
    private String nodeType;
    private BigInteger blockNumber;
    private BigInteger pbftView;
    private int nodeActive;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
}