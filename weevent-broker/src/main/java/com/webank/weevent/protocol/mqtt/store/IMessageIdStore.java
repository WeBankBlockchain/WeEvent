package com.webank.weevent.protocol.mqtt.store;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/8
 */
public interface IMessageIdStore {
    int getNextMessageId();

    void releaseMessageId(int messageId);
}
