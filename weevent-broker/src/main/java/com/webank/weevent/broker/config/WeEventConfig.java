package com.webank.weevent.broker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.ToString;


/**
 * WeEvent Config that auto loaded by spring ApplicationContext
 *
 * @author matthewliu
 * @version 1.0
 * @since 2019/1/28
 */
@Data
@ToString
@Component
public class WeEventConfig {
	
    @Value("${ip.check.white-list:}")
    private String ipWhiteList;

    @Value("${lru.cache.capacity:65536}")
    private Integer maxCapacity;

    @Value("${stomp.heartbeats:30}")
    private Integer stompHeartbeats;

    @Value("${mqtt.broker.tcp.port:0}")
    private Integer mqttTcpPort;

    @Value("${mqtt.broker.keepalive:60}")
    private Integer keepAlive;
    
}
