package com.webank.weevent.governance.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.webank.weevent.governance.entity.Account;
import com.webank.weevent.governance.entity.AccountExample;
import com.webank.weevent.governance.entity.AccountExample.Criteria;
import com.webank.weevent.governance.mapper.AccountMapper;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.JsonUtils;

@Service
public class LoginService {
	
	public static Map<String,Map<String,Long>> map = new HashMap<>();

	@Autowired
	private AccountMapper userMapper;
	
	@Value("${session.expire}")
	private Integer SESSION_EXPIRE;

	public GovernanceResult userLogin(String username, String password) {
		// 1、check username or passwore is correct
		// according username get userInfo
		AccountExample example = new AccountExample();
		Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
		// execute select
		List<Account> list = userMapper.selectByExample(example);
		if (list == null || list.size() == 0) {
			// return login fail
			return GovernanceResult.build(400, "username or password incorrect");
		}
		// get user info
		Account user = list.get(0);
		// check password is correct
		if (!DigestUtils.md5DigestAsHex(password.getBytes()).equals(user.getPassword())) {
			// 2、password is incorrect return login fail
			return GovernanceResult.build(400, "username or password incorrect");
		}
		user.setPassword(null);
		// 3、generate token。
		String token = UUID.randomUUID().toString();
		
		Map<String,Long> expiredMap = new HashMap<>();
		Long currentTime = new Date().getTime();
		Long expiredTime = currentTime + SESSION_EXPIRE*1000L;
		
		expiredMap.put(JsonUtils.objectToJson(user), expiredTime);
		map.put("SESSION:" + token,expiredMap);
		return GovernanceResult.ok(token);
	}

	public Account queryByUsername(String username) {
		AccountExample example = new AccountExample();
		Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
		// execute select
		List<Account> list = userMapper.selectByExample(example);
		if (list == null || list.size() == 0) {
			return null;
		}
		// get user info
		Account user = list.get(0);
		return user;
	}

}
