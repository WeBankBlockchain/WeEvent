package com.webank.weevent.processor.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class StatisticRule implements Serializable {
    private String id;
    private Date startTime;

    @Value("0")
    private int hitTimes;
    @Value("0")
    private int notHitTimes;
    @Value("0")
    private int publishEventSuccess;
    @Value("0")
    private int publishEventFail;
    @Value("0")
    private int writeDBSuccess;
    @Value("0")
    private int writeDBFail;
    private String lastFailReason;
}
