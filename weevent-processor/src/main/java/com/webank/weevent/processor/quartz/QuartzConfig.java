package com.webank.weevent.processor.quartz;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.quartz.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

    private JobFactory jobFactory;

    public QuartzConfig(JobFactory jobFactory) {
        this.jobFactory = jobFactory;
    }

    /**
     * SchedulerFactoryBean
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        try {
            if (this.getClass().getClassLoader().getResource("processor.properties") != null) {
                String fileUtl = this.getClass().getClassLoader().getResource("processor.properties").getPath();
                FileInputStream in = new FileInputStream(fileUtl);
                Properties quartzPropertie = new Properties();
                quartzPropertie.load(in);
                quartzPropertie.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
                quartzPropertie.setProperty("org.quartz.jobStore.isClustered", "true");
                quartzPropertie.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
                quartzPropertie.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
                quartzPropertie.setProperty("org.quartz.threadPool.makeThreadsDaemons", "true");
                quartzPropertie.setProperty("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");
                quartzPropertie.setProperty("org.quartz.plugin.shutdownHook.cleanShutdown", "true");
                quartzPropertie.setProperty("org.quartz.plugin.shutdownHook.class", "org.quartz.plugins.management.ShutdownHookPlugin");
                quartzPropertie.setProperty("org.quartz.scheduler.instanceId", "auto");
                quartzPropertie.setProperty("org.quartz.scheduler.skipupdateCheck", "auto");
                quartzPropertie.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
                quartzPropertie.setProperty("org.quartz.jobStore.misfireThreshold", "25000");
                quartzPropertie.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");

                factory.setQuartzProperties(quartzPropertie);
                factory.setJobFactory(jobFactory);
                in.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return factory;
    }

    @Bean(name = "scheduler")
    public Scheduler scheduler() {
        return schedulerFactoryBean().getScheduler();
    }
}
