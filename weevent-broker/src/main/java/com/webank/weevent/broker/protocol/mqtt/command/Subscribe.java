package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.webank.weevent.broker.protocol.mqtt.ProtocolProcess;
import com.webank.weevent.broker.protocol.mqtt.store.SessionStore;
import com.webank.weevent.broker.protocol.mqtt.store.SubscribeData;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.fisco.constant.WeEventConstants;

import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttSubAckPayload;
import io.netty.handler.codec.mqtt.MqttSubscribeMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class Subscribe implements MqttCommand {
    private final SessionStore sessionStore;

    public Subscribe(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public Optional<MqttMessage> process(MqttMessage req, String clientId, String remoteIp) throws BrokerException {
        MqttSubscribeMessage msg = (MqttSubscribeMessage) req;
        log.info("SUBSCRIBE, {}", msg.payload().topicSubscriptions());

        Optional<MqttTopicSubscription> notSupport = msg.payload().topicSubscriptions().stream().filter(item -> item.qualityOfService() == MqttQoS.EXACTLY_ONCE).findAny();
        if (notSupport.isPresent()) {
            log.error("DOT NOT support Qos=2, close");
            throw new BrokerException(ErrorCode.MQTT_NOT_SUPPORT_QOS2);
        }

        List<String> topics = msg.payload().topicSubscriptions().stream().map(MqttTopicSubscription::topicName).collect(Collectors.toList());
        if (topics.isEmpty()) {
            log.error("empty topic, skip it");
            return Optional.empty();
        }

        // external params
        Map<IConsumer.SubscribeExt, String> ext = new HashMap<>();
        ext.put(IConsumer.SubscribeExt.InterfaceType, WeEventConstants.MQTTTYPE);
        ext.put(IConsumer.SubscribeExt.RemoteIP, remoteIp);

        List<Integer> mqttQoSList = new ArrayList<>();
        if (msg.payload().topicSubscriptions().size() == 1) {
            String topic = msg.payload().topicSubscriptions().get(0).topicName();
            MqttQoS qos = msg.payload().topicSubscriptions().get(0).qualityOfService();

            // do subscribe
            SubscribeData subscribeData = new SubscribeData(clientId, topic, qos);
            String subscriptionId = this.sessionStore.subscribe(subscribeData, ext);
            if (StringUtils.isEmpty(subscriptionId)) {
                qos = MqttQoS.FAILURE;
            }
            mqttQoSList.add(qos.ordinal());
        } else {
            // subscribe one by one, because unsubscribe need support one by one
            msg.payload().topicSubscriptions().forEach(item -> {
                // do subscribe
                SubscribeData subscribeData = new SubscribeData(clientId, item.topicName(), item.qualityOfService());
                String subscriptionId = this.sessionStore.subscribe(subscribeData, ext);
                if (StringUtils.isEmpty(subscriptionId)) {
                    mqttQoSList.add(MqttQoS.FAILURE.ordinal());
                } else {
                    mqttQoSList.add(item.qualityOfService().ordinal());
                }
            });
        }

        MqttMessage rsp = genSubAck(msg.variableHeader().messageId(), mqttQoSList);
        return Optional.of(rsp);
    }

    private MqttMessage genSubAck(int messageId, List<Integer> mqttQoSList) {
        return MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_LEAST_ONCE, false, ProtocolProcess.fixLengthOfMessageId + mqttQoSList.size()),
                MqttMessageIdVariableHeader.from(messageId),
                new MqttSubAckPayload(mqttQoSList));
    }
}
