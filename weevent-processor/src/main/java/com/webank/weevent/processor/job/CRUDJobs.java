package com.webank.weevent.processor.job;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.service.CEPRuleServiceImpl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CRUDJobs implements Job {

    @Autowired
    private CEPRuleServiceImpl cepRuleService;

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

            case "selectByPage":

                break;

            default:
                log.info("the job name unknow:{}", type);

        }
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
