package com.webank.weevent.processor.model;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StatisticRule {
    private String id;
    private String brokerId;
    private String ruleName;
    private int status;
    private Date startTime;

    private int hitTimes = 0;
    private int notHitTimes = 0;
    private int dataFlowSuccess = 0;
    private int dataFlowFail = 0;
    private int destinationType = 0;
    private String lastFailReason = "";
}
