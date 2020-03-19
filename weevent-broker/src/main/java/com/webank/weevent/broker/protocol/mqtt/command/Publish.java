package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.broker.protocol.mqtt.BrokerHandler;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.fisco.constant.WeEventConstants;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
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
    int timeout;

    public Publish(IProducer iproducer, int timeout) {
        this.iproducer = iproducer;
        this.timeout = timeout;
    }

    public void processPublish(Channel channel, MqttPublishMessage msg) {
        log.info("PUBLISH, {} Qos: {}", msg.variableHeader().topicName(), msg.fixedHeader().qosLevel());

        switch (msg.fixedHeader().qosLevel()) {
            case AT_MOST_ONCE:
            case AT_LEAST_ONCE: {
                boolean result = this.publishMessage(msg, false);
                if (result && msg.fixedHeader().qosLevel() == MqttQoS.AT_LEAST_ONCE) {
                    this.sendPubAckMessage(channel, msg.variableHeader().packetId());
                }
            }
            break;

            case EXACTLY_ONCE:
            default: {
                log.error("DOT NOT support Qos=2, close");
                channel.close();
            }
        }
    }

    public boolean publishMessage(MqttPublishMessage msg, boolean will) {
        byte[] messageBytes = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
        Map<String, String> extensions = new HashMap<>();
        if (will) {
            extensions.put(WeEventConstants.EXTENSIONS_WILL_MESSAGE, WeEventConstants.EXTENSIONS_WILL_MESSAGE);
        }

        SendResult sendResult = this.publish(msg.variableHeader().topicName(), messageBytes, "", extensions);
        return sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS;
    }

    private SendResult publish(String topic, byte[] messageBytes, String groupId, Map<String, String> extensions) {
        try {
            return this.iproducer.publish(new WeEvent(topic, messageBytes, extensions), groupId, this.timeout);
        } catch (BrokerException e) {
            log.error("exception in publish", e);
            return new SendResult(SendResult.SendResultStatus.ERROR);
        }
    }

    private void sendPubAckMessage(Channel channel, int messageId) {
        MqttMessage rsp = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_LEAST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(messageId), null);
        BrokerHandler.sendRemote(channel, rsp);
    }
}
