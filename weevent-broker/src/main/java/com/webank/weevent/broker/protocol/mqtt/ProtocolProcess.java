package com.webank.weevent.broker.protocol.mqtt;

import java.util.Optional;

import com.webank.weevent.broker.config.WeEventConfig;
import com.webank.weevent.broker.protocol.mqtt.command.Connect;
import com.webank.weevent.broker.protocol.mqtt.command.DisConnect;
import com.webank.weevent.broker.protocol.mqtt.command.PingReq;
import com.webank.weevent.broker.protocol.mqtt.command.PubAck;
import com.webank.weevent.broker.protocol.mqtt.command.Publish;
import com.webank.weevent.broker.protocol.mqtt.command.Subscribe;
import com.webank.weevent.broker.protocol.mqtt.command.UnSubscribe;
import com.webank.weevent.broker.protocol.mqtt.store.AuthService;
import com.webank.weevent.broker.protocol.mqtt.store.MessageIdStore;
import com.webank.weevent.broker.protocol.mqtt.store.SessionContext;
import com.webank.weevent.broker.protocol.mqtt.store.SessionStore;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.config.FiscoConfig;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/2
 */
@Slf4j
@Getter
@Component
public class ProtocolProcess {
    // fix length of message id in variableHeader
    public static int fixLengthOfMessageId = 2;

    private Connect connect;
    private PingReq pingReq;
    private Publish publish;
    private PubAck pubAck;
    private Subscribe subscribe;
    private UnSubscribe unSubscribe;
    private DisConnect disConnect;

    private IConsumer consumer;
    private SessionStore sessionStore = new SessionStore();
    private MessageIdStore messageIdStore = new MessageIdStore();

    @Autowired
    public ProtocolProcess(Environment environment,
                           WeEventConfig weEventConfig,
                           FiscoConfig fiscoConfig,
                           IProducer producer,
                           IConsumer consumer) {
        AuthService authService = new AuthService(environment.getProperty("spring.security.user.name"),
                environment.getProperty("spring.security.user.password"));
        this.connect = new Connect(authService, this.sessionStore, weEventConfig.getKeepAlive());
        this.pingReq = new PingReq();
        this.publish = new Publish(producer, fiscoConfig.getWeb3sdkTimeout());
        this.pubAck = new PubAck(this.messageIdStore);
        this.subscribe = new Subscribe(this.sessionStore, this.messageIdStore, consumer);
        this.unSubscribe = new UnSubscribe(this.sessionStore, consumer);
        this.disConnect = new DisConnect();

        this.consumer = consumer;
    }

    public void cleanSession(String clientId) {
        log.info("clean session on clientId: {}", clientId);

        Optional<SessionContext> sessionContext = this.sessionStore.getSession(clientId);
        sessionContext.ifPresent(context -> {
            if (context.getWillMessage() != null) {
                this.publish.publishMessage(context.getWillMessage(), true);
            }

            context.getSubscribeDataList().forEach(subscribeData -> {
                try {
                    log.info("clientId: {}, unSubscribe topic: {} {}", clientId, subscribeData.getTopic(), subscribeData.getSubscriptionId());
                    this.consumer.unSubscribe(subscribeData.getSubscriptionId());
                } catch (BrokerException e) {
                    log.error("unSubscribe failed, {}", e.getMessage());
                }
            });
            context.getSubscribeDataList().clear();
            this.sessionStore.removeSession(clientId);
        });
    }
}
