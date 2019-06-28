package com.webank.weevent.protocol.mqttbroker.mqttprotocol;

import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.protocol.mqttbroker.common.IAuthService;
import com.webank.weevent.protocol.mqttbroker.store.IMessageIdStore;
import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/2
 */
@Component
public class ProtocolProcess {
    private Connect connect;
    private Publish publish;
    private Subscribe subscribe;
    private UnSubscribe unSubscribe;
    private IProducer iproducer;
    private IConsumer iconsumer;
    private DisConnect disConnect;
    private PubRel pubRel;
    private PubAck pubAck;
    private PubRec pubRec;
    private PubComp pubComp;
    private PingReq pingReq;

    @Autowired
    private ISessionStore iSessionStore;
    @Autowired
    private IAuthService iAuthService;
    @Autowired
    private ISubscribeStore iSubscribeStore;
    @Autowired
    private IMessageIdStore iMessageIdStore;

    @Autowired
    public void setProducer(IProducer producer) {
        this.iproducer = producer;
    }

    @Autowired
    public void setConsumer(IConsumer consumer) {
        this.iconsumer = consumer;
    }

    public Connect connect() {
        if (connect == null) {
            connect = new Connect(iSubscribeStore, iSessionStore, iAuthService);
        }
        return connect;
    }

    public DisConnect disConnect() {
        if (disConnect == null) {
            disConnect = new DisConnect(iSubscribeStore, iSessionStore, iconsumer);
        }
        return disConnect;
    }

    public Publish publish() {
        if (publish == null) {
            publish = new Publish(iproducer);
        }
        return publish;
    }

    public PubRel pubRel() {
        if (pubRel == null) {
            pubRel = new PubRel();
        }
        return pubRel;
    }

    public PubAck pubAck() {
        if (pubAck == null) {
            pubAck = new PubAck(iMessageIdStore);
        }
        return pubAck;
    }

    public PubRec pubRec() {
        if (pubRec == null) {
            pubRec = new PubRec();
        }
        return pubRec;
    }

    public PubComp pubComp() {
        if (pubComp == null) {
            pubComp = new PubComp(iMessageIdStore);
        }
        return pubComp;
    }

    public Subscribe subscribe() {
        if (subscribe == null) {
            subscribe = new Subscribe(iSessionStore, iSubscribeStore, iMessageIdStore, iconsumer);
        }
        return subscribe;
    }

    public UnSubscribe unSubscribe() {
        if (unSubscribe == null) {
            unSubscribe = new UnSubscribe(iSubscribeStore, iconsumer);
        }
        return unSubscribe;
    }

    public PingReq pingReq() {
        if (pingReq == null) {
            pingReq = new PingReq();
        }
        return pingReq;
    }

    public ISessionStore getSessionStore() {
        return iSessionStore;
    }
}
