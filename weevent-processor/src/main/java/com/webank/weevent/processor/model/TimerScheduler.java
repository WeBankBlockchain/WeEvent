package com.webank.weevent.processor.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "timer_scheduler_job")
public class TimerScheduler extends TimerSchedulerBase {


    public TimerScheduler(String schedulerName, String jdbcUrl, Long timePeriod, String periodParams, Long delay, String parsingSql) {
        super(schedulerName, jdbcUrl, timePeriod, periodParams, delay, parsingSql);
    }

    public TimerScheduler() {
        super();
    }
}
