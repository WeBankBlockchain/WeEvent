package com.webank.weevent.processor.job;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QJobs implements Job {

    public void execute(JobExecutionContext context) {
        log.info(context.getJobDetail().getDescription());
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("{},{}Job   executing...", this.toString(), f.format(new Date()));

        String jobName = context.getJobDetail().getKey().getName();
        log.info("{},{} Job execute {}    executing...", this.toString(), jobName, f.format(new Date()));
    }
}