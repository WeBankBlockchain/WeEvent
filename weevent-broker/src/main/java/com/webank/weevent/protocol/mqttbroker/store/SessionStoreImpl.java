package com.webank.weevent.protocol.mqttbroker.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 *@ClassName SessionStoreImpl
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 21:32
 *@Version 1.0
 **/
@Service
public class SessionStoreImpl implements ISessionStore {
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
