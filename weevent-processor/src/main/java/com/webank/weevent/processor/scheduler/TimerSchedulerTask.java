package com.webank.weevent.processor.scheduler;

import java.util.TimerTask;

import com.webank.weevent.processor.model.TimerScheduler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class TimerSchedulerTask extends TimerTask {
    private TimerScheduler timerScheduler;

    public TimerSchedulerTask(TimerScheduler timerScheduler) {
        this.timerScheduler = timerScheduler;
    }

    public void run() {
        /**
         * 1。连接数据库
         * 2.执行sql
         */
        try {

        } catch (Exception e) {
            log.info("");
        }
    }
}
