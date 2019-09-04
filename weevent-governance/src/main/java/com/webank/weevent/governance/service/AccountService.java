package com.webank.weevent.governance.service;

import java.util.List;

import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.mapper.AccountMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountMapper userMapper;

    public AccountEntity queryByUsername(String username) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUsername(username);
        // execute select
        List<AccountEntity> list = userMapper.accountList(accountEntity);
        if (list.size() > 0) {
            // get user info
            AccountEntity user = list.get(0);
            return user;
        }
        return null;
    }

    public AccountEntity queryById(Integer id) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setId(id);
        // execute select
        List<AccountEntity> list = userMapper.accountList(accountEntity);
        // get user info
        AccountEntity user = list.get(0);
        return user;
    }
}
