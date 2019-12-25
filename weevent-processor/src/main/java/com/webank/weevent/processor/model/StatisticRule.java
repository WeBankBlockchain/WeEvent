package com.webank.weevent.processor.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class StatisticRule implements Serializable {
    private String id;
    private String brokerId;
    private String ruleName;
    private int status;
    private Date startTime;

    @Value("0")
    private int hitTimes;
    @Value("0")
    private int notHitTimes;
    @Value("0")
    private int dataFlowSuccess;
    @Value("0")
    private int dataFlowFail;
    @Value("0")
    private int destinationType;
    @Value("")
    private String lastFailReason;
}
