package com.webank.weevent.protocol.mqttbroker.mqttprotocol;

import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;

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

    public DisConnect(ISubscribeStore iSubscribeStore, ISessionStore iSessionStore) {
        this.iSubscribeStore = iSubscribeStore;
        this.iSessionStore = iSessionStore;
    }

    public void processDisConnect(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        iSubscribeStore.removeForClient(clientId);
        iSessionStore.remove(clientId);
        channel.close();
    }
}
