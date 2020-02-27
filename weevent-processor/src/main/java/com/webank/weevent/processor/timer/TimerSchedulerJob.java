package com.webank.weevent.processor.timer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.utils.CommonUtil;
import com.webank.weevent.processor.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimerSchedulerJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("{},{} timer execute {}  executing...", this.toString(), context.getJobDetail().getKey().getName(), f.format(new Date()));

        String taskName = context.getJobDetail().getKey().getName();
        log.info("{},{} Task execute {}    executing...", this.toString(), taskName, f.format(new Date()));
        dealTimerTask(context, taskName);
    }

    @SuppressWarnings("unchecked")
    private static void dealTimerTask(JobExecutionContext context, String taskName) {
        Object obj = context.getJobDetail().getJobDataMap().get("timer");
        Map<String, TimerScheduler> timerMap = (HashMap) context.getJobDetail().getJobDataMap().get("timerMap");
        try {
            if (obj instanceof TimerScheduler) {
                log.info("{}",  obj);
                TimerScheduler timerScheduler = (TimerScheduler) obj;
                // check the status,when the status equal 1,then update
                log.info("execute  task: {},rule:{}", taskName, JsonUtil.toJSONString(timerScheduler));
                runTask(timerScheduler);
                timerMap.put(timerScheduler.getSchedulerName(), timerScheduler);
            }
        } catch (Exception e) {
            log.info("error:{}", e.toString());
        }
    }

    public static void runTask(TimerScheduler timerScheduler) {
        try (Connection dbcpConnection = CommonUtil.getDbcpConnection(timerScheduler.getDatabaseUrl(),timerScheduler.getDataBaseType())) {
            if (dbcpConnection == null) {
                log.error("database connection fail,databaseUrl:{}", timerScheduler.getDatabaseUrl());
            } else {
                PreparedStatement preparedStmt = dbcpConnection.prepareStatement(timerScheduler.getParsingSql());
                boolean execute = preparedStmt.execute();
                if (execute) {
                    log.info("execute sql success");
                }
                dbcpConnection.close();
            }
        } catch (Exception e) {
            log.error("execute task fail,taskName:{}", timerScheduler.getSchedulerName(), e);
        }
    }

}


