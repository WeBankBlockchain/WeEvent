package com.webank.weevent.protocol.mqtt.command;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
@Slf4j
public class Publish {
    private IProducer iproducer;

    public Publish(IProducer iproducer) {
        this.iproducer = iproducer;
    }

    public void processPublish(Channel channel, MqttPublishMessage msg, boolean willmessage) {
        // QoS=0
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_MOST_ONCE) {
            log.error("doesn't support QoS=0 close channel");
            channel.close();//blockchain not suppuer QOS=0 colse channel
            return;
        }

        // QoS=1
        if (msg.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
            byte[] messageBytes = new byte[msg.payload().readableBytes()];
            msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
            Map<String, String> extensions = new HashMap<>();
            if (willmessage) {
                extensions.put(WeEventConstants.EXTENSIONS_WILL_MESSAGE, WeEventConstants.EXTENSIONS_WILL_MESSAGE);
            }
            SendResult sendResult = this.sendMessageToFisco(msg.variableHeader().topicName(), messageBytes, "", extensions);
            if (sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS) {
                this.sendPubAckMessage(channel, msg.variableHeader().packetId());
            }
        }

        // QoS=2
        if (msg.fixedHeader().qosLevel() == MqttQoS.EXACTLY_ONCE) {
            log.error("doesn't support QoS=2 close channel");
            channel.close();//blockchain not suppuer QOS=2 colse channel
        }
    }

    private SendResult sendMessageToFisco(String topic, byte[] messageBytes, String groupId, Map<String, String> extensions) {
        try {
            return this.iproducer.publishSync(new WeEvent(topic, messageBytes, extensions), groupId);
        } catch (BrokerException e) {
            log.error("exception in publish", e);
            return new SendResult(SendResult.SendResultStatus.ERROR);
        }
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
