package com.webank.weevent.jemeter.configuration;

import java.net.URL;

import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JMeterConfiguration {


    @Value("${weevent.broker.url:(127.0.0.1:8080)}")
    private String url;

    private final static String HTTP_HEADER = "http://";


    @Bean
    public IWeEventClient weEventClient() throws Exception {
        String jsonurl = HTTP_HEADER + url + "/weevent";
        return IWeEventClient.build(jsonurl);
    }

    @Bean
    public IBrokerRpc brokerRpc() throws Exception {
        String jsonRpcUrl = HTTP_HEADER + url + "/weevent/jsonrpc";
        URL url = new URL(jsonRpcUrl);
        JsonRpcHttpClient client = new JsonRpcHttpClient(url);
        return ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
    }

}


