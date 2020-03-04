package com.webank.weevent.core.dto;

import java.math.BigInteger;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class of table tb_trans_hash.
 */
@Getter
@Setter
@NoArgsConstructor
public class TbTransHash {

    private String transHash;
    private String transFrom;
    private String transTo;
    private BigInteger blockNumber;
    private String blockTimestamp;
    private String createTime;
    private String modifyTime;

    public TbTransHash(String transHash, String transFrom, String transTo, BigInteger blockNumber, String blockTimestamp) {
        this.transHash = transHash;
        this.transFrom = transFrom;
        this.transTo = transTo;
        this.blockNumber = blockNumber;
        this.blockTimestamp = blockTimestamp;
    }

}
