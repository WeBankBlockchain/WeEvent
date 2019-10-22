package com.webank.weevent.processor.config;

import java.util.Date;

import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.model.JobConfig;
import com.webank.weevent.processor.utils.SchedulerUtil;

import lombok.extern.slf4j.Slf4j;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SchedulerConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public StdSchedulerFactory stdSchedulerFactory() {
        StdSchedulerFactory stdSchedulerFactory = new StdSchedulerFactory();

        JobConfig config = new JobConfig();
        config.setId((new Date()).toString());
        config.setFullEntity("com.webank.weevent.processor.job.RuleJobs");
        config.setStatus(1);
        config.setUpdateAt(ProcessorApplication.processorConfig.getCronExpression());
        config.setName(ProcessorApplication.processorConfig.getQuartzScheduleName());
        config.setGroupName(ProcessorApplication.processorConfig.getQuartzGroupName());
        config.setCronTime(ProcessorApplication.processorConfig.getCronExpression());
        config.setCepRule(null);
        boolean flag = SchedulerUtil.createScheduler(config, applicationContext);
        log.info("execute：{}", flag);
        return stdSchedulerFactory;
    }

}