package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class HistoricalDataEntity  extends BaseEntity {

    private Integer brokerId;

    private Integer userId;

    private String topicName;

    private String groupId;

    private Integer blockNumber;

    private String eventId;

}
