package com.webank.weevent.governance.entity;

import java.util.Date;
import java.util.List;

import com.webank.weevent.governance.entity.base.HistoricalDataBase;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class HistoricalDataEntity  extends HistoricalDataBase {

    private Integer eventCount;

    private Date beginDate;

    private Date endDate;

    private List<String> topicList;

}
