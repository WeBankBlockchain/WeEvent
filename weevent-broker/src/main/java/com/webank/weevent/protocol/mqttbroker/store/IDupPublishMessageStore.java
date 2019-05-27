package com.webank.weevent.protocol.mqttbroker.store;

import java.util.List;

/**
 *@ClassName IDupPublishMessageStore
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 21:46
 *@Version 1.0
 **/
public interface IDupPublishMessageStore {
    /**
     * store message
     */
    void put(String clientId, DupPublishMessageStore dupPublishMessageStore);

    /**
     * get message list
     */
    List<DupPublishMessageStore> get(String clientId);

    /**
     * delete message
     */
    void remove(String clientId, int messageId);

    /**
     * delete message by clientId
     */
    void removeByClient(String clientId);
}
