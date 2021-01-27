package com.webank.weevent.broker.protocol.mqtt.store;

import com.webank.weevent.broker.entiry.AccountEntity;
import com.webank.weevent.broker.enums.IsAuthEnum;
import com.webank.weevent.broker.enums.IsDeleteEnum;
import com.webank.weevent.broker.repository.AccountRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author websterchen
 * @version v1.0
 * @since 2019/6/1
 */
@Slf4j
public class AuthService {
	
	private final String isAuth;
    private final AccountRepository accountRepository;

    public AuthService(String isAuth, AccountRepository accountRepository) {
    	this.isAuth = isAuth;
        this.accountRepository = accountRepository;
    }
    
    
    public boolean verifyUserName(String userName, String password) {
    	if(IsAuthEnum.OFF.getValue().equals(isAuth)) {
    		return true;
    	}
        AccountEntity accountEntity = accountRepository.findAllByUserNameAndDeleteAt(userName, IsDeleteEnum.NOT_DELETED.getCode());
        if(null == accountEntity) {
        	return false;
        }
        log.info("accountEntity:{}", accountEntity.toString());
        return password.equals(accountEntity.getPassword());
    }
}
