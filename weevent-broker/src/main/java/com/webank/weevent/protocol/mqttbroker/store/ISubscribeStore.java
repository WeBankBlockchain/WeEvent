package com.webank.weevent.protocol.mqttbroker.store;

import java.util.List;

/**
 *@ClassName ISubscribeStore
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 21:43
 *@Version 1.0
 **/
public interface ISubscribeStore {
    /**
     * store subscribe
     */
    void put(String topicFilter, SubscribeStore subscribeStore);

    /**
     * delete subscribe
     */
    void remove(String topicFilter, String clientId);

    /**
     * delete clientId subscribe
     */
    void removeForClient(String clientId);

    /**
     * get subscribe list
     */
    List<SubscribeStore> search(String topic);
}
