/**
 * Copyright 2014-2019  the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.weevent.protocol.rest.entity;

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