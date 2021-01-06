package com.webank.weevent.governance.common;

import lombok.Data;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;

@Data
@Component
@NacosPropertySource(dataId = "governance.properties", autoRefreshed = false)
public class GovernanceConfig {

    @NacosValue(value = "${https.read-timeout:3000}", autoRefreshed = true)
    private int readTimeout;

    @NacosValue(value = "${https.connect-timeout:3000}", autoRefreshed = true)
    private int connectTimeOut;

    @NacosValue(value = "${http.connect-timeout:3000}", autoRefreshed = true)
    private int httpConnectTimeOut;

    // max connect
    @NacosValue(value = "${http.client.max-total:200}", autoRefreshed = true)
    private int maxTotal;

    @NacosValue(value = "${http.client.max-per-route:500}", autoRefreshed = true)
    private int maxPerRoute;

    @NacosValue(value = "${http.client.connection-request-timeout:3000}", autoRefreshed = true)
    private int connectionRequestTimeout;

    @NacosValue(value = "${http.client.connection-timeout:3000}", autoRefreshed = true)
    private int connectionTimeout;

    @NacosValue(value = "${http.client.socket-timeout:5000}", autoRefreshed = true)
    private int socketTimeout;

    @NacosValue(value = "${jwt.private.secret:PrivateSecret}", autoRefreshed = true)
    private String PrivateSecret;

    @NacosValue(value = "${file.transport.path:./logs}", autoRefreshed = true)
    private String fileTransportPath;
    
    @NacosValue(value = "${acount.name}", autoRefreshed = true)
    private String acountName;
    
    @NacosValue(value = "${acount.passwrod}", autoRefreshed = true)
    private String acountPasswrod;
    
    public static String acount_name;
    public static String acount_passwrod;
    
    @PostConstruct
    private void init() {
    	acount_name = acountName;
    	acount_passwrod = acountPasswrod;
    }

}
