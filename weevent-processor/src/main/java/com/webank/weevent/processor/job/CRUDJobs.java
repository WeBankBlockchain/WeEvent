package com.webank.weevent.processor.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.mq.CEPRuleMQ;
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
        log.info(context.getJobDetail().getDescription());
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String jobName = context.getJobDetail().getKey().getName();
        String type = context.getJobDetail().getJobDataMap().get("type").toString();
        log.info("{},{} Job execute {}    executing...", this.toString(), jobName, f.format(new Date()));

        switch (type) {
            case "insert":
                getDetail(context, jobName);
                break;

            case "insertByParam":
                getDetail(context, jobName);
                break;

            case "deleteCEPRuleById":
                getDetail(context, jobName);
                break;

            case "startCEPRule":
                getDetail(context, jobName);
                break;

            case "updateCEPRuleById":
                getDetail(context, jobName);
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
        CEPRuleMQ.handleOnEvent(event, client, ruleMap);
    }

    private void getDetail(JobExecutionContext context, String jobName) {
        String id = context.getJobDetail().getJobDataMap().get("id").toString();
        Object obj = context.getJobDetail().getJobDataMap().get("rule");
        if (obj instanceof CEPRule) {
            log.info("{}", (CEPRule) obj);
        }
        log.info("startCEPRule in job: {},id:{},rule:{}", jobName, id, JSONObject.toJSON(obj));
    }
}
