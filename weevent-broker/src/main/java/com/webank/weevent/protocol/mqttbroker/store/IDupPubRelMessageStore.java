package com.webank.weevent.protocol.mqttbroker.store;

import java.util.List;

import com.webank.weevent.protocol.mqttbroker.store.dto.DupPubRelMessageStore;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/8
 */
public interface IDupPubRelMessageStore {
    void put(String clientId, DupPubRelMessageStore dupPubRelMessageStore);

    List<DupPubRelMessageStore> get(String clientId);

    void remove(String clientId, int messageId);

    void removeByClient(String clientId);
}
