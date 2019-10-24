package com.webank.weevent.protocol.mqtt.common.impl;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.protocol.mqtt.common.IAuthService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
@Service
@Slf4j
public class IAuthServiceImpl implements IAuthService {
    @Override
    public boolean verifyUserName(String userName, String password) {
        String authAccount = BrokerApplication.environment.getProperty("spring.security.user.name");
        String authPassword = BrokerApplication.environment.getProperty("spring.security.user.password");

        if (StringUtils.isBlank(authAccount) || StringUtils.isBlank(authPassword)) {
            return true;
        }

        return authAccount.equals(userName) && authPassword.equals(password);
    }
}
