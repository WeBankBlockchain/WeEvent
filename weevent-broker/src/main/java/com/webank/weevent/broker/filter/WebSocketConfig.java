package com.webank.weevent.broker.filter;


import com.webank.weevent.broker.config.WeEventConfig;
import com.webank.weevent.broker.protocol.mqtt.WebSocketMqtt;
import com.webank.weevent.broker.protocol.stomp.BrokerStomp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * BrokerStomp setup.
 * Support sockjs + stomp.js client.
 * see at https://github.com/sockjs/sockjs-client and https://github.com/stomp-js/stompjs.
 *
 * @author matthewliu
 * @since 2018/12/20
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private WeEventConfig weEventConfig;
    private BrokerStomp brokerStomp;
    private WebSocketMqtt webSocketMqtt;
    // max topic content's length is 10k
    private final int maxSize = 16 * 1024;

    @Autowired
    public void setWeEventConfig(WeEventConfig weEventConfig) {
        this.weEventConfig = weEventConfig;
    }

    @Autowired
    public void setBrokerStomp(BrokerStomp brokerStomp) {
        this.brokerStomp = brokerStomp;
    }

    @Autowired
    public void setWebSocketMqtt(WebSocketMqtt webSocketMqtt) {
        this.webSocketMqtt = webSocketMqtt;
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(maxSize);
        container.setMaxBinaryMessageBufferSize(maxSize);
        return container;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        WebSocketHandShakeInterceptor handShakeWebSocketInterceptor = interceptorWebSocket();

        registry.addHandler(this.brokerStomp, "/sockjs")
                .addInterceptors(handShakeWebSocketInterceptor)
                .setAllowedOrigins("*")
                .withSockJS()
                .setHeartbeatTime(this.weEventConfig.getStompHeartbeats() * 1000);

        registry.addHandler(this.brokerStomp, "/stomp")
                .addInterceptors(handShakeWebSocketInterceptor)
                .setAllowedOrigins("*");

        registry.addHandler(this.webSocketMqtt, "/mqtt")
                .addInterceptors(handShakeWebSocketInterceptor)
                .setAllowedOrigins("*");
    }

    /**
     * check if it is in the white table
     *
     * @return HandShakeWebSocketInterceptor
     */
    private WebSocketHandShakeInterceptor interceptorWebSocket() {
        return new WebSocketHandShakeInterceptor(this.weEventConfig.getIpWhiteList());
    }
}
