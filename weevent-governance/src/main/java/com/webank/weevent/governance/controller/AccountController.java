package com.webank.weevent.governance.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.webank.weevent.governance.code.ConstantCode;
import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.entity.BaseResponse;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.AccountService;

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

@RestController
@CrossOrigin
@RequestMapping(value = "/user")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @RequestMapping("/check/{param}/{type}")
    public GovernanceResult checkData(@PathVariable String param, @PathVariable Integer type) {
        return accountService.checkData(param, type);
    }

    @PostMapping(value = "/register")
    public GovernanceResult register(@Valid @RequestBody AccountEntity user, BindingResult result) {
        return accountService.register(user);
    }

    @PostMapping(value = "/update")
    public GovernanceResult updatePassword(@RequestBody AccountEntity user) {
        return accountService.updatePassword(user);
    }

    @PostMapping(value = "/reset")
    public GovernanceResult resetPassword(@RequestBody AccountEntity user) {
        return accountService.resetPassword(user);
    }

    @GetMapping("/forget")
    public GovernanceResult forgetPassword(@RequestParam String username, HttpServletRequest request,
                                           HttpServletResponse response) throws GovernanceException {
        String url = request.getRequestURL().toString();
        int index = url.indexOf("weevent-governance");
        String emailSendUrl = url.substring(0, index + "weevent-governance".length());
        emailSendUrl = emailSendUrl + "/#/reset?username=" + username;
        System.out.println(emailSendUrl);
        return accountService.forgetPassword(username, emailSendUrl);
    }

    @GetMapping("/getUserId")
    public GovernanceResult getUserId(@RequestParam String username) {
        return accountService.getUserId(username);
    }

    @RequestMapping("/require")
    public BaseResponse authRequire() {
        return new BaseResponse(ConstantCode.USER_NOT_LOGGED_IN);
    }

    /**
     * Query all account except themselves
     */
    @RequestMapping("/accountList")
    public GovernanceResult accountEntityList(AccountEntity accountEntity, HttpServletRequest request,
                                              HttpServletResponse response) throws GovernanceException {
        List<AccountEntity> accountEntities = accountService.accountEntityList(request, accountEntity);
        return new GovernanceResult(accountEntities);
    }

    /**
     * delete user by id
     */
    @RequestMapping("/delete")
    public GovernanceResult deleteUser(@RequestBody AccountEntity accountEntity, HttpServletRequest request,
                                       HttpServletResponse response) throws GovernanceException {
        accountService.deleteUser(request, accountEntity);
        return new GovernanceResult(true);
    }


}
