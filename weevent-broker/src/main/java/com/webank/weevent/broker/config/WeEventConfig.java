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

    @Value("${mqtt.broker.tcp.port:0}")
    private Integer mqttTcpPort;

    @Value("${mqtt.broker.keepalive:60}")
    private Integer keepAlive;

    @Value("${mqtt.broker.security.ssl:true}")
    private Boolean ssl;
    @Value("${mqtt.broker.security.ssl.client_auth:true}")
    private Boolean clientAuth;
    @Value("${mqtt.broker.security.ssl.ca_cert:ca.crt}")
    private String caCertFile;
    @Value("${mqtt.broker.security.ssl.server_cert:server.crt}")
    private String serverCertFile;
    @Value("${mqtt.broker.security.ssl.server_key:server.key}")
    private String serverKeyFile;
}
