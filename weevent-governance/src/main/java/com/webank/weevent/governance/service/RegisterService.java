package com.webank.weevent.governance.service;

import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.entity.AccountExample;
import com.webank.weevent.governance.entity.AccountExample.Criteria;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.AccountMapper;
import com.webank.weevent.governance.result.GovernanceResult;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * user registerservice
 * 
 * @since 2019/05/22
 *
 */
@Service
@Slf4j
public class RegisterService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountService accountService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        try {
            // check database contain admin
            AccountEntity accountEntity = accountService.queryByUsername("admin");
            if (accountEntity == null) {
                accountEntity = new AccountEntity();
                accountEntity.setUsername("admin");
                accountEntity.setPassword(passwordEncoder.encode("123456"));
                accountEntity.setLastUpdate(new Date());
                accountEntity.setEmail("admin@xxx.com");
                accountMapper.insert(accountEntity);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            System.exit(SpringApplication.exit(context));
        }
    }

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
        List<AccountEntity> list = accountMapper.selectByExample(example);
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
        if (StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())
                || StringUtils.isBlank(user.getEmail())) {
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
        accountMapper.insert(user);
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

        AccountEntity storeUser = accountService.queryByUsername(user.getUsername());
        if (!passwordEncoder.matches(oldPassword, storeUser.getPassword())) {
            return GovernanceResult.build(400, "old password is incorrect");
        }

        String password = passwordEncoder.encode(user.getPassword());
        storeUser.setPassword(password);
        storeUser.setLastUpdate(new Date());

        accountMapper.updateByPrimaryKey(storeUser);
        return GovernanceResult.ok();
    }

    public GovernanceResult forgetPassword(String username, String emailSendUrl) throws GovernanceException {
        GovernanceResult result = checkData(username, 1);
        // user not exist
        if ((boolean) result.getData()) {
            return GovernanceResult.build(400, "username not exists");
        }
        // get user by username
        AccountEntity user = accountService.queryByUsername(username);

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
        AccountEntity user = accountService.queryByUsername(username);
        Integer userId = user.getId();
        return GovernanceResult.ok(userId);
    }

    public GovernanceResult resetPassword(AccountEntity user) {
        if (user.getPassword().length() < 6) {
            return GovernanceResult.build(400, "password is too short");
        }

        AccountEntity storeUser = accountService.queryByUsername(user.getUsername());

        String password = passwordEncoder.encode(user.getPassword());
        storeUser.setPassword(password);
        storeUser.setLastUpdate(new Date());

        accountMapper.updateByPrimaryKey(storeUser);
        return GovernanceResult.ok(true);
    }

}
