package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TopicCreateEntity extends BaseEntity {

    private Integer brokerId;

    private String topic;

    private String creater;

    private Integer isDelete;

    private String description;

}
