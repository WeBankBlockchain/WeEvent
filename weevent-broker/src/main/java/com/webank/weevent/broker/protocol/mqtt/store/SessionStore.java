package com.webank.weevent.broker.protocol.mqtt.store;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * all session context in MQTT.
 *
 * @author matthewliu
 * @since 2020/03/18
 */
public class SessionStore {
    // clientId <-> session
    private Map<String, SessionContext> sessionContexts = new ConcurrentHashMap<>();

    public boolean addSession(String clientId, SessionContext sessionContext) {
        if (this.sessionContexts.containsKey(clientId)) {
            return false;
        }

        this.sessionContexts.put(clientId, sessionContext);
        return true;
    }

    public Optional<SessionContext> getSession(String clientId) {
        if (this.sessionContexts.containsKey(clientId)) {
            return Optional.of(this.sessionContexts.get(clientId));
        }

        return Optional.empty();
    }

    public void removeSession(String clientId) {
        this.sessionContexts.remove(clientId);
    }
}
