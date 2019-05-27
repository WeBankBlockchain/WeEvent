package com.webank.weevent.protocol.mqttbroker.protocol;

import java.util.List;

import com.webank.weevent.protocol.mqttbroker.common.IMessageId;
import com.webank.weevent.protocol.mqttbroker.common.InternalCommunication;
import com.webank.weevent.protocol.mqttbroker.common.InternalMessage;
import com.webank.weevent.protocol.mqttbroker.store.DupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IRetainMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;
import com.webank.weevent.protocol.mqttbroker.store.RetainMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.SubscribeStore;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

/**
 *@ClassName Publish
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 14:47
 *@Version 1.0
 **/
@Slf4j
public class Publish {
    private ISessionStore iSessionStore;

    private ISubscribeStore iSubscribeStore;

    private IMessageId iMessageId;

    private IRetainMessageStore iRetainMessageStore;

    private IDupPublishMessageStore iDupPublishMessageStore;

    private InternalCommunication internalCommunication;

    public Publish(ISessionStore iSessionStore, ISubscribeStore iSubscribeStore, IMessageId iMessageId, IRetainMessageStore iRetainMessageStore, IDupPublishMessageStore iDupPublishMessageStore, InternalCommunication internalCommunication) {
        this.iSessionStore = iSessionStore;
        this.iSubscribeStore = iSubscribeStore;
        this.iMessageId = iMessageId;
        this.iRetainMessageStore = iRetainMessageStore;
        this.iDupPublishMessageStore = iDupPublishMessageStore;
        this.internalCommunication = internalCommunication;
    }

    public void processPublish(Channel channel, MqttPublishMessage msg) {
        // QoS=0
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_MOST_ONCE) {
            byte[] messageBytes = new byte[msg.payload().readableBytes()];
            msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
            InternalMessage internalMessage = new InternalMessage().setTopic(msg.variableHeader().topicName())
                    .setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(messageBytes)
                    .setDup(false).setRetain(false);
            internalCommunication.internalSend(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
        }
        // QoS=1
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            byte[] messageBytes = new byte[msg.payload().readableBytes()];
            msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
            InternalMessage internalMessage = new InternalMessage().setTopic(msg.variableHeader().topicName())
                    .setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(messageBytes)
                    .setDup(false).setRetain(false);
            internalCommunication.internalSend(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
            this.sendPubAckMessage(channel, msg.variableHeader().packetId());
        }
        // QoS=2
        if (msg.fixedHeader().qosLevel() == MqttQoS.EXACTLY_ONCE) {
            byte[] messageBytes = new byte[msg.payload().readableBytes()];
            msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
            InternalMessage internalMessage = new InternalMessage().setTopic(msg.variableHeader().topicName())
                    .setMqttQoS(msg.fixedHeader().qosLevel().value()).setMessageBytes(messageBytes)
                    .setDup(false).setRetain(false);
            internalCommunication.internalSend(internalMessage);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
            this.sendPubRecMessage(channel, msg.variableHeader().packetId());
        }
        // retain=1, 保留消息
        if (msg.fixedHeader().isRetain()) {
            byte[] messageBytes = new byte[msg.payload().readableBytes()];
            msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
            if (messageBytes.length == 0) {
                iRetainMessageStore.remove(msg.variableHeader().topicName());
            } else {
                RetainMessageStore retainMessageStore = new RetainMessageStore().setTopic(msg.variableHeader().topicName()).setMqttQoS(msg.fixedHeader().qosLevel().value())
                        .setMessageBytes(messageBytes);
                iRetainMessageStore.put(msg.variableHeader().topicName(), retainMessageStore);
            }
        }
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        List<SubscribeStore> subscribeStores = iSubscribeStore.search(topic);
        subscribeStores.forEach(subscribeStore -> {
            if (iSessionStore.containsKey(subscribeStore.getClientId())) {
                MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    int messageId = iMessageId.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(subscribeStore.getClientId())
                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
                    iDupPublishMessageStore.put(subscribeStore.getClientId(), dupPublishMessageStore);
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = iMessageId.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(subscribeStore.getClientId())
                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
                    iDupPublishMessageStore.put(subscribeStore.getClientId(), dupPublishMessageStore);
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
            }
        });
    }

    private void sendPubAckMessage(Channel channel, int messageId) {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        channel.writeAndFlush(pubAckMessage);
    }

    private void sendPubRecMessage(Channel channel, int messageId) {
        MqttMessage pubRecMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        channel.writeAndFlush(pubRecMessage);
    }

}
