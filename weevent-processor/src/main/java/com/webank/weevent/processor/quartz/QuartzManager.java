package com.webank.weevent.processor.quartz;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.processor.cache.CEPRuleCache;
import com.webank.weevent.processor.enums.RuleStatusEnum;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.StatisticRule;
import com.webank.weevent.processor.model.StatisticWeEvent;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.RetCode;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
            Iterator<JobKey> it = this.scheduler.getJobKeys(GroupMatcher.groupEquals("rule")).iterator();
            Map<String, CEPRule> ruleMap = new HashMap<>();
            while (it.hasNext()) {
                JobKey jobKey = it.next();
                if (null != scheduler.getJobDetail(jobKey).getJobDataMap().get("rule")) {
                    CEPRule rule = JsonHelper.json2Object(scheduler.getJobDetail(jobKey).getJobDataMap().get("rule").toString(), CEPRule.class);
                    // if the current is delete
                    ruleMap.put(rule.getId(), rule);
                    log.info("{}", jobKey);
                    if (RuleStatusEnum.RUNNING.getCode().equals(rule.getStatus())) {
                        ruleMap.put(rule.getId(), rule);
                    }
                }

            }
            log.info("rule size:{} ...", ruleMap.size());
            log.info("start subscribe ...");
            for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
                CEPRuleCache.updateCEPRule(entry.getValue(), null);
            }

        } catch (SchedulerException | BrokerException e) {
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
            Iterator<JobKey> it = scheduler.getJobKeys(GroupMatcher.groupEquals(jobGroupName)).iterator();
            CEPRule currentRule = (CEPRule) params.get("rule");
            params.put("rule", currentRule == null ? null : JsonHelper.object2Json(currentRule));
            while (it.hasNext()) {
                JobKey jobKey = it.next();
                if (null != scheduler.getJobDetail(jobKey).getJobDataMap().get("rule")) {
                    CEPRule rule = JsonHelper.json2Object(scheduler.getJobDetail(jobKey).getJobDataMap().get("rule").toString(), CEPRule.class);
                    // if the current is delete
                    if ("deleteCEPRuleById".equals(params.get("type").toString()) && jobName.equals(rule.getId())) {
                        // update the delete status
                        rule.setStatus(RuleStatusEnum.IS_DELETED.getCode());
                        currentRule = rule;
                        params.put("rule", JsonHelper.object2Json(rule));
                    }
                }
            }

            Pair<CEPRule, CEPRule> ruleBak = null;
            if (scheduler.checkExists(JobKey.jobKey(jobName, jobGroupName))) {
                // add the old one
                ruleBak = new Pair<>(getJobDetail(jobName), currentRule);
            }
            // add latest one
            params.put("ruleBak", ruleBak == null ? null : JsonHelper.object2Json(ruleBak));

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
            // check status
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
     * get Job list
     **/
    public static Map<String, CEPRule> getJobList() throws SchedulerException, BrokerException {
        Iterator<JobKey> it = scheduler.getJobKeys(GroupMatcher.anyGroup()).iterator();
        Map<String, CEPRule> ruleMap = new HashMap<>();
        while (it.hasNext()) {
            JobKey jobKey = it.next();
            CEPRule rule = JsonHelper.json2Object(String.valueOf(scheduler.getJobDetail(jobKey).getJobDataMap().get("rule")), CEPRule.class);
            if (null != rule && RuleStatusEnum.RUNNING.getCode().equals(rule.getStatus())) {
                ruleMap.put(rule.getId(), rule);
            }
        }
        log.info("ruleMap:{}", ruleMap.size());
        return ruleMap;
    }


    /**
     * remove
     *
     * @param jobName remove job
     * @param jobGroupName job group name
     * @param triggerName trigger name
     * @param triggerGroupName trigger group name
     */
    public RetCode removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);

        scheduler.pauseTrigger(triggerKey);
        scheduler.unscheduleJob(triggerKey);
        if (scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName))) {
            return ConstantsHelper.SUCCESS;
        }
        return ConstantsHelper.FAIL;
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

    /**
     * get the job message
     *
     * @param jobName
     * @return
     */
    public CEPRule getJobDetail(String jobName) throws SchedulerException, BrokerException {
        JobDetail job = scheduler.getJobDetail(new JobKey(jobName, "rule"));
        if (!StringUtils.isEmpty(job.getJobDataMap().get("rule"))) {
            return JsonHelper.json2Object(job.getJobDataMap().get("rule").toString(), CEPRule.class);
        }
        return null;
    }

    /**
     * get the statistic jobs
     */
    public StatisticWeEvent getStatisticJobs(StatisticWeEvent statisticWeEvent, List<String> idList) throws SchedulerException, BrokerException {
        Map<String, StatisticRule> statisticRuleMap = statisticWeEvent.getStatisticRuleMap();

        // get the all rules
        Iterator<JobKey> it = scheduler.getJobKeys(GroupMatcher.anyGroup()).iterator();

        int systemAmount = 0;
        int userAmount = 0;
        int runAmount = 0;

        while (it.hasNext()) {
            JobKey jobKey = it.next();
            if (null != scheduler.getJobDetail(jobKey).getJobDataMap().get("rule")) {
                CEPRule rule = JsonHelper.json2Object(scheduler.getJobDetail(jobKey).getJobDataMap().get("rule").toString(), CEPRule.class);

                // statistic
                if ("1".equals(rule.getSystemTag())) {
                    systemAmount = systemAmount + 1;
                } else {
                    userAmount = userAmount + 1;
                }
                if ("1".equals(rule.getStatus())) {
                    runAmount = runAmount + 1;
                }

                // match the right rule
                if ((!statisticRuleMap.containsKey(rule.getId())) && idList.contains(rule.getId())) {
                    StatisticRule statisticRule = new StatisticRule();
                    statisticRule.setId(rule.getId());
                    statisticRule.setBrokerId(rule.getBrokerId());
                    statisticRule.setRuleName(rule.getRuleName());
                    statisticRule.setStatus(rule.getStatus());
                    statisticRule.setStartTime(rule.getCreatedTime());
                    statisticRule.setDestinationType(rule.getConditionType());
                    statisticRuleMap.put(rule.getId(), statisticRule);
                }
            }
        }
        statisticWeEvent.setSystemAmount(systemAmount);
        statisticWeEvent.setUserAmount(userAmount);
        statisticWeEvent.setRunAmount(runAmount);
        statisticWeEvent.setStatisticRuleMap(statisticRuleMap);
        return statisticWeEvent;
    }
}
