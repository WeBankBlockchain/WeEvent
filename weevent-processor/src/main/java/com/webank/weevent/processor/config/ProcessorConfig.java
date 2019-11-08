package com.webank.weevent.processor.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@PropertySource(value = "classpath:processor.properties", encoding = "UTF-8")
public class ProcessorConfig {

    @Value("${org.quartz.scheduler.instanceName:}")
    private String schedulerInstanceName;

    @Value("${quartz.schedule.name:}")
    private String quartzScheduleName;

    @Value("${org.quartz.dataSource.WeEvent_processor.driver:}")
    private String dataBaseDriver;


}