package com.webank.weevent.governance.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.webank.weevent.governance.entity.base.TopicBase;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TopicEntity class
 *
 * @since 2019/02/11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_topic",
        uniqueConstraints = {@UniqueConstraint(name = "topicNameBrokerGroupDelete",
                columnNames = {"topic_name", "broker_id", "group_id", "delete_at"})})
public class TopicEntity extends TopicBase {

    @Transient
    private String topicAddress;

    @Transient
    private String senderAddress;

    @Transient
    private Date createdTimestamp;

    @Transient
    private Long sequenceNumber;

    @Transient
    private Long blockNumber;


}
