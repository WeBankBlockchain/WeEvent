package com.webank.weevent.processor.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.RetCode;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
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
            TimerScheduler currentTimer = (TimerScheduler) params.get("timer");
            while (jobKeyIterator.hasNext()) {
                JobKey jobKey = jobKeyIterator.next();
                if (null != scheduler.getJobDetail(jobKey).getJobDataMap().get("timer")) {
                    TimerScheduler timer = (TimerScheduler) scheduler.getJobDetail(jobKey).getJobDataMap().get("timer");
                    // if the current is delete
                    timerSchedulerList.add(timer);
                    timerSchedulerMap.put(timer.getSchedulerName(), timer);
                }
            }
            timerSchedulerMap.put(currentTimer.getSchedulerName(), currentTimer);
            timerSchedulerList.add(currentTimer);
            params.put("timerMap", timerSchedulerMap);
            log.info("update the timer timerMap:{},ruleList:{}", timerSchedulerList.size(), timerSchedulerList.size());

            JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).setJobData(params).requestRecovery(true).storeDurably(true).build();

            //
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(new Date().toString().concat(currentTimer.getSchedulerName()), triggerGroupName)
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(timerScheduler.getPeriodParams())).forJob(jobName, jobGroupName).build();

            if (scheduler.checkExists(JobKey.jobKey(jobName, jobGroupName))) {
                removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
            }
            scheduler.scheduleJob(job, trigger);

            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            if (scheduler.checkExists(JobKey.jobKey(jobName, jobGroupName))) {
                log.info("deal timer task:{} success", jobName);
                return ConstantsHelper.SUCCESS;
            }
            return ConstantsHelper.FAIL;
        } catch (Exception e) {
            log.error("e:{}", e.toString());
            return RetCode.mark(0, e.toString());
        }
    }


    @Transactional(rollbackFor = Throwable.class)
    public void deleteTimerScheduler(TimerScheduler timerScheduler) throws BrokerException {
        try {
            this.removeJob(timerScheduler.getSchedulerName(), "timer", "timer", "timer-trigger");
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


