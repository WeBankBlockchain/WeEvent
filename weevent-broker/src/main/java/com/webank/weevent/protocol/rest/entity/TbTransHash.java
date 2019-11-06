/**
 * Copyright 2014-2019  the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.weevent.protocol.rest.entity;

import java.math.BigInteger;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class of table tb_trans_hash.
 */
@Data
@NoArgsConstructor
public class TbTransHash {

    private String transHash;
    private String transFrom;
    private String transTo;
    private BigInteger blockNumber;
    private String blockTimestamp;
    private String createTime;
    private String modifyTime;

    /**
     * init by transaction、blockNumber、blockTimestamp.
     */
    public TbTransHash(String transHash, String transFrom, String transTo, BigInteger blockNumber, String blockTimestamp) {
        this.transHash = transHash;
        this.transFrom = transFrom;
        this.transTo = transTo;
        this.blockNumber = blockNumber;
        this.blockTimestamp = blockTimestamp;
    }

}
