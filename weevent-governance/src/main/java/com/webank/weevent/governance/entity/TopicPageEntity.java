package com.webank.weevent.governance.entity;

import lombok.Data;
import lombok.NonNull;

@Data
public class TopicPageEntity {

    private String groupId;

    @NonNull
    private Integer brokerId;
    @NonNull
    private Integer pageSize;
    @NonNull
    private Integer pageIndex;
}
