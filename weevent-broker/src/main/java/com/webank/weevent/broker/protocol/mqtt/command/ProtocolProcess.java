package com.webank.weevent.broker.protocol.mqtt.command;

import com.webank.weevent.broker.protocol.mqtt.common.IAuthService;
import com.webank.weevent.broker.protocol.mqtt.store.IMessageIdStore;
import com.webank.weevent.broker.protocol.mqtt.store.ISessionStore;
import com.webank.weevent.broker.protocol.mqtt.store.ISubscribeStore;
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

    // beans
    private FiscoConfig fiscoConfig;
    private IProducer iproducer;
    private IConsumer iconsumer;
    private ISessionStore iSessionStore;
    private IAuthService iAuthService;
    private ISubscribeStore iSubscribeStore;
    private IMessageIdStore iMessageIdStore;

    @Autowired
    public void setFiscoConfig(FiscoConfig fiscoConfig) {
        this.fiscoConfig = fiscoConfig;
    }

    @Autowired
    public void setProducer(IProducer producer) {
        this.iproducer = producer;
    }

    @Autowired
    public void setConsumer(IConsumer consumer) {
        this.iconsumer = consumer;
    }

    @Autowired
    public void setiSessionStore(ISessionStore iSessionStore) {
        this.iSessionStore = iSessionStore;
    }

    @Autowired
    public void setiAuthService(IAuthService iAuthService) {
        this.iAuthService = iAuthService;
    }

    @Autowired
    public void setiSubscribeStore(ISubscribeStore iSubscribeStore) {
        this.iSubscribeStore = iSubscribeStore;
    }

    @Autowired
    public void setiMessageIdStore(IMessageIdStore iMessageIdStore) {
        this.iMessageIdStore = iMessageIdStore;
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
            publish = new Publish(fiscoConfig, iproducer);
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
