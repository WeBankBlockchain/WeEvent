package com.webank.weevent.governance.entity;

import java.util.Date;

import com.webank.weevent.governance.entity.base.BaseEntity;
import com.webank.weevent.governance.entity.base.TopicBase;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TopicEntity class
 *
 * @since 2019/02/11
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class TopicEntity extends TopicBase {

    private String topicAddress;

    private String senderAddress;

    private Date createdTimestamp;

    private Long sequenceNumber;

    private Long blockNumber;

}
