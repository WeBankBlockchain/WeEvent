package com.webank.weevent.broker.protocol.mqtt.store.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.broker.protocol.mqtt.store.dto.SessionStore;
import com.webank.weevent.broker.protocol.mqtt.store.ISessionStore;
import com.webank.weevent.broker.protocol.mqtt.store.dto.SessionStore;


/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
public class ISessionStoreImpl implements ISessionStore {
    private Map<String, SessionStore> sessionCache = new ConcurrentHashMap<>();

    @Override
    public void put(String clientId, SessionStore sessionStore) {
        this.sessionCache.put(clientId, sessionStore);
    }

    @Override
    public SessionStore get(String clientId) {
        return this.sessionCache.get(clientId);
    }

    @Override
    public boolean containsKey(String clientId) {
        return this.sessionCache.containsKey(clientId);
    }

    @Override
    public void remove(String clientId) {
        this.sessionCache.remove(clientId);
    }
}
