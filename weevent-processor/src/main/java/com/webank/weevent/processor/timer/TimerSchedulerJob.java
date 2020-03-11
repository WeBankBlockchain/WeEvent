package com.webank.weevent.processor.timer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.utils.CommonUtil;

import com.fasterxml.jackson.core.type.TypeReference;
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
        try {
            Object obj = context.getJobDetail().getJobDataMap().get("timer");
            TimerScheduler scheduler = JsonHelper.json2Object(obj.toString(), new TypeReference<TimerScheduler>() {
            });
            Map<String, TimerScheduler> timerMap = JsonHelper.json2Object(context.getJobDetail().getJobDataMap().get("timerMap").toString(), new TypeReference<Map<String, TimerScheduler>>() {
            });

            // check the status,when the status equal 1,then update
            log.info("execute  task: {},rule:{}", taskName, JsonHelper.object2Json(scheduler));
            runTask(scheduler);
            timerMap.put(scheduler.getId(), scheduler);
        } catch (Exception e) {
            log.info("error:{}", e.toString());
        }
    }

    public static void runTask(TimerScheduler timerScheduler) {
        try (Connection dbcpConnection = CommonUtil.getDbcpConnection(timerScheduler.getDatabaseUrl(), timerScheduler.getDataBaseType())) {
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
            log.error("execute task fail,taskId:{}", timerScheduler.getId(), e);
        }
    }

}


