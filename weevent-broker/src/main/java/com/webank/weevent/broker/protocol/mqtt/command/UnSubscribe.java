package com.webank.weevent.broker.protocol.mqtt.command;

import java.util.Optional;

import com.webank.weevent.broker.protocol.mqtt.BrokerHandler;
import com.webank.weevent.broker.protocol.mqtt.ProtocolProcess;
import com.webank.weevent.broker.protocol.mqtt.store.SessionContext;
import com.webank.weevent.broker.protocol.mqtt.store.SessionStore;
import com.webank.weevent.broker.protocol.mqtt.store.SubscribeData;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.core.IConsumer;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/5
 */
@Slf4j
public class UnSubscribe {
    private SessionStore sessionStore;
    private IConsumer iConsumer;

    public UnSubscribe(SessionStore sessionStore, IConsumer iConsumer) {
        this.sessionStore = sessionStore;
        this.iConsumer = iConsumer;
    }

    public void processUnSubscribe(Channel channel, String clientId, MqttUnsubscribeMessage msg) {
        log.info("UNSUBSCRIBE, {}", msg.payload().topics());

        if (msg.payload().topics().isEmpty()) {
            log.error("empty topic, skip message");
            channel.close();
            return;
        }

        Optional<SessionContext> sessionContext = this.sessionStore.getSession(clientId);
        sessionContext.ifPresent(context -> {
            msg.payload().topics().forEach(topic -> {
                Optional<SubscribeData> subscribeData = context.getSubscribeDataList().stream().filter(item -> item.getTopic().equals(topic)).findFirst();
                subscribeData.ifPresent(subscribe -> {
                    try {
                        log.info("clientId: {}, unSubscribe topic: {} {}", clientId, topic, subscribe.getSubscriptionId());
                        this.iConsumer.unSubscribe(subscribe.getSubscriptionId());
                    } catch (BrokerException e) {
                        log.error("unSubscribe failed", e);
                    }
                    context.getSubscribeDataList().remove(subscribe);
                });
            });
        });

        MqttMessage rsp = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_LEAST_ONCE, false, ProtocolProcess.fixLengthOfMessageId),
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()), null);
        BrokerHandler.sendRemote(channel, rsp);
    }
}
