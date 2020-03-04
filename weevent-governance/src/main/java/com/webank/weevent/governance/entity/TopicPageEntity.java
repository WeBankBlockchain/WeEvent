package com.webank.weevent.governance.entity;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class TopicPageEntity {

    private String groupId;

    @NonNull
    private Integer brokerId;
    @NonNull
    private Integer pageSize;
    @NonNull
    private Integer pageIndex;
}
