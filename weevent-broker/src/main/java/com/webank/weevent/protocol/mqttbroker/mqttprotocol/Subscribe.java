package com.webank.weevent.protocol.mqttbroker.mqttprotocol;

import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.protocol.mqttbroker.store.IMessageIdStore;
import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;
import com.webank.weevent.protocol.mqttbroker.store.dto.SubscribeStore;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckMessage;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class Subscribe {
    private ISubscribeStore iSubscribeStore;
    private IMessageIdStore iMessageIdStore;
    private ISessionStore iSessionStore;
    private IConsumer iConsumer;

    public Subscribe(ISessionStore iSessionStore, ISubscribeStore iSubscribeStore, IMessageIdStore iMessageIdStore, IConsumer iConsumer) {
        this.iSubscribeStore = iSubscribeStore;
        this.iMessageIdStore = iMessageIdStore;
        this.iSessionStore = iSessionStore;
        this.iConsumer = iConsumer;
    }

    public void processSubscribe(Channel channel, MqttSubscribeMessage msg) {
        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        if (this.validTopicFilter(topicSubscriptions)) {
            String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
            List<Integer> mqttQoSList = new ArrayList<>();
            topicSubscriptions.forEach(topicSubscription -> {
                String topicFilter = topicSubscription.topicName();
                MqttQoS mqttQoS = topicSubscription.qualityOfService();
                String subscriptionId = "";
                try {
                    subscriptionId = this.iConsumer.subscribe(topicFilter, WeEventConstants.DEFAULT_GROUP_ID, WeEvent.OFFSET_LAST, WeEventConstants.MQTTTYPE, new IConsumer.ConsumerListener() {
                        @Override
                        public void onEvent(String subscriptionId, WeEvent event) {
                            log.info("consumer onEvent, subscriptionId: {} event: {}", subscriptionId, event);
                            // send to subscribe
                            sendPublishMessage(topicFilter, mqttQoS, JSON.toJSON(event).toString().getBytes(), false, false);
                        }

                        @Override
                        public void onException(Throwable e) {
                            log.error("consumer onException", e);
                        }
                    });
                } catch (BrokerException e) {
                    log.error("subscribe exception:{}", e.getMessage());
                }
                SubscribeStore subscribeStore = new SubscribeStore(clientId, subscriptionId, topicFilter, mqttQoS.value());
                iSubscribeStore.put(topicFilter, subscribeStore);
                mqttQoSList.add(mqttQoS.value());
                log.debug("SUBSCRIBE - clientId: {}, topFilter: {}, QoS: {}", clientId, topicFilter, mqttQoS.value());
            });
            MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                    new MqttSubAckPayload(mqttQoSList));
            channel.writeAndFlush(subAckMessage);
        } else {
            channel.close();
        }
    }

    private boolean validTopicFilter(List<MqttTopicSubscription> topicSubscriptions) {
        /*for (MqttTopicSubscription topicSubscription : topicSubscriptions) {
            String topicFilter = topicSubscription.topicName();
            //valid topic fileter
            if (StrUtil.startWith(topicFilter, '#') || StrUtil.startWith(topicFilter, '+') || StrUtil.endWith(topicFilter, '/') || !StrUtil.contains(topicFilter, '/'))
                return false;
            if (StrUtil.contains(topicFilter, '#')) {
                if (!StrUtil.endWith(topicFilter, "/#")) return false;
                if (StrUtil.count(topicFilter, '#') > 1) return false;
            }
            if (StrUtil.contains(topicFilter, '+')) {
                if (StrUtil.count(topicFilter, '+') != StrUtil.count(topicFilter, "/+")) return false;
            }
        }*/
        return true;
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        List<SubscribeStore> subscribeStores = iSubscribeStore.searchByTopic(topic);
        subscribeStores.forEach(subscribeStore -> {
            if (iSessionStore.containsKey(subscribeStore.getClientId())) {
                //get subscribe QOS value
                MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    int messageId = iMessageIdStore.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH AT_LEAST_ONCE- clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = iMessageIdStore.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH EXACTLY_ONCE- clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
            }
        });
    }
}
