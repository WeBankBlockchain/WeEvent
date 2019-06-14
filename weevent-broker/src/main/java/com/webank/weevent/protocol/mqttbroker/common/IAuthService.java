package com.webank.weevent.protocol.mqttbroker.common;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
public interface IAuthService {
    boolean verifyUserName(String userName, String password);
}
