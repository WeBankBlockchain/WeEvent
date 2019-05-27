package com.webank.weevent.protocol.mqttbroker.store;

import org.springframework.stereotype.Service;

/**
 *@ClassName ISessionStore
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 20:52
 *@Version 1.0
 **/
public interface ISessionStore {
    /**
     * put session
     */
    void put(String clientId, SessionStore sessionStore);

    /**
     * get session
     */
    SessionStore get(String clientId);

    /**
     * clientId's session exist
     */
    boolean containsKey(String clientId);

    /**
     * delete session
     */
    void remove(String clientId);
}
