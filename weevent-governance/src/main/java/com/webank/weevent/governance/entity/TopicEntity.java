package com.webank.weevent.governance.entity;

import java.util.Date;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;

/**
 * TopicEntity class
 *
 * @since 2019/02/11
 */
@Data
public class TopicEntity extends BaseEntity {

    private Integer isDelete;

    private String topicName;

    private String creater;

    private String topicAddress;

    private String senderAddress;

    private Date createdTimestamp;

    private Long sequenceNumber;

    private Long blockNumber;

    private String description;

}
