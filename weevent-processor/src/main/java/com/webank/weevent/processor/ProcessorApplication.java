package com.webank.weevent.processor;


import com.webank.weevent.processor.cache.CEPRuleCache;
import com.webank.weevent.processor.config.ProcessorConfig;
import com.webank.weevent.processor.mq.CEPRuleMQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class ProcessorApplication {
    public static ProcessorConfig processorConfig;
    public static ApplicationContext applicationContext;
    public static Environment environment;
    public static DiscoveryClient discoveryClient;
    public static RestTemplate restTemplate;


    @Autowired
    public void setContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static void main(String[] args) throws Exception {
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

    @Autowired
    public void setEnvironment(Environment env) {
        environment = env;
    }


    @Autowired(required = false)
    public void setDiscoveryClient(DiscoveryClient client) {
        discoveryClient = client;
    }

    @Autowired
    public void setRestTemplate(RestTemplate template) {
        restTemplate = template;
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

    @Bean
    public RestTemplate restTemplate() {
        log.info("RestTemplate....");
        return new RestTemplate();
    }

}