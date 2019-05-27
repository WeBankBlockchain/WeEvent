package com.webank.weevent.protocol.mqttbroker.auth;

import java.security.interfaces.RSAPrivateKey;

import javax.annotation.PostConstruct;

import cn.hutool.core.io.IoUtil;
import org.apache.commons.lang.StringUtils;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import org.springframework.stereotype.Service;

/**
 *@ClassName Auth
 *@Description TODO
 *@Author websterchen
 *@Date 2019/5/21 22:02
 *@Version 1.0
 **/
@Service
public class Auth implements IAuth {
    private RSAPrivateKey privateKey;

    @Override
    public boolean verifyUser(String username, String password) {
        if (StringUtils.isBlank(username)) return false;
        if (StringUtils.isBlank(password)) return false;
        RSA rsa = new RSA(privateKey, null);

        String value = rsa.encryptBcd(username, KeyType.PrivateKey);
        return value.equals(password) ? true : false;
    }

    @PostConstruct
    public void init() {
        //privateKey = IoUtil.readObj(Auth.class.getClassLoader().getResourceAsStream("keystore/auth-private.key"));
    }
}
