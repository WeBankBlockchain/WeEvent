package com.webank.weevent.processor.quartz;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.cache.CEPRuleCache;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.JsonUtil;
import com.webank.weevent.sdk.BrokerException;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class CRUDJobs implements Job {

    public void execute(JobExecutionContext context) {

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("{},{} Job execute {}    executing...", this.toString(), context.getJobDetail().getKey().getName(), f.format(new Date()));

        // keep the only one.
        if (ProcessorApplication.processorConfig.getSchedulerInstanceName().equals(context.getJobDetail().getJobDataMap().get("instance").toString())) {
            String jobName = context.getJobDetail().getKey().getName();
            String type = context.getJobDetail().getJobDataMap().get("type").toString();
            log.info("{},{} Job execute {}    executing...", this.toString(), jobName, f.format(new Date()));

            switch (type) {
                case "startCEPRule":
                    startCEPRule(context, jobName, "startCEPRule");
                    break;

                case "deleteCEPRuleById":
                    startCEPRule(context, jobName, "deleteCEPRuleById");
                    break;

                case "stopCEPRuleById":
                    startCEPRule(context, jobName, "stopCEPRuleById");
                    break;

                case "updateCEPRuleById":
                    startCEPRule(context, jobName, "updateCEPRuleById");

                    break;

                case "insert":
                    startCEPRule(context, jobName, "insert");
                    break;

                default:
                    log.info("the job name type:{}", type);
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void startCEPRule(JobExecutionContext context, String jobName, String type) {
        Object obj = context.getJobDetail().getJobDataMap().get("rule");
        // ruleMap
        Pair<CEPRule, CEPRule> ruleBak = null;
        if (StringUtils.isEmpty(context.getJobDetail().getJobDataMap().get("ruleBak"))) {
            ruleBak = (Pair<CEPRule, CEPRule>) context.getJobDetail().getJobDataMap().get("ruleBak");
        }
        try {
            if (obj instanceof CEPRule) {
                log.info("{}", (CEPRule) obj);
                CEPRule rule = (CEPRule) obj;
                // check the status,when the status equal 1,then update
                if (1 == rule.getStatus() || 0 == rule.getStatus() || 2 == rule.getStatus()) {
                    CEPRuleCache.updateCEPRule(rule, ruleBak);
                }
                log.info("execute  job: {},rule:{},type:{}", jobName, JsonUtil.toJSONString(obj), type);
            }
        } catch (BrokerException | IOException e) {
            log.info("BrokerException:{}", e.toString());
        }

    }


}
