package com.webank.weevent.protocol.mqttbroker.store;

import java.util.List;

/**
 *@ClassName IRetainMessageStore
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/22 15:27
 *@Version 1.0
 **/
public interface IRetainMessageStore {
    /**
     * store retain message
     */
    void put(String topic, RetainMessageStore retainMessageStore);

    /**
     * get retain message
     */
    RetainMessageStore get(String topic);

    /**
     * remoce retain message
     */
    void remove(String topic);

    /**
     * get topic's retain message exist
     */
    boolean containsKey(String topic);

    /**
     * get retain message list
     */
    List<RetainMessageStore> search(String topicFilter);
}
