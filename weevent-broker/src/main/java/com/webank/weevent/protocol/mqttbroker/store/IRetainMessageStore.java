package com.webank.weevent.protocol.mqttbroker.store;

import java.util.List;

import com.webank.weevent.protocol.mqttbroker.store.dto.RetainMessageStore;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
public interface IRetainMessageStore {
    void put(String topic, RetainMessageStore retainMessageStore);

    RetainMessageStore get(String topic);

    void remove(String topic);

    boolean containsKey(String topic);

    List<RetainMessageStore> search(String topicFilter);
}
