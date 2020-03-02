package com.webank.weevent.broker.protocol.mqtt.store;

import java.util.List;

import com.webank.weevent.broker.protocol.mqtt.store.dto.SubscribeStore;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
public interface ISubscribeStore {
    void put(String topicFilter, SubscribeStore subscribeStore);

    SubscribeStore get(String topicFilter, String clientId);

    void remove(String topicFilter, String clientId);

    void removeForClient(String clientId);

    List<SubscribeStore> searchByTopic(String topic);

    List<SubscribeStore> searchByClientId(String clientId);
}
