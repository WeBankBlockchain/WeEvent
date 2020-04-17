package com.webank.weevent.broker.protocol.mqtt;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.config.FiscoConfig;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttConnAckMessage;
import io.netty.handler.codec.mqtt.MqttConnAckVariableHeader;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttIdentifierRejectedException;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageFactory;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttUnacceptableProtocolVersionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author websterchen
 * @author matthewliu
 * @version v1.0
 * @since 2019/6/2
 */
@Slf4j
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

    private int heartBeat;

    // session id(channel id if from tcp) <-> clientId
    private Map<String, String> authorSessions = new ConcurrentHashMap<>();
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
        this.connect = new Connect(authService, this.sessionStore);
        this.pingReq = new PingReq();
        this.publish = new Publish(producer, fiscoConfig.getWeb3sdkTimeout());
        this.pubAck = new PubAck(this.messageIdStore);
        this.subscribe = new Subscribe(this.sessionStore, this.messageIdStore, consumer);
        this.unSubscribe = new UnSubscribe(this.sessionStore, consumer);
        this.disConnect = new DisConnect();

        this.heartBeat = weEventConfig.getKeepAlive();
        this.consumer = consumer;
    }

    public int getHeartBeat() {
        return heartBeat;
    }

    public void cleanSession(String sessionId) {
        log.info("clean session: {}", sessionId);

        if (this.authorSessions.containsKey(sessionId)) {
            String clientId = this.authorSessions.get(sessionId);
            this.cleanClient(clientId);
            this.authorSessions.remove(sessionId);
        }
    }

    public void cleanClient(String clientId) {
        log.info("clean context on clientId: {}", clientId);

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

    // get response message if needed in decode failure
    public Optional<MqttMessage> getDecodeFailureRsp(MqttMessage msg) {
        if (msg.decoderResult().isFailure()) {
            log.error("decode message failed, {}", msg.decoderResult());

            if (msg.fixedHeader().messageType() == MqttMessageType.CONNECT) {
                MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_LEAST_ONCE, false, 0);

                Throwable cause = msg.decoderResult().cause();
                if (cause instanceof MqttUnacceptableProtocolVersionException) {
                    // Unsupported protocol
                    MqttMessage rsp = MqttMessageFactory.newMessage(fixedHeader,
                            new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
                    return Optional.of(rsp);
                } else if (cause instanceof MqttIdentifierRejectedException) {
                    // clientId illegal
                    MqttMessage rsp = MqttMessageFactory.newMessage(fixedHeader,
                            new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED, false), null);
                    return Optional.of(rsp);
                }
            }
        }

        return Optional.empty();
    }

    // CONNECT is different from the other command
    public MqttConnAckMessage processConnect(MqttConnectMessage msg, SessionContext sessionData) {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_LEAST_ONCE, false, 0);

        if (this.authorSessions.containsKey(sessionData.getSessionId())) {
            log.error("MUST be CONNECT only once");
            MqttMessage rsp = MqttMessageFactory.newMessage(fixedHeader,
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION, false), null);
            return (MqttConnAckMessage) rsp;
        }

        MqttConnAckMessage rsp = (MqttConnAckMessage) this.connect.processConnect(msg, sessionData);
        // if accept
        if (rsp.variableHeader().connectReturnCode() == MqttConnectReturnCode.CONNECTION_ACCEPTED) {
            this.authorSessions.put(sessionData.getSessionId(), sessionData.getClientId());
        }
        return rsp;
    }

    public MqttPublishMessage genWillMessage(MqttConnectMessage connectMessage) {
        if (connectMessage.variableHeader().isWillFlag()) {
            log.info("get will message from client");

            MqttMessage msg = MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(connectMessage.variableHeader().willQos()), connectMessage.variableHeader().isWillRetain(), 0),
                    new MqttPublishVariableHeader(connectMessage.payload().willTopic(), 0), Unpooled.buffer().writeBytes(connectMessage.payload().willMessageInBytes()));

            return (MqttPublishMessage) msg;
        }

        return null;
    }

    public Optional<MqttMessage> process(MqttMessage req, String sessionId, String remoteIp) throws BrokerException {
        if (!this.authorSessions.containsKey(sessionId)) {
            log.error("MUST CONNECT first, skip it");
            throw new BrokerException(ErrorCode.MQTT_CONNECT_CONFLICT);
        }

        String clientId = this.authorSessions.get(sessionId);
        if (!this.sessionStore.getSession(clientId).isPresent()) {
            log.error("unknown clientId, skip it");
            throw new BrokerException(ErrorCode.MQTT_UNKNOWN_CLIENT_ID);
        }

        switch (req.fixedHeader().messageType()) {
            case PINGREQ:
                return this.pingReq.process(req, clientId, remoteIp);

            case PUBLISH:
                return this.publish.process(req, clientId, remoteIp);

            case PUBACK:
                return this.pubAck.process(req, clientId, remoteIp);

            case SUBSCRIBE:
                return this.subscribe.process(req, clientId, remoteIp);

            case UNSUBSCRIBE:
                return this.unSubscribe.process(req, clientId, remoteIp);

            case DISCONNECT:
                return this.disConnect.process(req, clientId, remoteIp);

            default:
                log.error("DO NOT support MQTT command, {}", req.fixedHeader().messageType());
                throw new BrokerException(ErrorCode.MQTT_UNKNOWN_COMMAND);
        }
    }
}
