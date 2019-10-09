package com.webank.weevent.processor.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@PropertySource(value = "classpath:processor.properties", encoding = "UTF-8")
public class ProcessorConfig {
    //    @Value("${ip.check.white-table:}")
//    private String ipWhiteTable;
    @Value("${redis.server.ip:}")
    private String redisServerIp;

    @Value("${redis.server.password:}")
    private String redisServerPassword;

    @Value("${redis.server.port:6379}")
    private Integer redisServerPort;
}