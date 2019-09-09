package com.webank.weevent.jmeter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
@Slf4j
public class JMeterApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
        log.info("Start jmeter success");
    }

}
