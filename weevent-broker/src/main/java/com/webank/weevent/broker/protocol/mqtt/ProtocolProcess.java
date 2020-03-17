package com.webank.weevent.broker.protocol.mqtt;

import com.webank.weevent.broker.protocol.mqtt.command.Connect;
import com.webank.weevent.broker.protocol.mqtt.command.DisConnect;
import com.webank.weevent.broker.protocol.mqtt.command.PingReq;
import com.webank.weevent.broker.protocol.mqtt.command.PubAck;
import com.webank.weevent.broker.protocol.mqtt.command.PubComp;
import com.webank.weevent.broker.protocol.mqtt.command.PubRec;
import com.webank.weevent.broker.protocol.mqtt.command.PubRel;
import com.webank.weevent.broker.protocol.mqtt.command.Publish;
import com.webank.weevent.broker.protocol.mqtt.command.Subscribe;
import com.webank.weevent.broker.protocol.mqtt.command.UnSubscribe;
import com.webank.weevent.broker.protocol.mqtt.store.IAuthService;
import com.webank.weevent.broker.protocol.mqtt.store.IMessageIdStore;
import com.webank.weevent.broker.protocol.mqtt.store.ISessionStore;
import com.webank.weevent.broker.protocol.mqtt.store.ISubscribeStore;
import com.webank.weevent.broker.protocol.mqtt.store.impl.IAuthServiceImpl;
import com.webank.weevent.broker.protocol.mqtt.store.impl.IMessageIdStoreImpl;
import com.webank.weevent.broker.protocol.mqtt.store.impl.ISessionStoreImpl;
import com.webank.weevent.broker.protocol.mqtt.store.impl.ISubscribeStoreImpl;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.config.FiscoConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/2
 */
@Component
public class ProtocolProcess {
    private ISessionStore iSessionStore = new ISessionStoreImpl();
    private IAuthService iAuthService = new IAuthServiceImpl();
    private ISubscribeStore iSubscribeStore = new ISubscribeStoreImpl();
    private IMessageIdStore iMessageIdStore = new IMessageIdStoreImpl();

    private Connect connect;
    private Publish publish;
    private Subscribe subscribe;
    private UnSubscribe unSubscribe;
    private DisConnect disConnect;
    private PubRel pubRel;
    private PubAck pubAck;
    private PubRec pubRec;
    private PubComp pubComp;
    private PingReq pingReq;

    @Autowired
    public ProtocolProcess(FiscoConfig fiscoConfig, IProducer producer, IConsumer consumer) {
        connect = new Connect(iSubscribeStore, iSessionStore, iAuthService);
        disConnect = new DisConnect(iSubscribeStore, iSessionStore, consumer);
        publish = new Publish(fiscoConfig, producer);
        pubRel = new PubRel();
        pubAck = new PubAck(iMessageIdStore);
        pubRec = new PubRec();
        pubComp = new PubComp(iMessageIdStore);
        subscribe = new Subscribe(iSessionStore, iSubscribeStore, iMessageIdStore, consumer);
        unSubscribe = new UnSubscribe(iSubscribeStore, consumer);
        pingReq = new PingReq();
    }

    public Connect connect() {
        return connect;
    }

    public DisConnect disConnect() {
        return disConnect;
    }

    public Publish publish() {
        return publish;
    }

    public PubRel pubRel() {
        return pubRel;
    }

    public PubAck pubAck() {
        return pubAck;
    }

    public PubRec pubRec() {
        return pubRec;
    }

    public PubComp pubComp() {
        return pubComp;
    }

    public Subscribe subscribe() {
        return subscribe;
    }

    public UnSubscribe unSubscribe() {
        return unSubscribe;
    }

    public PingReq pingReq() {
        return pingReq;
    }

    public ISessionStore getSessionStore() {
        return iSessionStore;
    }
}
