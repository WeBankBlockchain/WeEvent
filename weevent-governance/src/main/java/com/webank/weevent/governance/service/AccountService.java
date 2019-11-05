package com.webank.weevent.governance.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.AccountMapper;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.CookiesTools;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;


    @Autowired
    private CookiesTools cookiesTools;

    @PostConstruct
    public void init() throws GovernanceException {
        try {
            // check database contain admin
            AccountEntity accountEntity = this.queryByUsername("admin");
            if (accountEntity == null) {
                accountEntity = new AccountEntity();
                accountEntity.setUsername("admin");
                accountEntity.setPassword(passwordEncoder.encode("123456"));
                accountEntity.setLastUpdate(new Date());
                accountEntity.setEmail("admin@xxx.com");
                accountMapper.insertAccount(accountEntity);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GovernanceException("init admin account fail,error:{}", e);
        }
    }

    public GovernanceResult checkData(String param, int type) {
        // according type generate select condition
        AccountEntity accountEntity = new AccountEntity();
        // 1：username
        if (type == 1) {
            accountEntity.setUsername(param);
        } else {
            return GovernanceResult.build(400, "data type error");
        }
        // excute select
        List<AccountEntity> list = accountMapper.accountList(accountEntity);
        // is list contain data
        if (list != null && list.size() > 0) {
            // if list contain data return false
            return GovernanceResult.ok(false);
        }
        // if not contain data true
        return GovernanceResult.ok(true);
    }

    public GovernanceResult register(AccountEntity user) {
        // data criteral
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
            return GovernanceResult.build(400, "user data incomplete，regist fail");
        }
        // check username exist
        GovernanceResult result = checkData(user.getUsername(), 1);
        if (!(boolean) result.getData()) {
            return GovernanceResult.build(400, "this username occupied");
        }

        if (user.getPassword().length() < 6) {
            return GovernanceResult.build(400, "password is too short");
        }

        user.setLastUpdate(new Date());
        // secret
        String storePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(storePassword);
        // insert user into database
        accountMapper.insertAccount(user);
        // return true
        return GovernanceResult.ok();
    }

    public GovernanceResult updatePassword(AccountEntity user) {
        // data criteral
        if (StringUtils.isBlank(user.getPassword()) || StringUtils.isBlank(user.getOldPassword())) {
            return GovernanceResult.build(400, "password is blank，update fail");
        }

        if (user.getPassword().length() < 6) {
            return GovernanceResult.build(400, "password is too short");
        }
        // check oldPassword is correct
        String oldPassword = user.getOldPassword();

        AccountEntity storeUser = this.queryByUsername(user.getUsername());
        if (!passwordEncoder.matches(oldPassword, storeUser.getPassword())) {
            return GovernanceResult.build(400, "old password is incorrect");
        }

        String password = passwordEncoder.encode(user.getPassword());
        storeUser.setPassword(password);
        storeUser.setLastUpdate(new Date());

        accountMapper.updateAccount(storeUser);
        return GovernanceResult.ok();
    }

    public GovernanceResult forgetPassword(String username, String emailSendUrl) throws GovernanceException {
        GovernanceResult result = checkData(username, 1);
        // user not exist
        if ((boolean) result.getData()) {
            return GovernanceResult.build(400, "username not exists");
        }
        // get user by username
        AccountEntity user = this.queryByUsername(username);
        if (StringUtil.isBlank(user.getEmail())) {
            throw new GovernanceException(ErrorCode.NO_MAILBOX_CONFIGURED);
        }
        String content = "reset url is : " + emailSendUrl;
        try {
            mailService.sendSimpleMail(user.getEmail(), "Reset Password url", content);
        } catch (Exception e) {
            throw new GovernanceException(ErrorCode.SEND_EMAIL_ERROR);
        }
        return GovernanceResult.ok(user.getEmail());
    }

    public GovernanceResult getUserId(String username) {
        // get user by username
        AccountEntity user = this.queryByUsername(username);
        Integer userId = user.getId();
        return GovernanceResult.ok(userId);
    }

    public GovernanceResult resetPassword(AccountEntity user) {
        if (user.getPassword().length() < 6) {
            return GovernanceResult.build(400, "password is too short");
        }

        AccountEntity storeUser = this.queryByUsername(user.getUsername());

        String password = passwordEncoder.encode(user.getPassword());
        storeUser.setPassword(password);
        storeUser.setLastUpdate(new Date());

        accountMapper.updateAccount(storeUser);
        return GovernanceResult.ok(true);
    }


    public AccountEntity queryByUsername(String username) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setUsername(username);
        // execute select
        List<AccountEntity> list = accountMapper.accountList(accountEntity);
        if (list.size() > 0) {
            // get user info
            AccountEntity user = list.get(0);
            return user;
        }
        return null;
    }

    public List<AccountEntity> accountEntityList(HttpServletRequest request, AccountEntity accountEntity) throws GovernanceException {
        // execute select
        String accountId = cookiesTools.getCookieValueByName(request, ConstantProperties.COOKIE_MGR_ACCOUNT_ID);
        List<AccountEntity> list = accountMapper.accountList(accountEntity);
        //filter current user
        list = list.stream().filter(it -> !it.getId().toString().equals(accountId)).collect(Collectors.toList());
        list.forEach(it -> it.setPassword(null));
        return list;
    }


}
