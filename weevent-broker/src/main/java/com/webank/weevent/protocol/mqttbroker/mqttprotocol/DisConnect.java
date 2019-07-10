package com.webank.weevent.protocol.mqttbroker.mqttprotocol;

import java.util.List;

import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;
import com.webank.weevent.protocol.mqttbroker.store.dto.SubscribeStore;
import com.webank.weevent.sdk.BrokerException;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class DisConnect {
    private ISessionStore iSessionStore;
    private ISubscribeStore iSubscribeStore;
    private IConsumer iConsumer;

    public DisConnect(ISubscribeStore iSubscribeStore, ISessionStore iSessionStore, IConsumer iConsumer) {
        this.iSubscribeStore = iSubscribeStore;
        this.iSessionStore = iSessionStore;
        this.iConsumer = iConsumer;
    }

    public void processDisConnect(Channel channel, MqttMessage msg) {
        log.debug("processDisConnect variableHeader:{} payLoadLen", msg.variableHeader().toString(), msg.payload().toString().length());
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        List<SubscribeStore> subscribeStores = iSubscribeStore.searchByClientId(clientId);
        subscribeStores.forEach(subscribeStore -> {
            try {
                this.iConsumer.unSubscribe(subscribeStore.getSubscriptionId());
            } catch (BrokerException e) {
                log.error("unSubscribe Exception:{}", e.getMessage());
            }
        });

        this.iSubscribeStore.removeForClient(clientId);
        this.iSessionStore.remove(clientId);
        channel.close();
    }
}
