package com.webank.weevent.processor.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "TIMER_SCHEDULER_JOB")
public class TimerScheduler extends TimerSchedulerBase {


    public TimerScheduler(String schedulerName, String jdbcUrl, String periodParams, Long delayTime, String parsingSql) {
        super(schedulerName, jdbcUrl, periodParams, delayTime, parsingSql);
    }

    public TimerScheduler() {
        super();
    }
}
