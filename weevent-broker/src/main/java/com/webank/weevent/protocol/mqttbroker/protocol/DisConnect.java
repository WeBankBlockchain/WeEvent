package com.webank.weevent.protocol.mqttbroker.protocol;

import com.webank.weevent.protocol.mqttbroker.store.IDupPubRelMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;
import com.webank.weevent.protocol.mqttbroker.store.SessionStore;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 *@ClassName DisConnect
 *@Description mqttbroker DisConnect
 *@Author websterchen
 *@Date 2019/5/21 19:58
 *@Version 1.0
 **/
@Slf4j
public class DisConnect {
    private ISessionStore iSessionStore;

    private ISubscribeStore iSubscribeStore;

    private IDupPublishMessageStore iDupPublishMessageStore;

    private IDupPubRelMessageStore iDupPubRelMessageStore;

    public DisConnect(ISessionStore iSessionStore, ISubscribeStore iSubscribeStore, IDupPublishMessageStore iDupPublishMessageStore, IDupPubRelMessageStore iDupPubRelMessageStore) {
        this.iSessionStore = iSessionStore;
        this.iSubscribeStore = iSubscribeStore;
        this.iDupPublishMessageStore = iDupPublishMessageStore;
        this.iDupPubRelMessageStore = iDupPubRelMessageStore;
    }

    public void processDisConnect(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        SessionStore sessionStore = iSessionStore.get(clientId);
        if (sessionStore.isCleanSession()) {
            iSubscribeStore.removeForClient(clientId);
            iDupPublishMessageStore.removeByClient(clientId);
            iDupPubRelMessageStore.removeByClient(clientId);
        }
        iSessionStore.remove(clientId);
        channel.close();
    }
}
