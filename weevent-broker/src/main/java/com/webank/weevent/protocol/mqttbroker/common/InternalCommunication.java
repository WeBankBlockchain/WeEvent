package com.webank.weevent.protocol.mqttbroker.common;

import java.util.List;

import javax.annotation.PostConstruct;

import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;
import com.webank.weevent.protocol.mqttbroker.store.SubscribeStore;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteMessaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *@ClassName InternalCommunication
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 15:52
 *@Version 1.0
 **/
@Slf4j
public class InternalCommunication {
    private final String internalTopic = "internal-communication-topic";

    @Autowired
    private IgniteMessaging igniteMessaging;

    @Autowired
    private ISessionStore iSessionStore;

    @Autowired
    private ISubscribeStore iSubscribeStore;

    @Autowired
    private IMessageId iMessageId;

    @PostConstruct
    private void internalListen() {
        igniteMessaging.localListen(internalTopic, (nodeId, msg) -> {
            InternalMessage internalMessage = (InternalMessage) msg;
            this.sendPublishMessage(internalMessage.getTopic(), MqttQoS.valueOf(internalMessage.getMqttQoS()), internalMessage.getMessageBytes(), internalMessage.isRetain(), internalMessage.isDup());
            return true;
        });
    }

    public void internalSend(InternalMessage internalMessage) {
        if (igniteMessaging.clusterGroup().nodes() != null && igniteMessaging.clusterGroup().nodes().size() > 0) {
            igniteMessaging.send(internalTopic, internalMessage);
        }
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        List<SubscribeStore> subscribeStores = iSubscribeStore.search(topic);
        subscribeStores.forEach(subscribeStore -> {
            if (iSessionStore.containsKey(subscribeStore.getClientId())) {
                // 订阅者收到MQTT消息的QoS级别, 最终取决于发布消息的QoS和主题订阅的QoS
                MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    int messageId = iMessageId.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = iMessageId.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
            }
        });
    }
}
