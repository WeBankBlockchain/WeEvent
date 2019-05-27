package com.webank.weevent.protocol.mqttbroker.store;

import java.io.Serializable;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;

/**
 *@ClassName SessionStore
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 21:05
 *@Version 1.0
 **/
public class SessionStore {
    private String clientId;

    private Channel channel;

    private boolean cleanSession;

    private MqttPublishMessage mqttPublishMessage;

    public SessionStore(String clientId, Channel channel, boolean cleanSession, MqttPublishMessage mqttPublishMessage) {
        this.clientId = clientId;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.mqttPublishMessage = mqttPublishMessage;
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
        return mqttPublishMessage;
    }

    public SessionStore setWillMessage(MqttPublishMessage mqttPublishMessage) {
        this.mqttPublishMessage = mqttPublishMessage;
        return this;
    }
}
