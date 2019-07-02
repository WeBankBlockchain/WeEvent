package com.webank.weevent.governance.entity;

import lombok.Data;

@Data
public class TopicCreaterDto {

    private Integer brokerId;

    private String topic;

    private String creater;
}
