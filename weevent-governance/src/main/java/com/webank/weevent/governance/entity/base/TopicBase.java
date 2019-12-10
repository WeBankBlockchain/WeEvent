package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TopicBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper=false)
@MappedSuperclass
public class TopicBase extends BaseEntity {

    @Column(name = "broker_id", columnDefinition = "int(11)")
    private Integer brokerId;

    @Column(name = "group_id", columnDefinition = "varchar(64)")
    private String groupId;

    @Column(name = "topic_name", columnDefinition = "varchar(128)")
    private String topicName;

    @Column(name = "creater")
    private String creater;

    @Column(name = "description")
    private String description;

    //0 means not deleted ,others means deleted
    @Column(name = "delete_at",nullable = false, columnDefinition = "BIGINT(16)")
    private Long deleteAt = 0L;

}
