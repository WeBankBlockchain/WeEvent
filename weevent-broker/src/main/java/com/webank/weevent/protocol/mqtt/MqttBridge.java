package com.webank.weevent.protocol.mqtt;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;

/**
 * Bridge to mosquito broker.
 *
 * @author matthewliu
 * @since 2019/01/15
 */
@Slf4j
public class MqttBridge implements MessageHandler {
    private IProducer producer;
    private IConsumer consumer;
    private MqttConfiguration.MqttGateway mqttGateway;

    public MqttBridge() {
        this.mqttGateway = BrokerApplication.applicationContext.getBean(MqttConfiguration.MqttGateway.class);
    }

    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    public void setConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }

    public boolean unBindOutboundTopic(String subscriptionId) throws BrokerException {
        boolean result = this.consumer.unSubscribe(subscriptionId);
        log.info("unSubscribe, subscriptionId: {} result: {}", subscriptionId, result);
        return result;
    }

    public void assertExist(String topic, Long groupId) throws BrokerException {
        if (!this.producer.exist(topic, groupId)) {
            log.error("not exist topic: {}", topic);
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }
    }

    public String bindOutboundTopic(String topic, Long groupId) throws BrokerException {
        log.info("bind mqtt outbound topic: {}", topic);
        if (!this.consumer.exist(topic, groupId)) {
            log.error("not exist topic: {}", topic);
            throw new BrokerException(ErrorCode.TOPIC_NOT_EXIST);
        }

        // start it if not
        if (!this.consumer.isStarted()) {
            boolean result = this.consumer.startConsumer();
            if (!result) {
                log.error("start consumer failed");
                return "";
            }
        }

        String subscriptionId = this.consumer.subscribe(topic, groupId, WeEvent.OFFSET_LAST, "mqtt", new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {
                log.info("subscribe onEvent, subscriptionId: {} event: {}", subscriptionId, event);
                sendToMqtt(event.getTopic(), new String(event.getContent(), StandardCharsets.UTF_8));
            }

            @Override
            public void onException(Throwable e) {
                log.error("subscribe onException", e);
            }
        });

        log.error("bind mqtt outbound topic success, topic: {}, subscriptionId: {}", topic, subscriptionId);
        return subscriptionId;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        MessageHeaders handlers = message.getHeaders();
        Object topicKey = handlers.get(WeEventConstants.EXTENSIONS_RECEIVED_TOPIC);
        if (topicKey == null) {
            log.error("unknown {} extension", WeEventConstants.EXTENSIONS_RECEIVED_TOPIC);
            return;
        }

        Map<String, String> extensions = new HashMap<>();
        try {
            extensions = WeEventUtils.getObjectExtensions(handlers);
        } catch (BrokerException e) {
            log.error("getExtensions exception:{}", e);
            return;
        }
        Long groupId = WeEventConstants.DEFAULT_GROUP_ID;
        if (handlers.containsKey(WeEventConstants.EVENT_GROUP_ID)) {
            try {
                groupId = WeEventUtils.getGroupId(handlers.get(WeEventConstants.EVENT_GROUP_ID).toString());
            } catch (BrokerException e) {
                return;
            }
        }

        String topic = topicKey.toString();
        log.info("mqtt input message, id: {} topic: {} extensions: {}", handlers.getId(), topic, extensions.toString());
        String payload = (String) message.getPayload();

        // publish to event broker
        WeEvent weEvent = new WeEvent(topic, payload.getBytes(StandardCharsets.UTF_8), extensions);
        try {
            SendResult sendResult = this.producer.publish(weEvent, groupId);
            log.info("publish ok, {}", sendResult);
        } catch (BrokerException e) {
            log.error("publish failed", e);
        }
    }

    private void sendToMqtt(String topic, String content) {
        //publish to mqtt
        if (this.mqttGateway != null) {
            log.info("mqtt output message, topic: {}, content: {}", topic, content.length());
            this.mqttGateway.sendToMqtt(topic, content);
        }
    }
}
