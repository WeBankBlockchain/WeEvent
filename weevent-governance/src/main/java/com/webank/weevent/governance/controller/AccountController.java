package com.webank.weevent.governance.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weevent.governance.common.ConstantCode;
import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResponse;
import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.service.AccountService;
import com.webank.weevent.governance.utils.JwtUtils;

@RestController
@CrossOrigin
@RequestMapping(value = "/user")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @RequestMapping("/check/{param}/{type}")
    public GovernanceResponse<Object> checkData(@PathVariable String param, @PathVariable Integer type) {
        return accountService.checkData(param, type);
    }

    @PostMapping(value = "/register")
    public GovernanceResponse<Object> register(@Valid @RequestBody AccountEntity user, BindingResult result) throws GovernanceException {
        return accountService.register(user);
    }

    @PostMapping(value = "/update")
    public GovernanceResponse<Object> updatePassword(@RequestBody AccountEntity user) {
        return accountService.updatePassword(user);
    }

    @PostMapping(value = "/reset")
    public GovernanceResponse<Object> resetPassword(@RequestBody AccountEntity user) {
        return accountService.resetPassword(user);
    }

    @GetMapping("/getUserId")
    public GovernanceResponse<Integer> getUserId(@RequestParam String username) {
        return accountService.getUserId(username);
    }

    @RequestMapping("/require")
    public GovernanceResponse<Object> authRequire() {
        return new GovernanceResponse<>(ConstantCode.USER_NOT_LOGGED_IN);
    }

    /**
     * Query all account except themselves
     */
    @RequestMapping("/accountList")
    public GovernanceResponse<List<AccountEntity>> accountEntityList(AccountEntity accountEntity, HttpServletRequest request,
                                              HttpServletResponse response) throws GovernanceException {
        List<AccountEntity> accountEntities = accountService.accountEntityList(request, accountEntity, JwtUtils.getAccountId(request));
        return new GovernanceResponse<>(accountEntities);
    }

    /**
     * delete user by id
     */
    @RequestMapping("/delete")
    public GovernanceResponse<Boolean> deleteUser(@RequestBody AccountEntity accountEntity, HttpServletRequest request,
                                       HttpServletResponse response) throws GovernanceException {
        accountService.deleteUser(request, accountEntity);
        return new GovernanceResponse<>(true);
    }


}
