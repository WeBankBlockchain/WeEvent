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

    @Value("${server.ssl.enabled:false}")
    private String sslEnable;

    @Value("${server.ssl.key-store-password:}")
    private String sslPassword;

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

    @Value("${mqtt.brokerserver.port:8083}")
    private Integer brokerServerPort;

    @Value("${mqtt.brokerserver.sobacklog:511}")
    private Integer soBackLog;

    @Value("${mqtt.brokerserver.sokeepalive:true}")
    private Boolean soKeepAlive;

    @Value("${mqtt.brokerserver.keepalive:60}")
    private Integer keepAlive;

    @Value("${mqtt.websocketserver.path:/weevent/mqtt}")
    private String webSocketServerPath;

    @Value("${mqtt.websocketserver.port:8084}")
    private Integer webSocketPort;

    @Value("${mqtt.user.login:}")
    private String mqttUserName;

    @Value("${mqtt.user.passcode:}")
    private String mqttPassCode;
}
