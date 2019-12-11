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

    @Value("${lru.cache.capacity:65536}")
    private Integer maxCapacity;

    @Value("${cgi.subscribe.notify.timeout:5000}")
    private Integer cgi_notify_timeout;

    @Value("${broker.blockchain.type}")
    private String blockChainType;

    @Value("${broker.zookeeper.ip:}")
    private String zookeeperIp;

    @Value("${broker.zookeeper.path:}")
    private String zookeeperPath;

    @Value("${broker.zookeeper.timeout:3000}")
    private Integer zookeeperTimeout;

    @Value("${stomp.heartbeats:30}")
    private Integer stompHeartbeats;

    @Value("${mqtt.broker.port:7001}")
    private Integer brokerServerPort;

    @Value("${mqtt.websocket.port:7002}")
    private Integer webSocketPort;

    @Value("${mqtt.broker.sobacklog:511}")
    private Integer soBackLog;

    @Value("${mqtt.broker.sokeepalive:true}")
    private Boolean soKeepAlive;

    @Value("${mqtt.broker.keepalive:60}")
    private Integer keepAlive;

    @Value("${mqtt.websocket.path:/weevent/mqtt}")
    private String webSocketServerPath;

}
