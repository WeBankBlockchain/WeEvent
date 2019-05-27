package com.webank.weevent.protocol.mqttbroker.protocol;

import java.util.List;

import com.webank.weevent.protocol.mqttbroker.auth.IAuth;
import com.webank.weevent.protocol.mqttbroker.store.DupPubRelMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.DupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPubRelMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;
import com.webank.weevent.protocol.mqttbroker.store.SessionStore;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttIdentifierRejectedException;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnacceptableProtocolVersionException;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 *@ClassName Connect
 *@Description mqttbroker connect
 *@Author websterchen
 *@Date 2019/5/21 19:54
 *@Version 1.0
 **/
@Slf4j
public class Connect {
    private ISessionStore iSessionStore;

    private ISubscribeStore iSubscribeStore;

    private IDupPublishMessageStore iDupPublishMessageStore;

    private IDupPubRelMessageStore iDupPubRelMessageStore;

    private IAuth iAuth;

    public Connect(ISessionStore iSessionStore, ISubscribeStore iSubscribeStore, IDupPublishMessageStore iDupPublishMessageStore, IDupPubRelMessageStore iDupPubRelMessageStore, IAuth iAuth) {
        this.iSessionStore = iSessionStore;
        this.iSubscribeStore = iSubscribeStore;
        this.iDupPublishMessageStore = iDupPublishMessageStore;
        this.iDupPubRelMessageStore = iDupPubRelMessageStore;
        this.iAuth = iAuth;
    }

    public void processConnect(Channel channel, MqttConnectMessage msg) {
        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
                channel.writeAndFlush(connAckMessage);
                channel.close();
                return;
            } else if (cause instanceof MqttIdentifierRejectedException) {
                //clientId is error
                MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
                channel.writeAndFlush(connAckMessage);
                channel.close();
                return;
            }
            channel.close();
            return;
        }
        // clientId is null
        if (StringUtils.isBlank(msg.payload().clientIdentifier())) {
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
            channel.writeAndFlush(connAckMessage);
            channel.close();
            return;
        }
        //verify userName and password
        String username = msg.payload().userName();
        String password = msg.payload().passwordInBytes() == null ? null : new String(msg.payload().passwordInBytes(), CharsetUtil.UTF_8);
        if (!iAuth.verifyUser(username, password)) {
            MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD, false), null);
            channel.writeAndFlush(connAckMessage);
            channel.close();
            return;
        }
        // if clientId exit close old
        if (iSessionStore.containsKey(msg.payload().clientIdentifier())) {
            SessionStore sessionStore = iSessionStore.get(msg.payload().clientIdentifier());
            Channel previous = sessionStore.getChannel();
            Boolean cleanSession = sessionStore.isCleanSession();
            if (cleanSession) {
                iSessionStore.remove(msg.payload().clientIdentifier());
                iSubscribeStore.removeForClient(msg.payload().clientIdentifier());
                iDupPublishMessageStore.removeByClient(msg.payload().clientIdentifier());
                iDupPubRelMessageStore.removeByClient(msg.payload().clientIdentifier());
            }
            previous.close();
        }
        // Processing session
        SessionStore sessionStore = new SessionStore(msg.payload().clientIdentifier(), channel, msg.variableHeader().isCleanSession(), null);
        if (msg.variableHeader().isWillFlag()) {
            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(msg.variableHeader().willQos()), msg.variableHeader().isWillRetain(), 0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(), 0), Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes()));
            sessionStore.setWillMessage(willMessage);
        }
        // Processing heartbeat
        if (msg.variableHeader().keepAliveTimeSeconds() > 0) {
            if (channel.pipeline().names().contains("idle")) {
                channel.pipeline().remove("idle");
            }
            channel.pipeline().addFirst("idle", new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }
        // store client session and response to client
        iSessionStore.put(msg.payload().clientIdentifier(), sessionStore);
        // clientId store to channel map
        channel.attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
        Boolean sessionPresent = iSessionStore.containsKey(msg.payload().clientIdentifier()) && !msg.variableHeader().isCleanSession();
        MqttConnAckMessage okResp = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, sessionPresent), null);
        channel.writeAndFlush(okResp);
        log.debug("CONNECT - clientId: {}, cleanSession: {}", msg.payload().clientIdentifier(), msg.variableHeader().isCleanSession());
        // if cleanSession is 0 must send clientId's QoS1 and QoS2 DUP message
        if (!msg.variableHeader().isCleanSession()) {
            List<DupPublishMessageStore> dupPublishMessageStoreList = iDupPublishMessageStore.get(msg.payload().clientIdentifier());
            List<DupPubRelMessageStore> dupPubRelMessageStoreList = iDupPubRelMessageStore.get(msg.payload().clientIdentifier());
            dupPublishMessageStoreList.forEach(dupPublishMessageStore -> {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, true, MqttQoS.valueOf(dupPublishMessageStore.getMqttQoS()), false, 0),
                        new MqttPublishVariableHeader(dupPublishMessageStore.getTopic(), dupPublishMessageStore.getMessageId()), Unpooled.buffer().writeBytes(dupPublishMessageStore.getMessageBytes()));
                channel.writeAndFlush(publishMessage);
            });
            dupPubRelMessageStoreList.forEach(dupPubRelMessageStore -> {
                MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBREL, true, MqttQoS.AT_MOST_ONCE, false, 0),
                        MqttMessageIdVariableHeader.from(dupPubRelMessageStore.getMessageId()), null);
                channel.writeAndFlush(pubRelMessage);
            });
        }
    }

}
