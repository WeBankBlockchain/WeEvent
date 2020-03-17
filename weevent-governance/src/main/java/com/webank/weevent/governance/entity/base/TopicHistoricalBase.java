package com.webank.weevent.governance.entity.base;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class TopicHistoricalBase {

    /**
     * primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;


    @Column(name = "brokerId", columnDefinition = "int(11)")
    private Integer brokerId;

    @Column(name = "topicName", columnDefinition = "varchar(128)")
    private String topicName;

    @Column(name = "groupId", columnDefinition = "varchar(64)")
    private String groupId;

    @Column(name = "eventId", columnDefinition = "varchar(64)")
    private String eventId;

    @Column(name = "create_date")
    private Date createDate;

    @Column(name = "last_update")
    private Date lastUpdate;

}
