package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.List;

import com.webank.weevent.broker.protocol.mqtt.store.ISubscribeStore;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.sdk.BrokerException;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class UnSubscribe {
    private ISubscribeStore iSubscribeStore;
    private IConsumer iConsumer;

    public UnSubscribe(ISubscribeStore iSubscribeStore, IConsumer iConsumer) {
        this.iSubscribeStore = iSubscribeStore;
        this.iConsumer = iConsumer;
    }

    public void processUnSubscribe(Channel channel, MqttUnsubscribeMessage msg) {
        log.debug("processUnSubscribe: variableHeader:{} payLoadLen:{}", msg.variableHeader().toString(), msg.payload().toString().length());
        List<String> topicFilters = msg.payload().topics();
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        topicFilters.forEach(topicFilter -> {
            try {
                if (null != iSubscribeStore.get(topicFilter, clientId)) {
                    iConsumer.unSubscribe(iSubscribeStore.get(topicFilter, clientId).getSubscriptionId());
                }
            } catch (BrokerException e) {
                log.error("UNSUBSCRIBE error - subscriptionId: {}", iSubscribeStore.get(topicFilter, clientId).getSubscriptionId());
            }

            iSubscribeStore.remove(topicFilter, clientId);
            log.debug("UNSUBSCRIBE - clientId: {}, topicFilter: {}", clientId, topicFilter);
        });
        MqttUnsubAckMessage unsubAckMessage = (MqttUnsubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()), null);
        channel.writeAndFlush(unsubAckMessage);
    }
}
