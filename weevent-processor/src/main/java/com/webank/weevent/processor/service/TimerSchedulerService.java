package com.webank.weevent.processor.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.utils.CommonUtil;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.RetCode;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TimerSchedulerService {

    private static Scheduler scheduler;


    public TimerSchedulerService(Scheduler scheduler) {
        TimerSchedulerService.scheduler = scheduler;
    }

    @SuppressWarnings("unchecked")
    public RetCode createTimerScheduler(String jobName, String jobGroupName, String triggerName, String triggerGroupName, Class jobClass, JobDataMap params, TimerScheduler timerScheduler) {
        try {
            // get the all timer
            Iterator<JobKey> jobKeyIterator = scheduler.getJobKeys(GroupMatcher.groupEquals(jobGroupName)).iterator();
            List<TimerScheduler> timerSchedulerList = new ArrayList<>();
            Map<String, TimerScheduler> timerSchedulerMap = new HashMap<>();
            TimerScheduler currentTimer = JsonHelper.json2Object(params.get("timer").toString(), TimerScheduler.class);
            while (jobKeyIterator.hasNext()) {
                JobKey jobKey = jobKeyIterator.next();
                if (null != scheduler.getJobDetail(jobKey).getJobDataMap().get("timer")) {
                    TimerScheduler timer = JsonHelper.json2Object(scheduler.getJobDetail(jobKey).getJobDataMap().get("timer").toString(),TimerScheduler.class);
                    // if the current is delete
                    timerSchedulerList.add(timer);
                    timerSchedulerMap.put(timer.getId(), timer);
                }
            }
            timerSchedulerMap.put(currentTimer.getId(), currentTimer);
            timerSchedulerList.add(currentTimer);
            params.put("timerMap", JsonHelper.object2Json(timerSchedulerMap));
            log.info("update the timer timerMap:{},ruleList:{}", timerSchedulerList.size(), timerSchedulerList.size());

            JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).setJobData(params).requestRecovery(true).storeDurably(true).build();

            //
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(new Date().toString().concat(currentTimer.getSchedulerName()), triggerGroupName)
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(timerScheduler.getPeriodParams())).forJob(jobName, jobGroupName).build();
            //check
            RetCode retCode = checkTimerTask(timerScheduler, params, jobName, jobGroupName, triggerName, triggerGroupName);
            if (1 == retCode.getErrorCode()) {
                return retCode;
            }

            scheduler.scheduleJob(job, trigger);

            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            if (scheduler.checkExists(JobKey.jobKey(jobName, jobGroupName))) {
                log.info("deal timer task:{} success", jobName);
                return ConstantsHelper.RET_SUCCESS;
            }
            return ConstantsHelper.RET_FAIL;
        } catch (Exception e) {
            log.error("e:{}", e.toString());
            return RetCode.mark(1, e.toString());
        }
    }

    private RetCode checkTimerTask(TimerScheduler timerScheduler, JobDataMap params, String jobName, String jobGroupName, String triggerName, String triggerGroupName) throws BrokerException, SchedulerException {
        Connection dbcpConnection = CommonUtil.getDbcpConnection(timerScheduler.getDatabaseUrl(), timerScheduler.getDataBaseType());
        if (dbcpConnection == null) {
            return RetCode.mark(1, "database connect fail,please enter the correct database URL");
        }
        boolean exists = scheduler.checkExists(JobKey.jobKey(jobName, jobGroupName));
        if (exists && "createTimerTask".equals(params.get("type").toString())) {
            return RetCode.mark(1, "create job fail,job is exists");
        }
        if (!exists && "updateTimerTask".equals(params.get("type").toString())) {
            return RetCode.mark(1, "update job fail,job is not exists");
        }
        if (exists && "updateTimerTask".equals(params.get("type").toString())) {
            removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
            return ConstantsHelper.RET_SUCCESS;
        }
        return ConstantsHelper.RET_SUCCESS;
    }


    @Transactional(rollbackFor = Throwable.class)
    public void deleteTimerScheduler(TimerScheduler timerScheduler) throws BrokerException {
        try {
            this.removeJob(timerScheduler.getId(), "timer", "timer", "timer-trigger");
            log.info("delete timerScheduler success");
        } catch (Exception e) {
            log.error("delete timerScheduler fail", e);
            throw new BrokerException("delete timerScheduler fail," + e.getMessage());
        }
    }


    /**
     * remove
     *
     * @param jobName remove job
     * @param jobGroupName job group name
     * @param triggerName trigger name
     * @param triggerGroupName trigger group name
     */
    public RetCode removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName) throws BrokerException {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            if (scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName))) {
                return ConstantsHelper.SUCCESS;
            }
            return ConstantsHelper.FAIL;
        } catch (Exception e) {
            throw new BrokerException(e.getMessage());
        }
    }

}


