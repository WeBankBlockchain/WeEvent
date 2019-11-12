package com.webank.weevent.processor;

import com.webank.weevent.processor.cache.CEPRuleCache;
import com.webank.weevent.processor.config.ProcessorConfig;
import com.webank.weevent.processor.mq.CEPRuleMQ;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@SpringBootApplication
@MapperScan(basePackages = "com.webank.weevent.processor")
public class ProcessorApplication {
    public static ProcessorConfig processorConfig;
    public static ApplicationContext applicationContext;
    @Autowired
    public void setContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static void main(String[] args) {
        log.info("start processor success");
        SpringApplication app = new SpringApplication(ProcessorApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
        log.info("start processor success");
    }

    @Autowired
    public void setProcessorConfig(ProcessorConfig config) {
        processorConfig = config;
    }

    private static void exit() {
        if (applicationContext != null) {
            System.exit(SpringApplication.exit(applicationContext));
        } else {
            System.exit(1);
        }
    }


    @Bean(name = "processor_daemon_task_executor")
    public static ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setThreadNamePrefix("processor_daemon_");
        // run in thread immediately, no blocking queue
        pool.setQueueCapacity(0);
        pool.setDaemon(true);
        pool.initialize();

        log.info("init processor daemon thread pool");
        return pool;
    }

    @Bean
    public CEPRuleCache cEPRuleCache() {
        log.info("cEPRuleCache....");
        return new CEPRuleCache();
    }

    @Bean
    public CEPRuleMQ cEPRuleMQ() {
        log.info("CEPRuleMQ....");
        return new CEPRuleMQ();
    }
}


