package com.webank.weevent.protocol.mqttbroker.store;

import java.util.List;

import com.webank.weevent.protocol.mqttbroker.store.dto.SubscribeStore;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
public interface ISubscribeStore {
    void put(String topicFilter, SubscribeStore subscribeStore);

    void remove(String topicFilter, String clientId);

    void removeForClient(String clientId);

    List<SubscribeStore> search(String topic);
}
