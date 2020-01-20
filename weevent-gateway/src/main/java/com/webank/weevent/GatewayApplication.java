package com.webank.weevent;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * API gateway of WeEvent.
 *
 * @author matthewliu
 * @since 2020/01/20
 */
@Slf4j
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
    public static void setApplicationContext(ApplicationContext applicationContext) {
        GatewayApplication.applicationContext = applicationContext;
    }

    @Autowired
    public static void setEnvironment(Environment environment) {
        GatewayApplication.environment = environment;
    }
}
