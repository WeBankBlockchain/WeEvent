package com.webank.weevent.broker.protocol.mqtt.store;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.broker.protocol.mqtt.ProtocolProcess;
import com.webank.weevent.broker.utils.ZKStore;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.fisco.constant.WeEventConstants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import lombok.extern.slf4j.Slf4j;

/**
 * all session context in MQTT.
 * include persist session in local memory and zookeeper if setting
 *
 * @author matthewliu
 * @since 2020/03/18
 */
@Slf4j
public class SessionStore {
    private final IProducer producer;
    private final IConsumer consumer;
    private final MessageIdStore messageIdStore;
    private final int timeout;

    // clientId <-> session
    private final Map<String, SessionContext> sessionContexts = new ConcurrentHashMap<>();

    // persist session data update while connection open/close
    // PersistSession in local memory
    private final Map<String, PersistSession> persistSessions = new ConcurrentHashMap<>();
    // PersistSession in zookeeper if setting
    private final ZKStore<PersistSession> zkStore;

    public SessionStore(IProducer producer,
                        IConsumer consumer,
                        int timeout,
                        MessageIdStore messageIdStore,
                        ZKStore<PersistSession> zkStore) {
        this.producer = producer;
        this.consumer = consumer;
        this.timeout = timeout;
        this.messageIdStore = messageIdStore;
        this.zkStore = zkStore;
    }

    // initialize session in CONNECT, subscribeDataList is always empty right now
    public boolean addSession(String clientId, SessionContext sessionContext) {
        log.info("add session context, client id: {}", clientId);
        this.sessionContexts.put(clientId, sessionContext);

        if (!sessionContext.isCleanSession()) {
            PersistSession value = new PersistSession(clientId);

            // persist data in local memory
            if (this.persistSessions.containsKey(clientId)) {
                value = this.persistSessions.get(clientId);
            }

            // may be have persist session data in zookeeper
            if (this.zkStore != null) {
                try {
                    Optional<PersistSession> data = this.zkStore.get(clientId);
                    if (data.isPresent()) {
                        value = data.get();
                        log.info("reload persist session from zookeeper, {}", JsonHelper.object2Json(value));
                    }
                } catch (BrokerException e) {
                    log.error("reload persist session from zookeeper failed, {}", e.toString());
                    return false;
                }
            }

            this.persistSessions.put(clientId, value);
        }

        return true;
    }

    // finalize session when DISCONNECT or connection closed
    public void removeSession(String clientId) {
        if (this.sessionContexts.containsKey(clientId)) {
            log.info("clean session context, client id: {}", clientId);

            SessionContext session = this.sessionContexts.remove(clientId);
            this.cleanContext(session);
            session.closeSession();
        }
    }

    public boolean existSession(String clientId) {
        return this.sessionContexts.containsKey(clientId);
    }

    public void discardWillMessage(String clientId) {
        if (this.sessionContexts.containsKey(clientId)) {
            log.info("try to clean up will message");
            SessionContext session = this.sessionContexts.get(clientId);
            session.discardWillMessage();
        }
    }

    private Optional<SubscribeData> getSubscription(String clientId, String topic) {
        if (!this.sessionContexts.containsKey(clientId)) {
            return Optional.empty();
        }

        return this.sessionContexts.get(clientId).getSubscribeDataList().stream().filter(subscribeData -> subscribeData.getTopic().equals(topic)).findAny();
    }

    public String subscribe(SubscribeData subscribeData, Map<IConsumer.SubscribeExt, String> ext) {
        Optional<SubscribeData> exist = this.getSubscription(subscribeData.getClientId(), subscribeData.getTopic());
        if (exist.isPresent()) {
            log.info("already exist subscription, skip this");
            return exist.get().getSubscriptionId();
        }

        try {
            String offset = WeEvent.OFFSET_LAST;
            // get subscription id and offset from persist session
            if (this.persistSessions.containsKey(subscribeData.getClientId())) {
                Optional<SubscribeData> persist = this.persistSessions.get(subscribeData.getClientId()).getSubscribeDataList().stream().filter(subscribe -> subscribe.getTopic().equals(subscribeData.getTopic())).findFirst();
                if (persist.isPresent()) {
                    SubscribeData original = persist.get();
                    ext.put(IConsumer.SubscribeExt.SubscriptionId, original.getSubscriptionId());
                    offset = original.getOffset();
                    log.info("get data from persist session, topic: {} offset: {} subscriptionId: {}", original.getTopic(), offset, original.getSubscriptionId());
                }
            }

            String sid = this.consumer.subscribe(subscribeData.getTopic(),
                    subscribeData.getGroupId(),
                    offset,
                    ext,
                    new IConsumer.ConsumerListener() {
                        @Override
                        public void onEvent(String subscriptionId, WeEvent event) {
                            // send to subscribe
                            int messageId = messageIdStore.getNextMessageId();
                            sendEvent(subscribeData.getClientId(), subscriptionId, event, messageId);
                        }

                        @Override
                        public void onException(Throwable e) {
                            log.error("consumer onException", e);
                        }
                    });

            log.info("subscribe success, subscriptionId: {}", sid);

            // save subscriptionId and offset
            subscribeData.setSubscriptionId(sid);
            Long currentBlock = this.consumer.getBlockHeight(subscribeData.getGroupId());
            subscribeData.setOffset(currentBlock.toString());

            // update SessionContext
            this.getSession(subscribeData.getClientId()).ifPresent(context -> context.getSubscribeDataList().add(subscribeData));
            return sid;
        } catch (BrokerException e) {
            log.error("subscribe failed, {}", e.toString());
            return "";
        }
    }

    public void unSubscribe(String clientId, List<String> topics) {
        this.getSession(clientId).ifPresent(context -> {
            topics.forEach(topic -> {
                Optional<SubscribeData> subscribeData = context.getSubscribeDataList().stream().filter(item -> item.getTopic().equals(topic)).findFirst();
                subscribeData.ifPresent(subscribe -> {
                    try {
                        log.info("unSubscribe, clientId: {} topic: {} subscriptionId: {}", clientId, topic, subscribe.getSubscriptionId());
                        this.consumer.unSubscribe(subscribe.getSubscriptionId());
                        context.getSubscribeDataList().remove(subscribe);
                    } catch (BrokerException e) {
                        log.error("unSubscribe failed, {}", e.toString());
                    }
                });
            });
        });
    }

    private Optional<SessionContext> getSession(String clientId) {
        if (this.sessionContexts.containsKey(clientId)) {
            return Optional.of(this.sessionContexts.get(clientId));
        }

        return Optional.empty();
    }

    public boolean publishMessage(MqttPublishMessage msg, boolean will) {
        byte[] messageBytes = new byte[msg.payload().readableBytes()];
        msg.payload().getBytes(msg.payload().readerIndex(), messageBytes);
        Map<String, String> extensions = new HashMap<>();
        if (will) {
            extensions.put(WeEventConstants.EXTENSIONS_WILL_MESSAGE, WeEventConstants.EXTENSIONS_WILL_MESSAGE);
        }

        try {
            WeEvent event = new WeEvent(msg.variableHeader().topicName(), messageBytes, extensions);
            SendResult sendResult = this.producer.publish(event, "", this.timeout);
            return sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS;
        } catch (BrokerException e) {
            log.error("exception in publish, {}", e.toString());
            return false;
        }
    }

    private void sendEvent(String clientId, String subscriptionId, WeEvent event, int messageId) {
        ByteBuf payload = Unpooled.buffer();
        int payloadSize;
        try {
            byte[] content = JsonHelper.object2JsonBytes(event.getContent());
            payloadSize = content.length;
            payload.writeBytes(content);
        } catch (BrokerException e) {
            log.error("json encode failed, {}", e.toString());
            return;
        }

        int finalPayloadSize = payloadSize;
        this.getSession(clientId).ifPresent(context -> {
            log.debug("subscription list in session context, {}", context.getSubscribeDataList());

            context.getSubscribeDataList()
                    .stream()
                    .filter(item -> item.getSubscriptionId().equals(subscriptionId))
                    .findFirst()
                    .ifPresent(subscribe -> {
                        switch (subscribe.getMqttQoS()) {
                            case AT_MOST_ONCE:
                            case AT_LEAST_ONCE:
                                log.info("PUBLISH subscribe message to client, client id: {} {} {}", clientId, messageId, event);

                                // update offset
                                subscribe.setOffset(event.getEventId());

                                int remaining = ProtocolProcess.fixLengthOfMessageId + subscribe.getTopic().length() + finalPayloadSize;
                                //subscribe.getTopic() may be contain wildcard, use original topic in WeEvent
                                MqttMessage rsp = MqttMessageFactory.newMessage(new MqttFixedHeader(MqttMessageType.PUBLISH, false, subscribe.getMqttQoS(), false, remaining),
                                        new MqttPublishVariableHeader(event.getTopic(), messageId), payload);
                                context.sendRemote(rsp);
                                break;

                            case EXACTLY_ONCE:
                            default:
                                log.error("DOT NOT support Qos=2");
                        }
                    });
        });
    }

    private void cleanContext(SessionContext sessionContext) {
        if (sessionContext.getWillMessage() != null) {
            log.info("publish will message");
            this.publishMessage(sessionContext.getWillMessage(), true);
        }

        // deal with persist session
        if (sessionContext.isCleanSession()) {
            // clean persist state in local memory
            this.persistSessions.remove(sessionContext.getClientId());

            // clean persist state in zookeeper
            if (this.zkStore != null) {
                try {
                    if (this.zkStore.exist(sessionContext.getClientId())) {
                        log.info("clean persist state in zookeeper, {}", sessionContext.getClientId());

                        this.zkStore.remove(sessionContext.getClientId());
                    }
                } catch (BrokerException e) {
                    log.error("delete data in zookeeper failed, {}", e.toString());
                }
            }
        } else {
            if (!sessionContext.getSubscribeDataList().isEmpty()) {
                // update local memory
                PersistSession data = this.persistSessions.get(sessionContext.getClientId());
                data.setSubscribeDataList(sessionContext.getSubscribeDataList());

                // may be need update zookeeper
                if (this.zkStore != null) {
                    try {
                        log.info("flush persist session into zookeeper, {}", JsonHelper.object2Json(data));
                        this.zkStore.set(sessionContext.getClientId(), data);
                    } catch (BrokerException e) {
                        log.error("flush data in zookeeper failed, {}", e.toString());
                    }
                }
            }
        }

        log.debug("try to clean subscription list, {}", sessionContext.getSubscribeDataList());
        sessionContext.getSubscribeDataList().forEach(subscribeData -> {
            try {
                log.info("unSubscribe topic: {} {}", subscribeData.getTopic(), subscribeData.getSubscriptionId());
                this.consumer.unSubscribe(subscribeData.getSubscriptionId());
            } catch (BrokerException e) {
                log.error("unSubscribe failed, {}", e.toString());
            }
        });
    }
}
