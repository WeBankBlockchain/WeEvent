package com.webank.weevent.governance.service;

import java.util.Date;
import java.util.List;

import com.webank.weevent.governance.entity.Account;
import com.webank.weevent.governance.entity.AccountExample;
import com.webank.weevent.governance.entity.AccountExample.Criteria;
import com.webank.weevent.governance.mapper.AccountMapper;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.GeneratePasswordUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * user registerservice
 * 
 * @since 2019/05/22
 *
 */
@Service
public class RegisterService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MailService mailService;

    public GovernanceResult checkData(String param, int type) {
	// according type generate select condition
	AccountExample example = new AccountExample();
	Criteria criteria = example.createCriteria();
	// 1：username
	if (type == 1) {
	    criteria.andUsernameEqualTo(param);
	} else {
	    return GovernanceResult.build(400, "data type error");
	}
	// excute select
	List<Account> list = accountMapper.selectByExample(example);
	// is list contain data
	if (list != null && list.size() > 0) {
	    // if list contain data return false
	    return GovernanceResult.ok(false);
	}
	// if not contain data true
	return GovernanceResult.ok(true);
    }

    public GovernanceResult register(Account user) {
	// data criteral
	if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())
		|| StringUtils.isBlank(user.getEmail())) {
	    return GovernanceResult.build(400, "user data incomplete，regist fail");
	}
	// check username exist
	GovernanceResult result = checkData(user.getUsername(), 1);
	if (!(boolean) result.getData()) {
	    return GovernanceResult.build(400, "this username occupied");
	}
	
	if(user.getPassword().length() < 6) {
	    return GovernanceResult.build(400, "password is too short");
	}
	
	user.setLastUpdate(new Date());
	// secret
	String storePassword = passwordEncoder.encode(user.getPassword());
	user.setPassword(storePassword);
	// insert user into database
	accountMapper.insert(user);
	// return true
	return GovernanceResult.ok();
    }

    public GovernanceResult updatePassword(Account user) {
	// data criteral
	if (StringUtils.isBlank(user.getPassword()) || StringUtils.isBlank(user.getOldPassword())) {
	    return GovernanceResult.build(400, "password is blank，update fail");
	}
	// check oldPassword is correct
	String oldPassword = user.getOldPassword();

	Account storeUser = accountService.queryByUsername(user.getUsername());
	if (!passwordEncoder.matches(oldPassword, storeUser.getPassword())) {
	    return GovernanceResult.build(400, "old password is incorrect");
	}

	String password = passwordEncoder.encode(user.getPassword());
	storeUser.setPassword(password);
	storeUser.setLastUpdate(new Date());

	accountMapper.updateByPrimaryKey(storeUser);
	return GovernanceResult.ok();
    }

    public GovernanceResult forgetPassword(String username) {
	GovernanceResult result = checkData(username, 1);
	// user not exist
	if ((boolean) result.getData()) {
	    return GovernanceResult.build(400, "username not exists");
	}
	// get user by username
	Account user = accountService.queryByUsername(username);

	// generate new password
	String newPassword = GeneratePasswordUtil.generatePassword();
	String pwd = passwordEncoder.encode(newPassword);
	user.setPassword(pwd);
	user.setLastUpdate(new Date());
	// update password into database
	accountMapper.updateByPrimaryKey(user);

	String content = "The new reset password is : " + newPassword;
	mailService.sendSimpleMail(user.getEmail(), "Reset Password", content);
	return GovernanceResult.ok();
    }

    public GovernanceResult getUserId(String username) {
	// get user by username
	Account user = accountService.queryByUsername(username);
	Integer userId = user.getId();
	return GovernanceResult.ok(userId);
    }

}
