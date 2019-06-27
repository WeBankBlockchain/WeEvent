package com.webank.weevent.governance.controller;

import javax.validation.Valid;

import com.webank.weevent.governance.entity.Account;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.RegisterService;

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
public class RegitsterController {

    @Autowired
    private RegisterService registerService;

    @RequestMapping("/check/{param}/{type}")
    public GovernanceResult checkData(@PathVariable String param, @PathVariable Integer type) {
        GovernanceResult governanceResult = registerService.checkData(param, type);
        return governanceResult;
    }

    @PostMapping(value = "/register")
    public GovernanceResult register(@Valid @RequestBody Account user, BindingResult result) {
        GovernanceResult governanceResult = registerService.register(user);
        return governanceResult;
    }

    @PostMapping(value = "/update")
    public GovernanceResult updatePassword(@RequestBody Account user) {
        GovernanceResult governanceResult = registerService.updatePassword(user);
        return governanceResult;
    }

    @PostMapping(value = "/reset")
    public GovernanceResult resetPassword(@RequestBody Account user) {
        GovernanceResult governanceResult = registerService.resetPassword(user);
        return governanceResult;
    }

    @GetMapping("/forget")
    public GovernanceResult forgetPassword(@RequestParam String username) {
        GovernanceResult governanceResult = registerService.forgetPassword(username);
        return governanceResult;
    }

    @GetMapping("/getUserId")
    public GovernanceResult getUserId(@RequestParam String username) {
        GovernanceResult governanceResult = registerService.getUserId(username);
        return governanceResult;
    }
}
