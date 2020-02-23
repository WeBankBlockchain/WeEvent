package com.webank.weevent.protocol.mqtt.common.impl;

import com.webank.weevent.protocol.mqtt.common.IAuthService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
@Slf4j
@Service
public class IAuthServiceImpl implements IAuthService {
    private String authAccount = "";
    private String authPassword = "";

    @Autowired
    public void setEnvironment(Environment environment) {
        this.authAccount = environment.getProperty("spring.security.user.name");
        this.authPassword = environment.getProperty("spring.security.user.password");
    }

    @Override
    public boolean verifyUserName(String userName, String password) {
        if (StringUtils.isBlank(this.authAccount) || StringUtils.isBlank(this.authPassword)) {
            return true;
        }

        return this.authAccount.equals(userName) && this.authPassword.equals(password);
    }
}
