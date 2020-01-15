package com.webank.weevent.processor.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TimerScheduler implements Serializable {


    @NotNull(message = "schedulerName cannot be empty")
    private String schedulerName;

    @NotNull(message = "jdbcUrl cannot be empty")
    private String jdbcUrl;

    @NotNull(message = "periodParams cannot be empty")
    private String periodParams;

    private Long delayTime;

    @NotNull(message = "parsingSql cannot be empty")
    private String parsingSql;

    private Date createdTime = new Date();

    private Date updatedTime = new Date();


    public TimerScheduler(String schedulerName, String jdbcUrl, String periodParams, Long delayTime, String parsingSql) {
        this.schedulerName = schedulerName;
        this.jdbcUrl = jdbcUrl;
        this.periodParams = periodParams;
        this.delayTime = delayTime;
        this.parsingSql = parsingSql;
    }

    public TimerScheduler() {
    }
}
