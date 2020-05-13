package com.webank.weevent.broker.protocol.mqtt.store;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
@Slf4j
public class AuthService {
    private final String authAccount;
    private final String authPassword;

    public AuthService(String authAccount, String authPassword) {
        this.authAccount = authAccount;
        this.authPassword = authPassword;
    }

    public boolean verifyUserName(String userName, String password) {
        if (StringUtils.isBlank(this.authAccount) || StringUtils.isBlank(this.authPassword)) {
            return true;
        }

        return this.authAccount.equals(userName) && this.authPassword.equals(password);
    }
}
