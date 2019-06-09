package com.webank.weevent.protocol.mqttbroker.protocol;

import com.webank.weevent.protocol.mqttbroker.common.dto.SessionStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPubRelMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPublishMessageStore;
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
    private IDupPublishMessageStore iDupPublishMessageStore;
    private IDupPubRelMessageStore iDupPubRelMessageStore;

    public DisConnect(ISubscribeStore iSubscribeStore, IDupPublishMessageStore iDupPublishMessageStore, IDupPubRelMessageStore iDupPubRelMessageStore, ISessionStore iSessionStore) {
        this.iSubscribeStore = iSubscribeStore;
        this.iDupPublishMessageStore = iDupPublishMessageStore;
        this.iDupPubRelMessageStore = iDupPubRelMessageStore;
        this.iSessionStore = iSessionStore;
    }

    public void processDisConnect(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        SessionStore sessionStore = iSessionStore.get(clientId);
        if (sessionStore.isCleanSession()) {
            iSubscribeStore.removeForClient(clientId);
            iDupPublishMessageStore.removeByClient(clientId);
            iDupPubRelMessageStore.removeByClient(clientId);
        }
        log.debug("DISCONNECT - clientId: {}, cleanSession: {}", clientId, sessionStore.isCleanSession());
        iSessionStore.remove(clientId);
        channel.close();
    }
}
