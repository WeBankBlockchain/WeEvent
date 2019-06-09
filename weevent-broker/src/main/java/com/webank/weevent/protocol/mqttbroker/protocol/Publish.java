package com.webank.weevent.protocol.mqttbroker.protocol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.protocol.mqttbroker.internal.InternalCommunication;
import com.webank.weevent.protocol.mqttbroker.internal.InternalMessage;
import com.webank.weevent.protocol.mqttbroker.store.IDupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IMessageIdStore;
import com.webank.weevent.protocol.mqttbroker.store.IRetainMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;
import com.webank.weevent.protocol.mqttbroker.store.dto.DupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.dto.RetainMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.dto.SubscribeStore;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

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
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
@Slf4j
public class Publish {
    private IRetainMessageStore iRetainMessageStore;
    private ISessionStore iSessionStore;
    private ISubscribeStore iSubscribeStore;
    private IDupPublishMessageStore iDupPublishMessageStore;
    private InternalCommunication internalCommunication;
    private IMessageIdStore iMessageIdStore;
    private IProducer iproducer;
    private IConsumer iconsumer;

    public Publish(IRetainMessageStore iRetainMessageStore, ISessionStore iSessionStore, ISubscribeStore iSubscribeStore, IDupPublishMessageStore iDupPublishMessageStore, InternalCommunication internalCommunication, IMessageIdStore iMessageIdStore, IProducer iproducer, IConsumer iconsumer) {
        this.iRetainMessageStore = iRetainMessageStore;
        this.iSessionStore = iSessionStore;
        this.iSubscribeStore = iSubscribeStore;
        this.iDupPublishMessageStore = iDupPublishMessageStore;
        this.internalCommunication = internalCommunication;
        this.iMessageIdStore = iMessageIdStore;
        this.iproducer = iproducer;
        this.iconsumer = iconsumer;
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
            Map<String, String> extensions = new HashMap<>();
            this.sendMessageToFisco(msg.variableHeader().topicName(), messageBytes, WeEventConstants.DEFAULT_GROUP_ID, extensions);
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
            Map<String, String> extensions = new HashMap<>();
            this.sendMessageToFisco(msg.variableHeader().topicName(), messageBytes, WeEventConstants.DEFAULT_GROUP_ID, extensions);
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
            Map<String, String> extensions = new HashMap<>();
            this.sendMessageToFisco(msg.variableHeader().topicName(), messageBytes, WeEventConstants.DEFAULT_GROUP_ID, extensions);
            this.sendPublishMessage(msg.variableHeader().topicName(), msg.fixedHeader().qosLevel(), messageBytes, false, false);
            this.sendPubRecMessage(channel, msg.variableHeader().packetId());
        }
        // retain=1, retain message
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

    private boolean sendMessageToFisco(String topic, byte[] messageBytes, String groupId, Map<String, String> extensions) {
        SendResult sendResult = new SendResult();
        try {
            sendResult = this.iproducer.publish(new WeEvent(topic, messageBytes, extensions), groupId);
        } catch (BrokerException e) {
            log.error("publish error:{}", sendResult.toString());
            return false;
        }
        if (sendResult.getStatus() != SendResult.SendResultStatus.SUCCESS) {
            return false;
        }
        return true;
    }

    private void sendPublishMessage(String topic, MqttQoS mqttQoS, byte[] messageBytes, boolean retain, boolean dup) {
        List<SubscribeStore> subscribeStores = iSubscribeStore.search(topic);
        subscribeStores.forEach(subscribeStore -> {
            if (iSessionStore.containsKey(subscribeStore.getClientId())) {
                //get subscribe QOS value
                MqttQoS respQoS = mqttQoS.value() > subscribeStore.getMqttQoS() ? MqttQoS.valueOf(subscribeStore.getMqttQoS()) : mqttQoS;
                if (respQoS == MqttQoS.AT_MOST_ONCE) {
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, 0), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH - clientId: {}, topic: {}, Qos: {}", subscribeStore.getClientId(), topic, respQoS.value());
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.AT_LEAST_ONCE) {
                    int messageId = iMessageIdStore.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH AT_LEAST_ONCE- clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
                    DupPublishMessageStore dupPublishMessageStore = new DupPublishMessageStore().setClientId(subscribeStore.getClientId())
                            .setTopic(topic).setMqttQoS(respQoS.value()).setMessageBytes(messageBytes);
                    iDupPublishMessageStore.put(subscribeStore.getClientId(), dupPublishMessageStore);
                    iSessionStore.get(subscribeStore.getClientId()).getChannel().writeAndFlush(publishMessage);
                }
                if (respQoS == MqttQoS.EXACTLY_ONCE) {
                    int messageId = iMessageIdStore.getNextMessageId();
                    MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                            new MqttFixedHeader(MqttMessageType.PUBLISH, dup, respQoS, retain, 0),
                            new MqttPublishVariableHeader(topic, messageId), Unpooled.buffer().writeBytes(messageBytes));
                    log.debug("PUBLISH EXACTLY_ONCE- clientId: {}, topic: {}, Qos: {}, messageId: {}", subscribeStore.getClientId(), topic, respQoS.value(), messageId);
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
