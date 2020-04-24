package com.webank.weevent.broker.protocol.mqtt.store;

import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.broker.protocol.mqtt.TcpHandler;
import com.webank.weevent.broker.protocol.mqtt.WebSocketMqtt;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

/**
 * session context in MQTT.
 *
 * @author matthewliu
 * @since 2020/03/18
 */
@Getter
@Setter
@Slf4j
public class SessionContext {
    private final String sessionId;
    private final String clientId;
    private final boolean cleanSession;
    private MqttPublishMessage willMessage;

    // if from websocket
    private WebSocketSession session;
    // if from tcp
    private Channel channel;

    // subscription on session
    private final List<SubscribeData> subscribeDataList = new ArrayList<>();

    // from websocket
    public SessionContext(String sessionId, String clientId, WebSocketSession session, boolean cleanSession, MqttPublishMessage willMessage) {
        this.sessionId = sessionId;
        this.clientId = clientId;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;

        this.channel = null;
        this.session = session;
    }

    // from tcp
    public SessionContext(String sessionId, String clientId, Channel channel, boolean cleanSession, MqttPublishMessage willMessage) {
        this.sessionId = sessionId;
        this.clientId = clientId;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;

        this.channel = channel;
        this.session = null;
    }

    public void discardWillMessage() {
        if (this.willMessage != null) {
            log.info("discard will message");
            this.willMessage = null;
        }
    }

    public void sendRemote(MqttMessage rsp) {
        if (this.session != null) {
            WebSocketMqtt.send2Remote(this.session, rsp);
            return;
        }

        if (this.channel != null) {
            TcpHandler.sendRemote(this.channel, rsp);
        }
    }

    public void closeSession() {
        try {
            if (this.session != null) {
                log.info("close session connection");
                this.session.close();
            }

            if (this.channel != null) {
                log.info("close session connection");
                this.channel.close();
            }
        } catch (Exception e) {
            log.error("close session failed, {}", e.getMessage());
        }
    }
}
