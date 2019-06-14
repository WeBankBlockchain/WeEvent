package com.webank.weevent.protocol.mqttbroker.store;

import com.webank.weevent.protocol.mqttbroker.common.dto.SessionStore;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
public interface ISessionStore {
    void put(String clientId, SessionStore sessionStore);

    SessionStore get(String clientId);

    boolean containsKey(String clientId);

    void remove(String clientId);
}
