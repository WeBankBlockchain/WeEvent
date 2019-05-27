package com.webank.weevent.protocol.mqttbroker.store;

import java.util.List;

/**
 *@ClassName IDupPubRelMessageStore
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 21:52
 *@Version 1.0
 **/
public interface IDupPubRelMessageStore {
    /**
     * store message
     */
    void put(String clientId, DupPubRelMessageStore dupPubRelMessageStore);

    /**
     * get message list
     */
    List<DupPubRelMessageStore> get(String clientId);

    /**
     * 删除消息
     */
    void remove(String clientId, int messageId);

    /**
     * delete message by client
     */
    void removeByClient(String clientId);
}
