package com.webank.weevent.processor.service;

import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class QuartzManager {

    private static Scheduler scheduler;

    public QuartzManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * add a job
     *
     * @param jobName job name
     * @param jobGroupName job group name
     * @param triggerName trigger name
     * @param triggerGroupName trigger group name
     * @param jobClass job class
     * @param cron time quartz
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName, Class jobClass, String cron, Map<String, Object> params) {
        try {
            JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            job.getJobDataMap().putAll(params);

            TriggerBuilder<Trigger> triggerBuilder = newTrigger();
            triggerBuilder.withIdentity(triggerName, triggerGroupName);
            triggerBuilder.startNow();
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            CronTrigger trigger = (CronTrigger) triggerBuilder.build();

            scheduler.scheduleJob(job, trigger);

            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * add job and modify
     *
     * @param jobName job name
     * @param jobGroupName job group name
     * @param triggerName trigger name
     * @param triggerGroupName trigger group name
     * @param jobClass job class
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addModifyJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName, Class jobClass, JobDataMap params) {
        try {

            JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).setJobData(params).requestRecovery(true).build();


//            TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroupName);
//            SimpleScheduleBuilder simpleBuilder = simpleSchedule().withRepeatCount(0);
//
//            Trigger trigger = newTrigger()
//                    .withIdentity(triggerKey).startNow()
//                    .withSchedule(simpleBuilder)
//                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity("trigger3", "group1")
                    .startNow()  // if a start time is not given (if this line were omitted), "now" is implied
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(10)
                            .withRepeatCount(10)) // note that 10 repeats will give a total of 11 firings
                    .build();

            scheduler.scheduleJob(job, trigger);
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * modify Job Time
     *
     * @param triggerName trigger name
     * @param triggerGroupName trigger group name
     * @param cron set time
     */
    public void modifyJobTime(String triggerName, String triggerGroupName, String cron) {
        try {

            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }

            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(cron)) {
                TriggerBuilder<Trigger> triggerBuilder = newTrigger();
                triggerBuilder.withIdentity(triggerName, triggerGroupName);
                triggerBuilder.startNow();
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
                trigger = (CronTrigger) triggerBuilder.build();
                scheduler.rescheduleJob(triggerKey, trigger);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * modify Job Time
     *
     * @param jobName job name
     * @param jobGroupName job group name
     */
    public JobDetail getJobDetail(String jobName, String jobGroupName) {
        try {
            return scheduler.getJobDetail(new JobKey(jobName, jobGroupName));
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public void removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName) {
        try {

            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);

            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get the job
     * STATE_BLOCKED 4
     * STATE_COMPLETE 2
     * STATE_ERROR 3
     * STATE_NONE -1
     * STATE_NORMAL 0
     * STATE_PAUSED 1
     */
    public Boolean notExists(String triggerName, String triggerGroupName) {
        try {
            return scheduler.getTriggerState(TriggerKey.triggerKey(triggerName, triggerGroupName)) == Trigger.TriggerState.NONE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
