package com.webank.weevent.protocol.stomp;


import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.config.WeEventConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@Configuration
@EnableWebSocket
@Slf4j
public class StompConfig implements WebSocketConfigurer {
    private BrokerStomp brokerStomp;

    @Autowired
    public void setBrokerStomp(BrokerStomp brokerStomp) {
        this.brokerStomp = brokerStomp;
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // max topic content's length is 10k
        container.setMaxTextMessageBufferSize(16 * 1024);
        return container;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        HandShakeWebSocketInterceptor handShakeWebSocketInterceptor = interceptorWebSocket();

        registry.addHandler(this.brokerStomp, "/sockjs")
                .addInterceptors(handShakeWebSocketInterceptor)
                .setAllowedOrigins("*")
                .withSockJS()
                .setHeartbeatTime(BrokerApplication.weEventConfig.getStompHeartbeats() * 1000);

        registry.addHandler(this.brokerStomp, "/stomp")
                .addInterceptors(handShakeWebSocketInterceptor)
                .setAllowedOrigins("*");
    }

    /**
     * check if it is in the white table
     *
     * @return HandShakeWebSocketInterceptor
     */
    private HandShakeWebSocketInterceptor interceptorWebSocket() {

        log.info("client ip white table: {}", BrokerApplication.weEventConfig.getIpWhiteTable());
        return new HandShakeWebSocketInterceptor(BrokerApplication.weEventConfig.getIpWhiteTable());
    }
}
