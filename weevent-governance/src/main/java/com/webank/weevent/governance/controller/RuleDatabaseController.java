package com.webank.weevent.governance.controller;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.webank.weevent.governance.entity.RuleDatabaseEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.RuleDatabaseService;

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
    public GovernanceResult getRuleDataBaseList(HttpServletRequest request, @RequestBody RuleDatabaseEntity ruleDatabaseEntity) throws GovernanceException {
        log.info("getRuleDataBaseList,userId:{}", ruleDatabaseEntity.getUserId());
        List<RuleDatabaseEntity> ruleDatabases = ruleDatabaseService.getRuleDataBaseList(request, ruleDatabaseEntity);
        return new GovernanceResult(ruleDatabases);
    }

    // add ruleDatabase
    @PostMapping("/add")
    public GovernanceResult addRuleDatabase(@Valid @RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request,
                                            HttpServletResponse response) throws GovernanceException {
        log.info("add  ruleDatabaseEntity service into db :{}", ruleDatabaseEntity);
        RuleDatabaseEntity rule = ruleDatabaseService.addRuleDatabase(ruleDatabaseEntity, request, response);
        return new GovernanceResult(rule);
    }

    @PostMapping("/update")
    public GovernanceResult updateRuleDatabase(@Validated @RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request,
                                               HttpServletResponse response) throws GovernanceException {
        log.info("update  ruleDatabaseEntity service ,id:{}", ruleDatabaseEntity.getId());
        ruleDatabaseService.updateRuleDatabase(ruleDatabaseEntity, request, response);
        return new GovernanceResult(true);
    }


    @PostMapping("/delete")
    public GovernanceResult deleteRuleDatabase(@RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        log.info("delete  ruleDatabaseEntity service ,id:{}", ruleDatabaseEntity.getId());
        ruleDatabaseService.deleteRuleDatabase(ruleDatabaseEntity, request);
        return new GovernanceResult(true);
    }

    @PostMapping("/checkDataBaseUrl")
    public GovernanceResult checkDataBaseUrl(@Validated @RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        log.info("checkDataBaseUrl service ,ruleDatabaseEntity:{}", ruleDatabaseEntity);
        ruleDatabaseService.checkRuleDataBaseUrl(ruleDatabaseEntity, request);
        return new GovernanceResult(true);
    }
}
