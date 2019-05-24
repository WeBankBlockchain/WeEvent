package com.webank.weevent.governance.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.webank.weevent.governance.entity.Account;
import com.webank.weevent.governance.entity.AccountExample;
import com.webank.weevent.governance.entity.AccountExample.Criteria;
import com.webank.weevent.governance.mapper.AccountMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AccountDetailsService implements UserDetailsService {
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AccountMapper accountMapper;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info("username: {}", username);
	    // TODO 根据用户名，查找到对应的密码，与权限
		AccountExample example = new AccountExample();
		Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
	    List<Account> list = null;
		try {
			list = accountMapper.selectByExample(example);
		} catch (Exception e) {
			throw new UsernameNotFoundException("sql execute error!");
		}
	    
		if (list == null || list.size() == 0) {
			// if list contain data return false
			throw new UsernameNotFoundException("username or password is incorrect!");
		}
		Account account = list.get(0);
		String password = passwordEncoder.encode(account.getPassword());
		
        log.info("password: {}", password);
	 
	    // 封装用户信息，并返回。参数分别是：用户名，密码，用户权限
	    User user = new User(username, password,
	              AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));
	    return user;
	}

}
