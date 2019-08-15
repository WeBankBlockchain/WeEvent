package com.webank.weevent.protocol.mqttbroker.common.impl;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.protocol.mqttbroker.common.IAuthService;

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
        if (StringUtils.isBlank(BrokerApplication.weEventConfig.getMqttUserName()) || StringUtils.isBlank(BrokerApplication.weEventConfig.getMqttPassCode())){
            return true;
        }
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
            log.error("userName is isBlank");
            return false;
        }

        if (StringUtils.isBlank(BrokerApplication.weEventConfig.getMqttUserName()) || StringUtils.isBlank(BrokerApplication.weEventConfig.getMqttPassCode())) {
            return true;
        }

        if (userName.equals(BrokerApplication.weEventConfig.getMqttUserName()) && password.equals(BrokerApplication.weEventConfig.getMqttPassCode())) {
            return true;
        }

        return false;
    }
}
