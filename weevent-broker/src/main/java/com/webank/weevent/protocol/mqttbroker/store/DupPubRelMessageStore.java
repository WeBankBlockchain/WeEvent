package com.webank.weevent.protocol.mqttbroker.store;

/**
 *@ClassName DupPubRelMessageStore
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 21:53
 *@Version 1.0
 **/
public class DupPubRelMessageStore {
    private String clientId;

    private int messageId;

    public String getClientId() {
        return clientId;
    }

    public DupPubRelMessageStore setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public int getMessageId() {
        return messageId;
    }

    public DupPubRelMessageStore setMessageId(int messageId) {
        this.messageId = messageId;
        return this;
    }
}
