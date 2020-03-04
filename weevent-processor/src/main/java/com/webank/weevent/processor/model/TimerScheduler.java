package com.webank.weevent.processor.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TimerScheduler {

    private String id;

    @NotNull(message = "schedulerName cannot be empty")
    private String schedulerName;

    @NotNull(message = "databaseUrl cannot be empty")
    private String databaseUrl;

    @NotNull(message = "periodParams cannot be empty")
    private String periodParams;

    @NotNull(message = "parsingSql cannot be empty")
    private String parsingSql;

    private String dataBaseType;

    private Date createdTime = new Date();

    private Date updatedTime = new Date();


    public TimerScheduler(String id, @NotNull(message = "schedulerName cannot be empty") String schedulerName,
                          @NotNull(message = "databaseUrl cannot be empty") String databaseUrl,
                          @NotNull(message = "periodParams cannot be empty") String periodParams,
                          @NotNull(message = "parsingSql cannot be empty") String parsingSql) {
        this.id = id;
        this.schedulerName = schedulerName;
        this.databaseUrl = databaseUrl;
        this.periodParams = periodParams;
        this.parsingSql = parsingSql;
    }

    public TimerScheduler() {
    }
}
