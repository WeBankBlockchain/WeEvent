package com.webank.weevent.protocol.mqttbroker.store.dto;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/8
 */
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
