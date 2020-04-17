package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.ProtocolProcess;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.fisco.constant.WeEventConstants;

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
public class Publish implements MqttCommand {
    private IProducer iproducer;
    private int timeout;

    public Publish(IProducer iproducer, int timeout) {
        this.iproducer = iproducer;
        this.timeout = timeout;
    }

    @Override
    public Optional<MqttMessage> process(MqttMessage req, String clientId, String remoteIp) throws BrokerException {
        MqttPublishMessage msg = (MqttPublishMessage) req;
        log.info("PUBLISH, {} Qos: {}", msg.variableHeader().topicName(), msg.fixedHeader().qosLevel());

        switch (msg.fixedHeader().qosLevel()) {
            case AT_MOST_ONCE: {
                this.publishMessage(msg, false);
                return Optional.empty();
            }

            case AT_LEAST_ONCE: {
                boolean result = this.publishMessage(msg, false);
                if (result) {
                    MqttMessage rsp = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_LEAST_ONCE, false, ProtocolProcess.fixLengthOfMessageId),
                            MqttMessageIdVariableHeader.from(msg.variableHeader().packetId()), null);
                    return Optional.of(rsp);
                }

                return Optional.empty();
            }

            case EXACTLY_ONCE:
            default: {
                log.error("DOT NOT support Qos=2, close");
                throw new BrokerException(ErrorCode.MQTT_NOT_SUPPORT_QOS2);
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
}
