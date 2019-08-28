package com.webank.weevent.processor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan(basePackages = "com.webank.weevent.processor")
public class ProcessorApplication {
    public static void main(String[] args) {
        System.out.println("start processor success");
        SpringApplication.run(ProcessorApplication.class, args);
    }
}
