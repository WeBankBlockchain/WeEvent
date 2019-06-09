package com.webank.weevent.protocol.mqttbroker.store;

import java.util.List;

import com.webank.weevent.protocol.mqttbroker.store.dto.DupPublishMessageStore;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/3
 */
public interface IDupPublishMessageStore {
    void put(String clientId, DupPublishMessageStore dupPublishMessageStore);

    List<DupPublishMessageStore> get(String clientId);

    void remove(String clientId, int messageId);

    void removeByClient(String clientId);
}
