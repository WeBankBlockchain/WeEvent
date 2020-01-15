package com.webank.weevent.processor.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class TimerScheduler implements Serializable {


    @NotNull(message = "schedulerName cannot be empty")
    private String schedulerName;

    @NotNull(message = "databaseUrl cannot be empty")
    private String databaseUrl;

    @NotNull(message = "periodParams cannot be empty")
    private String periodParams;

    @NotNull(message = "parsingSql cannot be empty")
    private String parsingSql;

    private Date createdTime = new Date();

    private Date updatedTime = new Date();


    public TimerScheduler(String schedulerName, String databaseUrl, String periodParams , String parsingSql) {
        this.schedulerName = schedulerName;
        this.databaseUrl = databaseUrl;
        this.periodParams = periodParams;
        this.parsingSql = parsingSql;
    }

    public TimerScheduler() {
    }
}
