package com.webank.weevent.broker.protocol.mqtt.common.dto;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
public class SessionStore {
    private String clientId;

    private Channel channel;

    private boolean cleanSession;

    private MqttPublishMessage willMessage;

    public SessionStore(String clientId, Channel channel, boolean cleanSession, MqttPublishMessage willMessage) {
        this.clientId = clientId;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;
    }

    public String getClientId() {
        return clientId;
    }

    public SessionStore setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public Channel getChannel() {
        return channel;
    }

    public SessionStore setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public SessionStore setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
        return this;
    }

    public MqttPublishMessage getWillMessage() {
        return willMessage;
    }

    public SessionStore setWillMessage(MqttPublishMessage willMessage) {
        this.willMessage = willMessage;
        return this;
    }
}
