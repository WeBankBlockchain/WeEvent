package com.webank.weevent.processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Component
@PropertySource(value = "classpath:processor.properties", ignoreResourceNotFound = true, encoding = "UTF-8")
public class ProcessorConfig {

    @Value("${org.quartz.scheduler.instanceName:}")
    private String schedulerInstanceName;
    
}