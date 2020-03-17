package com.webank.weevent.broker.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


/**
 * WeEvent Config that auto loaded by spring ApplicationContext
 *
 * @author matthewliu
 * @version 1.0
 * @since 2019/1/28
 */
@Slf4j
@Getter
@Setter
@ToString
@Component
@PropertySource(value = "classpath:weevent.properties", encoding = "UTF-8")
public class WeEventConfig {
    @Value("${ip.check.white-list:}")
    private String ipWhiteList;

    @Value("${lru.cache.capacity:65536}")
    private Integer maxCapacity;

    @Value("${stomp.heartbeats:30}")
    private Integer stompHeartbeats;

    @Value("${mqtt.broker.port:0}")
    private Integer mqttPort;

    @Value("${mqtt.broker.tcp.port:0}")
    private Integer mqttTcpPort;

    @Value("${mqtt.broker.sobacklog:511}")
    private Integer soBackLog;

    @Value("${mqtt.broker.sokeepalive:true}")
    private Boolean soKeepAlive;

    @Value("${mqtt.broker.keepalive:60}")
    private Integer keepAlive;

    @Value("${mqtt.broker.path:}")
    private String mqttServerPath;

    @Value("${file.path:./logs/file}")
    private String filePath;

    @Value("${file.chunk.size:1048576}")
    private int fileChunkSize;
}
