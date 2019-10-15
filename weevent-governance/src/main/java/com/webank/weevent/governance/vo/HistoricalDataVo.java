package com.webank.weevent.governance.vo;

import java.util.Date;
import java.util.List;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class HistoricalDataVo extends BaseEntity {

    private Integer brokerId;

    private Integer userId;

    private String topicName;

    private String groupId;

    private Integer blockNumber;

    private Integer eventCount;

    private String eventId;

    private Date beginDate;

    private Date endDate;

    private List<String> topicList;
}
