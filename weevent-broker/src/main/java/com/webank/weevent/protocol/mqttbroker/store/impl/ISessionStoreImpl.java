package com.webank.weevent.protocol.mqttbroker.store.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.protocol.mqttbroker.common.dto.SessionStore;
import com.webank.weevent.protocol.mqttbroker.store.ISessionStore;

import org.springframework.stereotype.Service;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
@Service
public class ISessionStoreImpl implements ISessionStore {
    private Map<String, SessionStore> sessionCache = new ConcurrentHashMap<String, SessionStore>();

    @Override
    public void put(String clientId, SessionStore sessionStore) {
        sessionCache.put(clientId, sessionStore);
    }

    @Override
    public SessionStore get(String clientId) {
        return sessionCache.get(clientId);
    }

    @Override
    public boolean containsKey(String clientId) {
        return sessionCache.containsKey(clientId);
    }

    @Override
    public void remove(String clientId) {
        sessionCache.remove(clientId);
    }
}
