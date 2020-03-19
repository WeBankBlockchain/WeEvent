package com.webank.weevent.broker.protocol.mqtt.store;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.Getter;
import lombok.Setter;

/**
 * session context in MQTT.
 *
 * @author matthewliu
 * @since 2020/03/18
 */
@Getter
@Setter
public class SessionContext {
    private String clientId;
    private Channel channel;
    private boolean cleanSession;
    private MqttPublishMessage willMessage;

    // subscription on session
    private List<SubscribeData> subscribeDataList = new ArrayList<>();

    public SessionContext(String clientId, Channel channel, boolean cleanSession, MqttPublishMessage willMessage) {
        this.clientId = clientId;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;
    }
}
