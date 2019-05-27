package com.webank.weevent.protocol.mqttbroker.common;

/**
 *@ClassName IMessageId
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 14:37
 *@Version 1.0
 **/
public interface IMessageId {
    /**
     * get MessageId
     */
    int getNextMessageId();

    /**
     * release MessageId
     */
    void releaseMessageId(int messageId);
}
