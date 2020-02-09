package com.webank.weevent;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * API gateway of WeEvent.
 *
 * @author matthewliu
 * @since 2020/01/20
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {
    public static ApplicationContext applicationContext;

    public static Environment environment;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GatewayApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run();
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        GatewayApplication.applicationContext = applicationContext;
    }

    @Autowired
    public void setEnvironment(Environment environment) {
        GatewayApplication.environment = environment;
    }

    @Bean
    public static GlobalFilter getLogGlobalFilter() {
        return new LogGlobalFilter();
    }
}
