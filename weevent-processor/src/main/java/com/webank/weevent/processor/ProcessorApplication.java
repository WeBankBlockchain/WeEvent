package com.webank.weevent.processor;

import com.webank.weevent.processor.config.ProcessorConfig;
import com.webank.weevent.processor.service.InitRule;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@SpringBootApplication
@MapperScan(basePackages = "com.webank.weevent.processor")
public class ProcessorApplication {
    public static ProcessorConfig processorConfig;

    public static void main(String[] args) {
        log.info("start processor success");
        SpringApplication.run(ProcessorApplication.class, args);
        
    }

    @Bean
    InitRule initRule() {
        return new InitRule();
    }

    @Autowired
    public void setProcessorConfig(ProcessorConfig config) {
        processorConfig = config;
    }


    // daemon thread pool
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
}


