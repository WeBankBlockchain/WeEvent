package com.webank.weevent.processor;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
@MapperScan(basePackages = "com.webank.weevent.processor")
public class ProcessorApplication {
    public static void main(String[] args) {
        log.info("start processor success");
        SpringApplication.run(ProcessorApplication.class, args);
    }
}
