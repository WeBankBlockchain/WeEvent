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
    private InternalCommunication internalCommunication;
    private IProducer iproducer;

    public Publish(IRetainMessageStore iRetainMessageStore, InternalCommunication internalCommunication, IProducer iproducer) {
        this.iRetainMessageStore = iRetainMessageStore;
        this.internalCommunication = internalCommunication;
        this.iproducer = iproducer;
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
            SendResult sendResult = this.sendMessageToFisco(msg.variableHeader().topicName(), messageBytes, WeEventConstants.DEFAULT_GROUP_ID, extensions);
            if (sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS) {
                this.sendPubAckMessage(channel, msg.variableHeader().packetId());
            }
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
            SendResult sendResult = this.sendMessageToFisco(msg.variableHeader().topicName(), messageBytes, WeEventConstants.DEFAULT_GROUP_ID, extensions);
            if (sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS) {
                this.sendPubRecMessage(channel, msg.variableHeader().packetId());
            }
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

    private SendResult sendMessageToFisco(String topic, byte[] messageBytes, String groupId, Map<String, String> extensions) {
        SendResult sendResult = new SendResult();
        try {
            this.iproducer.open(topic, groupId);
            sendResult = this.iproducer.publish(new WeEvent(topic, messageBytes, extensions), groupId);
        } catch (BrokerException e) {
            log.error("publish error:{}", sendResult.toString());
        }
        return sendResult;
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
