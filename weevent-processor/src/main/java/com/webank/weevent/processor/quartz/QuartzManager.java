package com.webank.weevent.processor.quartz;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import javax.annotation.PostConstruct;

import com.webank.weevent.processor.cache.CEPRuleCache;
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
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
@Service
public class QuartzManager {

    private static Scheduler scheduler;

    @PostConstruct
    public void init() {
        try {
            // get all rule
            Iterator<JobKey> it = this.scheduler.getJobKeys(GroupMatcher.anyGroup()).iterator();
            Map<String, CEPRule> ruleMap = new HashMap<>();
            while (it.hasNext()) {
                JobKey jobKey = (JobKey) it.next();
                if (null != (CEPRule) scheduler.getJobDetail(jobKey).getJobDataMap().get("rule")) {
                    CEPRule rule = (CEPRule) scheduler.getJobDetail(jobKey).getJobDataMap().get("rule");
                    // if the current is delete
                    ruleMap.put(rule.getId(), rule);
                    log.info("{}", jobKey);
                    if (rule.getStatus().equals(1)) {
                        ruleMap.put(rule.getId(), rule);
                    }
                }

            }
            log.info("rule size:{} ...", ruleMap.size());
            log.info("start subscribe ...");
            for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
                CEPRuleCache.updateCEPRule(entry.getValue(), ruleMap);
            }

        } catch (Exception e) {
            log.error("e:{}", e.toString());
        }
    }

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
            // get the all rules
            Iterator<JobKey> it = scheduler.getJobKeys(GroupMatcher.anyGroup()).iterator();
            List<CEPRule> ruleList = new ArrayList<>();
            Map<String, CEPRule> ruleMap = new HashMap<>();
            CEPRule currentRule = (CEPRule) params.get("rule");
            while (it.hasNext()) {
                JobKey jobKey = (JobKey) it.next();
                if (null != (CEPRule) scheduler.getJobDetail(jobKey).getJobDataMap().get("rule")) {
                    CEPRule rule = (CEPRule) scheduler.getJobDetail(jobKey).getJobDataMap().get("rule");
                    // if the current is delete
                    if ((("deleteCEPRuleById".equals(params.get("type").toString()))) && (jobName.equals(rule.getId()))) {
                        // update the delete status
                        rule.setStatus(2);
                        params.put("rule", rule);
                        currentRule = rule;
                    } else {
                        ruleList.add(rule);
                        ruleMap.put(rule.getId(), rule);
                        log.info("{}", jobKey);
                    }
                }

            }
            ruleMap.put(currentRule.getId(), currentRule);
            ruleList.add(currentRule);
            params.put("ruleMap", ruleMap);

            log.info("update the job  ruleMap:{},ruleList:{}", ruleMap.size(), ruleList.size());
            JobDetail job = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).setJobData(params).requestRecovery(true).storeDurably(true).build();

            // just do one time
            SimpleTrigger trigger = newTrigger()
                    .withIdentity(new Date().toString().concat(currentRule.getId()), triggerGroupName)
                    .startNow()
                    .withSchedule(simpleSchedule())
                    .forJob(jobName, jobGroupName)
                    .build();

            if (scheduler.checkExists(JobKey.jobKey(jobName, jobGroupName))) {
                removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
            }
            scheduler.scheduleJob(job, trigger);
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
            if (scheduler.checkExists(JobKey.jobKey(jobName, jobGroupName))) {
                log.info("update job:{} success", jobName);

                return ConstantsHelper.SUCCESS;
            }

            return ConstantsHelper.FAIL;
        } catch (Exception e) {
            log.error("e:{}", e.toString());
            return RetCode.mark(0, e.toString());
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
