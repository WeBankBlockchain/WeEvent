package com.webank.weevent.core.dto;

import java.math.BigInteger;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class of table tb_block.
 */
@Getter
@Setter
@NoArgsConstructor
public class TbBlock {
    private String pkHash;
    private BigInteger blockNumber = BigInteger.ZERO;
    private String blockTimestamp;
    private int transCount;
    private int sealerIndex;
    private String sealer;
    private String createTime;
    private String modifyTime;

    public TbBlock(String pkHash, BigInteger blockNumber,
                   String blockTimestamp, Integer transCount, int sealerIndex) {
        this.pkHash = pkHash;
        this.blockNumber = blockNumber;
        this.blockTimestamp = blockTimestamp;
        this.transCount = transCount;
        this.sealerIndex = sealerIndex;
    }

}
