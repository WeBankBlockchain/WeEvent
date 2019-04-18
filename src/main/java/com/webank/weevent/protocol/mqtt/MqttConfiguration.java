package com.webank.weevent.protocol.mqtt;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;

/**
 * Beans to bridge to mqtt broker, like eclipse mosquito.
 *
 * @author matthewliu
 * @since 2018/02/25
 */
@Slf4j
@Configuration
public class MqttConfiguration {
    static private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        //url
        mqttConnectOptions.setServerURIs(new String[]{BrokerApplication.weEventConfig.getMqttBrokerUrl()});
        //auth
        if (!BrokerApplication.weEventConfig.getMqttBrokerUser().isEmpty()
                && !BrokerApplication.weEventConfig.getMqttBrokerPassword().isEmpty()) {
            mqttConnectOptions.setUserName(BrokerApplication.weEventConfig.getMqttBrokerUser());
            mqttConnectOptions.setPassword(BrokerApplication.weEventConfig.getMqttBrokerPassword().toCharArray());
        }
        //keep connection
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setConnectionTimeout(BrokerApplication.weEventConfig.getMqttBrokerTimeout());
        mqttConnectOptions.setKeepAliveInterval(BrokerApplication.weEventConfig.getMqttBrokerKeepAlive());
        return mqttConnectOptions;
    }

    static private MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(getMqttConnectOptions());
        return factory;
    }

    //server side
    @Bean
    public static MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    @ConditionalOnProperty({"mqtt.broker.url", "broker.zookeeper.ip"})
    public static MessageProducer mqttInbound() {
        log.info("MqttPahoMessageDrivenChannelAdapter bean instrument, url: {}", BrokerApplication.weEventConfig.getMqttBrokerUrl());

        // client id can not duplicate
        String clientId = "weevent-inbound-" + System.currentTimeMillis();
        log.info("inbound client id: {}", clientId);
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory());
        adapter.setCompletionTimeout(BrokerApplication.weEventConfig.getMqttBrokerTimeout());
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(BrokerApplication.weEventConfig.getMqttBrokerQos());
        adapter.setOutputChannel((MessageChannel) BrokerApplication.applicationContext.getBean("mqttInputChannel"));
        
        /* MqttPahoMessageDrivenChannelAdapter will connect and subscribe even when topic list is empty
         a trick to avoid "Error connecting or subscribing to []"
          */
        adapter.addTopic("not_exist_topic");
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    @ConditionalOnBean(MessageProducer.class)
    public static MessageHandler MqttBridgeHandler() {
        MqttBridge mqttBridge = new MqttBridge();
        mqttBridge.setProducer(BrokerApplication.applicationContext.getBean(IProducer.class));
        mqttBridge.setConsumer(BrokerApplication.applicationContext.getBean(IConsumer.class));
        return mqttBridge;
    }

    //client side
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    @ConditionalOnProperty({"mqtt.broker.url", "broker.zookeeper.ip"})
    public static MessageHandler mqttOutbound() {
        log.info("MqttPahoMessageHandler bean instrument, url: {}", BrokerApplication.weEventConfig.getMqttBrokerUrl());

        // client id can not duplicate
        String clientId = "weevent-outbound-" + System.currentTimeMillis();
        log.info("outbound client id: {}", clientId);
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(clientId, mqttClientFactory());
        messageHandler.setAsync(true);
        return messageHandler;
    }

    @Bean
    public static MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MqttGateway {
        void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, String data);
    }
}
