package com.webank.weevent.protocol.mqttbroker.protocol;

import com.webank.weevent.protocol.mqttbroker.auth.IAuth;
import com.webank.weevent.protocol.mqttbroker.common.IMessageId;
import com.webank.weevent.protocol.mqttbroker.common.InternalCommunication;
import com.webank.weevent.protocol.mqttbroker.store.IDupPubRelMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IDupPublishMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.IRetainMessageStore;
import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISubscribeStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *@ClassName ProtocolProcess
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 20:16
 *@Version 1.0
 **/
@Component
public class ProtocolProcess {
    @Autowired
    private ISessionStore iSessionStore;

    @Autowired
    private ISubscribeStore iSubscribeStore;

    @Autowired
    private IAuth iAuth;

    @Autowired
    private IMessageId iMessageId;

    @Autowired
    private IRetainMessageStore iRetainMessageStore;

    @Autowired
    private IDupPublishMessageStore iDupPublishMessageStore;

    @Autowired
    private IDupPubRelMessageStore iDupPubRelMessageStore;

    /*@Autowired
    private InternalCommunication internalCommunication;*/

    private Connect connect;

    private Subscribe subscribe;

    private UnSubscribe unSubscribe;

    private Publish publish;

    private DisConnect disConnect;

    private PingReq pingReq;

    private PubRel pubRel;

    private PubAck pubAck;

    private PubRec pubRec;

    private PubComp pubComp;

    public Connect connect() {
        if (connect == null) {
            connect = new Connect(iSessionStore, iSubscribeStore, iDupPublishMessageStore, iDupPubRelMessageStore, iAuth);
        }
        return connect;
    }

    public Subscribe subscribe() {
        if (subscribe == null) {
            subscribe = new Subscribe(iSubscribeStore, iMessageId, iRetainMessageStore);
        }
        return subscribe;
    }

    public UnSubscribe unSubscribe() {
        if (unSubscribe == null) {
            unSubscribe = new UnSubscribe(iSubscribeStore);
        }
        return unSubscribe;
    }

    public Publish publish() {
        if (publish == null) {
            //publish = new Publish(iSessionStore, iSubscribeStore, iMessageId, iRetainMessageStore, iDupPublishMessageStore, internalCommunication);
        }
        return publish;
    }

    public DisConnect disConnect() {
        if (disConnect == null) {
            disConnect = new DisConnect(iSessionStore, iSubscribeStore, iDupPublishMessageStore, iDupPubRelMessageStore);
        }
        return disConnect;
    }

    public PingReq pingReq() {
        if (pingReq == null) {
            pingReq = new PingReq();
        }
        return pingReq;
    }

    public PubRel pubRel() {
        if (pubRel == null) {
            pubRel = new PubRel();
        }
        return pubRel;
    }

    public PubAck pubAck() {
        if (pubAck == null) {
            pubAck = new PubAck(iMessageId, iDupPublishMessageStore);
        }
        return pubAck;
    }

    public PubRec pubRec() {
        if (pubRec == null) {
            pubRec = new PubRec(iDupPublishMessageStore, iDupPubRelMessageStore);
        }
        return pubRec;
    }

    public PubComp pubComp() {
        if (pubComp == null) {
            pubComp = new PubComp(iMessageId, iDupPubRelMessageStore);
        }
        return pubComp;
    }

    public ISessionStore getSessionStoreService() {
        return iSessionStore;
    }
}
