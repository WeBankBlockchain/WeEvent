package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class TopicHistoricalBase extends BaseEntity {


    @Column(name = "brokerId", columnDefinition = "int(11)")
    private Integer brokerId;

    @Column(name = "topicName", columnDefinition = "varchar(128)")
    private String topicName;

    @Column(name = "groupId", columnDefinition = "varchar(64)")
    private String groupId;

    @Column(name = "eventId", columnDefinition = "varchar(64)")
    private String eventId;

}
