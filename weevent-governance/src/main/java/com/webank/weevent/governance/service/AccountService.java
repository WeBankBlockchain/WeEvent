package com.webank.weevent.governance.service;

import java.util.List;

import com.webank.weevent.governance.entity.Account;
import com.webank.weevent.governance.entity.AccountExample;
import com.webank.weevent.governance.entity.AccountExample.Criteria;
import com.webank.weevent.governance.mapper.AccountMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountMapper userMapper;

    public Account queryByUsername(String username) {
	AccountExample example = new AccountExample();
	Criteria criteria = example.createCriteria();
	criteria.andUsernameEqualTo(username);
	// execute select
	List<Account> list = userMapper.selectByExample(example);
	// get user info
	Account user = list.get(0);
	return user;
    }

    public Account queryById(Integer id) {
	AccountExample example = new AccountExample();
	Criteria criteria = example.createCriteria();
	criteria.andIdEqualTo(id);
	// execute select
	List<Account> list = userMapper.selectByExample(example);
	// get user info
	Account user = list.get(0);
	return user;
    }
}
