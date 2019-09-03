package com.webank.weevent.governance.entity;

import lombok.Data;

@Data
public class TopicCreateEntity {

    private Integer brokerId;

    private String topic;

    private String creater;

    private Integer isDelete;

    private String description;

}
