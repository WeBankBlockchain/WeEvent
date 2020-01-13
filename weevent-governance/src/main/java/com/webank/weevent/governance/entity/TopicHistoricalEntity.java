package com.webank.weevent.governance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.webank.weevent.governance.entity.base.TopicHistoricalBase;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_topic_historical",
        uniqueConstraints = {@UniqueConstraint(name = "brokerIdGroupIdEventId",
                columnNames = {"brokerId", "groupId", "eventId"})})
public class TopicHistoricalEntity extends TopicHistoricalBase {

}
