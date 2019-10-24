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

    @Value("${quartz.schedule.groupname:}")
    private String quartzGroupName;

    @Value("${quartz.schedule.cronexpression:}")
    private String cronExpression;

    @Value("${org.quartz.scheduler.instanceId:}")
    private String schedulerInstanceId;

    @Value("${org.quartz.dataSource.cep.driver:}")
    private String dataSourceDriver;

    @Value("${org.quartz.dataSource.cep.URL:}")
    private String dataSourceURL;

    @Value("${org.quartz.dataSource.cep.user:}")
    private String dataSourceUser;

    @Value("${org.quartz.dataSource.cep.password:}")
    private String dataSourcePassword;

    @Value("${org.quartz.dataSource.cep.maxConnection:}")
    private String dataSourceMConnection;

    @Value("${org.quartz.jobStore.class:}")
    private String jobStoreClass;

    @Value("${org.quartz.jobStore.driverDelegateClass:}")
    private String driverDelegateClass;

    @Value("${org.quartz.jobStore.dataSource:}")
    private String jobStoreDataSource;

    @Value("${org.quartz.jobStore.misfireThreshold:}")
    private String misfireThreshold;

    @Value("${org.quartz.jobStore.isClustereds:}")
    private String isClustereds;

    @Value("${org.quartz.jobStore.clusterCheckinInterval:}")
    private String clusterCheckinInterval;

    @Value("${org.quartz.threadPool.class:}")
    private String threadPool;

    @Value("${org.quartz.threadPool.makeThreadsDaemons:}")
    private String makeThreadsDaemons;

    @Value("${org.quartz.threadPool.threadCount:}")
    private String threadCount;

    @Value("${org.quartz.threadPool.threadPriority:}")
    private String threadPriority;

}