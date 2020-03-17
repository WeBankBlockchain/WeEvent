package com.webank.weevent.broker.protocol.mqtt.store;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
public interface IAuthService {
    boolean verifyUserName(String userName, String password);
}
