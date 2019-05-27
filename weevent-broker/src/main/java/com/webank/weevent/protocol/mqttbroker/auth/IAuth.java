package com.webank.weevent.protocol.mqttbroker.auth;

/**
 *@ClassName IAuth
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 22:00
 *@Version 1.0
 **/
public interface IAuth {
    /**
     * verify username and password
     */
    boolean verifyUser(String username, String password);
}
