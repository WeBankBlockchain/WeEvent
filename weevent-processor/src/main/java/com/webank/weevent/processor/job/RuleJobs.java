package com.webank.weevent.processor.job;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.webank.weevent.processor.service.CEPRuleServiceImpl;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RuleJobs implements Job {

    public void execute(JobExecutionContext context) {
        log.info(context.getJobDetail().getDescription());
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String jobName = context.getJobDetail().getKey().getName();
        context.getJobDetail().getKey().getGroup();
        log.info("{},{} Job execute {}    executing...", this.toString(), jobName, f.format(new Date()));
        log.info("insert rule{}",context.getJobDetail().getJobDataMap().get("insert"));

    }
}
