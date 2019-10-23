package com.webank.weevent.processor.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.webank.weevent.processor.cache.CEPRuleCache;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.mq.CEPRuleMQ;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CRUDJobs implements Job {

    public void execute(JobExecutionContext context) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String jobName = context.getJobDetail().getKey().getName();
        String type = context.getJobDetail().getJobDataMap().get("type").toString();
        log.info("{},{} Job execute {}    executing...", this.toString(), jobName, f.format(new Date()));

        switch (type) {
            case "startCEPRule":
                startCEPRule(context, jobName);
                break;

            case "onEvent":
                handleWeEvent(context);
                log.info("onEvent:{}", jobName);
                break;

            default:
                log.info("the job name type:{}", type);
                break;
        }
    }

    private void handleWeEvent(JobExecutionContext context) {
        WeEvent event = (WeEvent) context.getJobDetail().getJobDataMap().get("weevent");
        IWeEventClient client = (IWeEventClient) context.getJobDetail().getJobDataMap().get("client");
        Map<String, CEPRule> ruleMap = (Map<String, CEPRule>) context.getJobDetail().getJobDataMap().get("ruleMap");
        log.info("startCEPRule in job: {},rule:{}", JSONObject.toJSON(event));

        CEPRuleMQ.handleOnEvent(event, client, ruleMap);
    }


    private void startCEPRule(JobExecutionContext context, String jobName) {
        Object obj = context.getJobDetail().getJobDataMap().get("rule");
        try {
            if (obj instanceof CEPRule) {
                log.info("{}", (CEPRule) obj);
                CEPRule rule = (CEPRule) obj;
                // check the json ，update the rule map，subscribe
                CEPRuleCache.updateCEPRule(rule);
                log.info("startCEPRule in job: {},rule:{}", jobName, JSONObject.toJSON(obj));

            }
        }catch (BrokerException e){
            log.info("BrokerException:{}",e.toString());
        }

    }
}
