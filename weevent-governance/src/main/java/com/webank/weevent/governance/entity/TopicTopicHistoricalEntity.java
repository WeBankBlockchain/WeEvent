package com.webank.weevent.governance.entity;

import java.util.Date;
import java.util.List;

import com.webank.weevent.governance.entity.base.TopicHistoricalBase;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class TopicTopicHistoricalEntity extends TopicHistoricalBase {

    private Integer eventCount;

    private Date beginDate;

    private Date endDate;

    private String createDateStr;

    private List<String> topicList;

    private String tableName;

}
