package com.webank.weevent.broker.protocol.mqtt.command;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.webank.weevent.broker.protocol.mqtt.BrokerHandler;
import com.webank.weevent.broker.protocol.mqtt.ProtocolProcess;
import com.webank.weevent.broker.protocol.mqtt.store.MessageIdStore;
import com.webank.weevent.broker.protocol.mqtt.store.SessionContext;
import com.webank.weevent.broker.protocol.mqtt.store.SessionStore;
import com.webank.weevent.broker.protocol.mqtt.store.SubscribeData;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.fisco.constant.WeEventConstants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
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
public class Subscribe {
    private SessionStore sessionStore;
    private MessageIdStore messageIdStore;
    private IConsumer iConsumer;

    public Subscribe(SessionStore sessionStore, MessageIdStore messageIdStore, IConsumer iConsumer) {
        this.sessionStore = sessionStore;
        this.messageIdStore = messageIdStore;
        this.iConsumer = iConsumer;
    }

    public void processSubscribe(Channel channel, String clientId, MqttSubscribeMessage msg) {
        log.info("SUBSCRIBE, {}", msg.payload().topicSubscriptions());

        Optional<MqttTopicSubscription> notSupport = msg.payload().topicSubscriptions().stream().filter(item -> item.qualityOfService() == MqttQoS.EXACTLY_ONCE).findAny();
        if (notSupport.isPresent()) {
            log.error("DOT NOT support Qos=2, close");
            channel.close();
            return;
        }

        List<String> topics = msg.payload().topicSubscriptions().stream().map(MqttTopicSubscription::topicName).collect(Collectors.toList());
        if (topics.isEmpty()) {
            log.error("empty topic, close");
            channel.close();
            return;
        }

        // external params
        Map<IConsumer.SubscribeExt, String> ext = new HashMap<>();
        ext.put(IConsumer.SubscribeExt.InterfaceType, WeEventConstants.MQTTTYPE);
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        ext.put(IConsumer.SubscribeExt.RemoteIP, socketAddress.getAddress().getHostAddress());

        List<Integer> mqttQoSList = new ArrayList<>();
        if (msg.payload().topicSubscriptions().size() == 1) {
            String topic = msg.payload().topicSubscriptions().get(0).topicName();
            MqttQoS qos = msg.payload().topicSubscriptions().get(0).qualityOfService();

            String subscriptionId = this.subscribe(topic, "", ext, clientId);
            if (StringUtils.isEmpty(subscriptionId)) {
                qos = MqttQoS.FAILURE;
            } else {
                SubscribeData subscribeData = new SubscribeData(clientId, subscriptionId, topic, qos);
                Optional<SessionContext> sessionContext = this.sessionStore.getSession(clientId);
                sessionContext.ifPresent(context -> context.getSubscribeDataList().add(subscribeData));
            }

            mqttQoSList.add(qos.ordinal());
        } else {
            // subscribe one by one, because unsubscribe need support one by one
            msg.payload().topicSubscriptions().forEach(item -> {
                String subscriptionId = this.subscribe(item.topicName(), "", ext, clientId);
                if (StringUtils.isEmpty(subscriptionId)) {
                    mqttQoSList.add(MqttQoS.FAILURE.ordinal());
                } else {
                    SubscribeData subscribeData = new SubscribeData(clientId, subscriptionId, item.topicName(), item.qualityOfService());
                    Optional<SessionContext> sessionContext = this.sessionStore.getSession(clientId);
                    sessionContext.ifPresent(context -> context.getSubscribeDataList().add(subscribeData));
                    mqttQoSList.add(item.qualityOfService().ordinal());
                }
            });
        }

        MqttMessage rsp = genSubAck(msg.variableHeader().messageId(), mqttQoSList);
        BrokerHandler.sendRemote(channel, rsp);
    }

    private String subscribe(String topic, String groupId, Map<IConsumer.SubscribeExt, String> ext, String clientId) {
        try {
            String subscriptionId = this.iConsumer.subscribe(topic,
                    groupId,
                    WeEvent.OFFSET_LAST,
                    ext,
                    new IConsumer.ConsumerListener() {
                        @Override
                        public void onEvent(String subscriptionId, WeEvent event) {
                            // send to subscribe
                            sendEvent(clientId, subscriptionId, event);
                        }

                        @Override
                        public void onException(Throwable e) {
                            log.error("consumer onException", e);
                        }
                    });

            log.info("subscribe success, subscriptionId: {}", subscriptionId);
            return subscriptionId;
        } catch (BrokerException e) {
            log.error("subscribe exception: {}", e.getMessage());
            return "";
        }
    }

    private MqttMessage genSubAck(int messageId, List<Integer> mqttQoSList) {
        return MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_LEAST_ONCE, false, ProtocolProcess.fixLengthOfMessageId + mqttQoSList.size()),
                MqttMessageIdVariableHeader.from(messageId),
                new MqttSubAckPayload(mqttQoSList));
    }

    private void sendEvent(String clientId, String subscriptionId, WeEvent event) {
        ByteBuf payload = Unpooled.buffer();
        int payloadSize;
        try {
            byte[] content = JsonHelper.object2JsonBytes(event.getContent());
            payloadSize = content.length;
            payload.writeBytes(content);
        } catch (BrokerException e) {
            log.error("json encode failed, {}", e.getMessage());
            return;
        }

        Optional<SessionContext> sessionContext = this.sessionStore.getSession(clientId);
        int finalPayloadSize = payloadSize;
        sessionContext.ifPresent(context -> {
            Optional<SubscribeData> subscribeDataOptional = context.getSubscribeDataList().stream().filter(item -> item.getSubscriptionId().equals(subscriptionId)).findFirst();
            subscribeDataOptional.ifPresent(subscribe -> {
                switch (subscribe.getMqttQoS()) {
                    case AT_MOST_ONCE:
                    case AT_LEAST_ONCE:
                        int remaining = ProtocolProcess.fixLengthOfMessageId + subscribe.getTopic().length() + finalPayloadSize;
                        MqttMessage rsp = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PUBLISH, false, subscribe.getMqttQoS(), false, remaining),
                                new MqttPublishVariableHeader(subscribe.getTopic(), this.messageIdStore.getNextMessageId()), payload);
                        BrokerHandler.sendRemote(context.getChannel(), rsp);
                        break;

                    case EXACTLY_ONCE:
                    default:
                        log.error("DOT NOT support Qos=2");
                }
            });
        });
    }
}
