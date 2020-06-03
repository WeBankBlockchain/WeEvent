package com.webank.weevent.governance.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
@Setter
@Getter
@Component
@PropertySource(value = "classpath:governance.properties", encoding = "UTF-8")
public class GovernanceConfig {

    @Value("${https.read-timeout:3000}")
    private int readTimeout;

    @Value("${https.connect-timeout:3000}")
    private int connectTimeOut;

    @Value("${http.connect-timeout:3000}")
    private int httpConnectTimeOut;

    // max connect
    @Value("${http.client.max-total:200}")
    private int maxTotal;

    @Value("${http.client.max-per-route:500}")
    private int maxPerRoute;

    @Value("${http.client.connection-request-timeout:3000}")
    private int connectionRequestTimeout;

    @Value("${http.client.connection-timeout:3000}")
    private int connectionTimeout;

    @Value("${http.client.socket-timeout:5000}")
    private int socketTimeout;

    @Value("${jwt.private.secret:PrivateSecret}")
    private String PrivateSecret;

    @Value("${file.transport.path:./logs}")
    private String fileTransportPath;
}
