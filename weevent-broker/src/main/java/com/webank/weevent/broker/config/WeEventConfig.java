package com.webank.weevent.broker.config;

import lombok.Data;
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
@Data
@Component
@PropertySource(value = "classpath:weevent.properties", encoding = "UTF-8")
public class WeEventConfig {
    @Value("${ip.check.white-table:}")
    private String ipWhiteTable;

    @Value("${redis.server.ip:}")
    private String redisServerIp;

    @Value("${redis.server.password:}")
    private String redisServerPassword;

    @Value("${redis.server.port:6379}")
    private Integer redisServerPort;

    @Value("${lru.cache.capacity:65536}")
    private Integer maxCapacity;

    @Value("${restful.subscribe.callback.timeout:5000}")
    private Integer restful_timeout;

    @Value("${broker.zookeeper.ip:}")
    private String zookeeperIp;

    @Value("${broker.zookeeper.path:}")
    private String zookeeperPath;

    @Value("${broker.zookeeper.timeout:3000}")
    private Integer zookeeperTimeout;

    @Value("${stomp.user.login:}")
    private String stompLogin;

    @Value("${stomp.user.passcode:}")
    private String stompPasscode;

    @Value("${stomp.heartbeats:30}")
    private Integer stompHeartbeats;

    @Value("${mqtt.broker.port:8083}")
    private Integer brokerServerPort;

    @Value("${mqtt.broker.sobacklog:511}")
    private Integer soBackLog;

    @Value("${mqtt.broker.sokeepalive:true}")
    private Boolean soKeepAlive;

    @Value("${mqtt.broker.keepalive:60}")
    private Integer keepAlive;

    @Value("${mqtt.websocket.path:/weevent/mqtt}")
    private String webSocketServerPath;

    @Value("${mqtt.websocket.port:8084}")
    private Integer webSocketPort;

    @Value("${mqtt.user.login:}")
    private String mqttUserName;

    @Value("${mqtt.user.passcode:}")
    private String mqttPassCode;
}
