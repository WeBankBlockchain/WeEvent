package com.webank.weevent.broker.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
@Data
@Component
@PropertySource(value = "classpath:weevent.properties", encoding = "UTF-8")
public class WeEventConfig {
    @Value("${fisco.topic-controller.contract-address}")
    private String topicControllerAddress;

    @Value("${server.port}")
    private String serverPort;

    @Value("server.ssl.enabled")
    private String  sslEnable;

    @Value("${ip.check.white-table}")
    private String ipWhiteTable;
    /**
     * Consumer.EventDetectLoop's helper thread number.
     */
    private Integer consumerHelperThreadNum;

    /**
     * Idle time in FiscoBcosBroker4Consumer, ms.
     */
    private Integer consumerIdleTime;

    /**
     * Redis Server Ip
     */
    @Value("${redis.server.ip:}")
    private String redisServerIp;

    /**
     * Redis Server Password
     */
    @Value("${redis.server.password:}")
    private String redisServerPassword;

    /**
     * Redis Server Port
     */
    @Value("${redis.server.port:}")
    private Integer redisServerPort;
    
    @Value("${lru.cache.capacity:65536}")
    private Integer maxCapacity;

    @Value("${restful.subscribe.callback.timeout}")
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


    @Value("${fisco.consumer.helper-thread-num:10}")
    public void setConsumerHelperThreadNum(Integer consumerHelperThreadNum) {
        this.consumerHelperThreadNum = consumerHelperThreadNum;
        if (this.consumerHelperThreadNum < 10) {
            this.consumerHelperThreadNum = 10;
        }
    }

    @Value("${fisco.consumer.idle-time:1000}")
    public void setConsumerIdleTime(Integer consumerIdleTime) {
        this.consumerIdleTime = consumerIdleTime;
        if (this.consumerIdleTime < 1000) {
            this.consumerIdleTime = 1000;
        }
    }
}
