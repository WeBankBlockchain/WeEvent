package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class TopicHistoricalBase extends BaseEntity {


    @Column(name = "brokerId")
    private Integer brokerId;

    @Column(name = "topicName")
    private String topicName;

    @Column(name = "groupId")
    private String groupId;

    @Column(name = "eventId")
    private String eventId;

}
