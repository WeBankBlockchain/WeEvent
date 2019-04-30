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
    @Value("${server.port:8081}")
    private String serverPort;

    @Value("server.ssl.enabled:false")
    private String sslEnable;

    @Value("${ip.check.white-table:}")
    private String ipWhiteTable;

    @Value("${consumer.helper-thread-num:10}")
    private Integer consumerHelperThreadNum;

    @Value("${consumer.idle-time:1000}")
    private Integer consumerIdleTime;

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

    @Value("${mqtt.broker.url:}")
    private String mqttBrokerUrl;

    @Value("${mqtt.broker.user:}")
    private String mqttBrokerUser;

    @Value("${mqtt.broker.password:}")
    private String mqttBrokerPassword;

    @Value("${mqtt.broker.qos:2}")
    private Integer mqttBrokerQos;

    @Value("${mqtt.broker.keep-alive:15}")
    private Integer mqttBrokerKeepAlive;

    @Value("${mqtt.broker.timeout:5000}")
    private Integer mqttBrokerTimeout;

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
}
