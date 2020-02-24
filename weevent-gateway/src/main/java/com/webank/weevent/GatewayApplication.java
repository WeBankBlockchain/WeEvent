package com.webank.weevent;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;

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
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GatewayApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run();
    }

    @Bean
    public static GlobalFilter getLogGlobalFilter() {
        return new LogGlobalFilter();
    }
}
