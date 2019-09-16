package com.webank.weevent.governance.entity;

import lombok.Data;

@Data
public class TopicPageEntity {

    private Integer brokerId;

    private Integer pageSize;

    private Integer pageIndex;
}
