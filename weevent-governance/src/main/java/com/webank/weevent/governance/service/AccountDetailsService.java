package com.webank.weevent.governance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.webank.weevent.governance.entity.Account;

@Component
@Slf4j
public class AccountDetailsService implements UserDetailsService {

    @Autowired
    private AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	log.info("username: {}", username);
	Account account = null;
	try {
	    account = accountService.queryByUsername(username);
	} catch (Exception e) {
	    throw new UsernameNotFoundException("sql execute error!");
	}
	String password = account.getPassword();

	log.info("password: {}", password);

	User user = new User(username, password, AuthorityUtils.commaSeparatedStringToAuthorityList("admin"));
	return user;
    }

}
