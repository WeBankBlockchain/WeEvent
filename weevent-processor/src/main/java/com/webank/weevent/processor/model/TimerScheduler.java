package com.webank.weevent.processor.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "timer_scheduler_job")
public class TimerScheduler extends TimerSchedulerBase {


    public TimerScheduler(String schedulerName, String jdbcUrl,
                          Long timePeriod, String parsingSql,
                          Date createdTime, Date updatedTime) {
        super(schedulerName, jdbcUrl, timePeriod, parsingSql, createdTime, updatedTime);
    }

    public TimerScheduler() {
        super();
    }

    //year ,month,week,day ,hour,minute,second
    private Map<String, Integer> periodMap = new HashMap<>();

    public Map<String, Integer> getPeriodMap() {
        return periodMap;
    }

    public void setPeriodMap(Map<String, Integer> periodMap) {
        this.periodMap = periodMap;
    }
}
