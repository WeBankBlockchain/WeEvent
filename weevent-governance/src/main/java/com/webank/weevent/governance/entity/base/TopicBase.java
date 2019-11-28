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

    @Column(name = "broker_id")
    private Integer brokerId;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "creater")
    private String creater;

    @Column(name = "description")
    private String description;

    @Column(name = "delete_at")
    private String deleteAt;

}
