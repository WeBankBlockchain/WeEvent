package com.webank.weevent.processor.quartz;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.RetCode;

import lombok.extern.slf4j.Slf4j;
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
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
@Service
public class QuartzManager {

    private static Scheduler scheduler;

    public QuartzManager(Scheduler scheduler) {
        this.scheduler = scheduler;
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
    public RetCode addModifyJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName, Class jobClass, JobDataMap params) {
        try {
            // get the whole rules
            Iterator<JobKey> it = scheduler.getJobKeys(GroupMatcher.anyGroup()).iterator();
            List<CEPRule> ruleList = new ArrayList<>();
            Map<String, CEPRule> ruleMap = new HashMap<>();

            while (it.hasNext()) {
                JobKey jobKey = (JobKey) it.next();
                if (null != (CEPRule) scheduler.getJobDetail(jobKey).getJobDataMap().get("rule")) {
                    CEPRule rule = (CEPRule) scheduler.getJobDetail(jobKey).getJobDataMap().get("rule");
                    ruleList.add(rule);
                    ruleMap.put(rule.getId(), rule);
                    log.info("{}", jobKey);
                }

            }
            // add current rule
            CEPRule currentRule = (CEPRule) params.get("rule");
            ruleMap.put(currentRule.getId(), currentRule);
            ruleList.add(currentRule);
            params.put("ruleMap", ruleMap);

            JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).setJobData(params).requestRecovery(true).build();

            // just do one time
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            triggerBuilder.withIdentity(new Date().toString(), triggerGroupName);
            triggerBuilder.startNow();
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(ProcessorApplication.processorConfig.getCronExpression()));
            CronTrigger trigger = (CronTrigger) triggerBuilder.build();


            scheduler.scheduleJob(job, trigger);
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            if (scheduler.checkExists(JobKey.jobKey(jobName, jobGroupName))) {
                return ConstantsHelper.SUCCESS;
            }

            return ConstantsHelper.FAIL;
        } catch (Exception e) {
            log.error("e:{}", e.toString());
            return ConstantsHelper.FAIL;
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
    public RetCode removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName) {
        try {

            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);

            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            if (scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName))) {
                return ConstantsHelper.SUCCESS;
            }
            return ConstantsHelper.FAIL;
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
            log.error("Exception:{}:", e.toString());
            return false;
        }
    }

}
