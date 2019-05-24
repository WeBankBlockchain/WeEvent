package com.webank.weevent.governance.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.webank.weevent.governance.entity.Account;
import com.webank.weevent.governance.entity.AccountExample;
import com.webank.weevent.governance.entity.AccountExample.Criteria;
import com.webank.weevent.governance.mapper.AccountMapper;
import com.webank.weevent.governance.result.GovernanceResult;

/**
 * user registerservice
 * @since 2019/05/22
 *
 */
@Service
public class RegisterService {

	@Autowired
	private AccountMapper accountMapper;

	public GovernanceResult checkData(String param, int type) {
		//according type generate select condition
		AccountExample example = new AccountExample();
		Criteria criteria = example.createCriteria();
		//1：username 2：email
		if (type == 1) {
			criteria.andUsernameEqualTo(param);
		} else if (type == 2) {
			criteria.andEmailEqualTo(param);
		} else {
			return GovernanceResult.build(400, "data type error");
		}
		//excute select
		List<Account> list = accountMapper.selectByExample(example);
		//is list contain data
		if (list != null && list.size()>0) {
			//if list contain data return false
			return GovernanceResult.ok(false);
		}
		//if not contain data true
		return GovernanceResult.ok(true);
	}

	public GovernanceResult register(Account user) {
		//data criteral
		if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())
				||StringUtils.isBlank(user.getEmail())) {
			return GovernanceResult.build(400, "user data incomplete，regist fail");
		}
		//1：username 2：email
		GovernanceResult result = checkData(user.getUsername(), 1);
		if (!(boolean) result.getData()) {
			return GovernanceResult.build(400, "this username occupied");
		}
		result = checkData(user.getEmail(), 2);
		if (!(boolean)result.getData()) {
			return GovernanceResult.build(400, "this email occupied");
		}
		user.setLastUpdate(new Date());
//		//md5 secret
//		String md5Pass = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
		user.setPassword(user.getPassword());
		//insert user into database
		accountMapper.insert(user);
		//return true
		return GovernanceResult.ok();
	}
}
