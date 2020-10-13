package com.webank.weevent.governance.controller;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.service.RuleDatabaseService;
import com.webank.weevent.governance.utils.JwtUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/circulationDatabase")
@Slf4j
public class RuleDatabaseController {

    @Autowired
    private RuleDatabaseService ruleDatabaseService;

    @PostMapping("/list")
    public GovernanceResult<List<RuleDatabaseEntity>> getRuleDataBaseList(HttpServletRequest request, @RequestBody RuleDatabaseEntity ruleDatabaseEntity) throws GovernanceException {
        log.info("getRuleDataBaseList,userId:{}", ruleDatabaseEntity.getUserId());
        ruleDatabaseEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        List<RuleDatabaseEntity> ruleDatabases = ruleDatabaseService.getRuleDataBaseList(request, ruleDatabaseEntity);
        return new GovernanceResult<>(ruleDatabases);
    }

    // add ruleDatabase
    @PostMapping("/add")
    public GovernanceResult<RuleDatabaseEntity> addRuleDatabase(@Valid @RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request,
                                            HttpServletResponse response) throws GovernanceException {
        log.info("add  ruleDatabaseEntity service into db :{}", ruleDatabaseEntity);
        ruleDatabaseEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        RuleDatabaseEntity rule = ruleDatabaseService.addRuleDatabase(ruleDatabaseEntity, request, response);
        return new GovernanceResult<>(rule);
    }

    @PostMapping("/update")
    public GovernanceResult<Boolean> updateRuleDatabase(@Validated @RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request,
                                               HttpServletResponse response) throws GovernanceException {
        log.info("update  ruleDatabaseEntity service ,id:{}", ruleDatabaseEntity.getId());
        ruleDatabaseEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        ruleDatabaseService.updateRuleDatabase(ruleDatabaseEntity, request, response);
        return new GovernanceResult<>(true);
    }


    @PostMapping("/delete")
    public GovernanceResult<Boolean> deleteRuleDatabase(@RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        log.info("delete  ruleDatabaseEntity service ,id:{}", ruleDatabaseEntity.getId());
        ruleDatabaseService.deleteRuleDatabase(ruleDatabaseEntity, request);
        return new GovernanceResult<>(true);
    }

    @PostMapping("/checkDataBaseUrl")
    public GovernanceResult<Boolean> checkDataBaseUrl(@RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        log.info("checkDataBaseUrl service ,ruleDatabaseEntity:{}", ruleDatabaseEntity);
        ruleDatabaseService.checkRuleDataBaseUrl(ruleDatabaseEntity, request);
        return new GovernanceResult<>(true);
    }
}
