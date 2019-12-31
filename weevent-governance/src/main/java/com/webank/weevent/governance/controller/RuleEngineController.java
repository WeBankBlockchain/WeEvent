package com.webank.weevent.governance.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.service.RuleEngineService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/ruleEngine")
@Slf4j
public class RuleEngineController {

    @Autowired
    private RuleEngineService ruleEngineService;


    // get  ruleEngine list
    @PostMapping("/list")
    public GovernanceResult getRuleEngines(HttpServletRequest request, @RequestBody RuleEngineEntity ruleEngineEntity, @CookieValue("MGR_ACCOUNT_ID") Integer accountId) throws GovernanceException {
        log.info("get ruleEngines , ruleEngineEntity :{}", ruleEngineEntity);
        ruleEngineEntity.setUserId(accountId);
        List<RuleEngineEntity> ruleEngines = ruleEngineService.getRuleEngines(request, ruleEngineEntity);
        GovernanceResult governanceResult = new GovernanceResult(ruleEngines);
        governanceResult.setTotalCount(ruleEngineEntity.getTotalCount());
        return governanceResult;
    }

    // add RuleEngineEntity
    @PostMapping("/add")
    public GovernanceResult addRuleEngine(@Valid @RequestBody RuleEngineEntity ruleEngineEntity, @CookieValue("MGR_ACCOUNT_ID") Integer accountId, HttpServletRequest request,
                                          HttpServletResponse response) throws GovernanceException {
        log.info("add  ruleEngineEntity service into db :{}", ruleEngineEntity);
        ruleEngineEntity.setUserId(accountId);
        RuleEngineEntity rule = ruleEngineService.addRuleEngine(ruleEngineEntity, request, response);
        return new GovernanceResult(rule);
    }

    @PostMapping("/update")
    public GovernanceResult updateRuleEngine(@RequestBody RuleEngineEntity ruleEngineEntity, @CookieValue("MGR_ACCOUNT_ID") Integer accountId, HttpServletRequest request,
                                             HttpServletResponse response) throws GovernanceException {
        log.info("update  ruleEngineEntity service ,ruleEngineEntity:{}", ruleEngineEntity);
        ruleEngineEntity.setUserId(accountId);
        boolean flag = ruleEngineService.updateRuleEngine(ruleEngineEntity, request, response);
        return new GovernanceResult(flag);
    }

    @PostMapping("/updateStatus")
    public GovernanceResult updateRuleEngineStatus(@RequestBody RuleEngineEntity ruleEngineEntity, @CookieValue("MGR_ACCOUNT_ID") Integer accountId, HttpServletRequest request,
                                                   HttpServletResponse response) throws GovernanceException {
        log.info("update  ruleEngineStatus service ,status:{}", ruleEngineEntity.getStatus());
        ruleEngineEntity.setUserId(accountId);
        boolean flag = ruleEngineService.updateRuleEngineStatus(ruleEngineEntity, request, response);
        return new GovernanceResult(flag);
    }

    @PostMapping("/delete")
    public GovernanceResult deleteBroker(@RequestBody RuleEngineEntity ruleEngineEntity, @CookieValue("MGR_ACCOUNT_ID") Integer accountId, HttpServletRequest request) throws GovernanceException {
        log.info("delete  ruleEngineEntity service ,id:{}", ruleEngineEntity.getId());
        ruleEngineEntity.setUserId(accountId);
        boolean flag = ruleEngineService.deleteRuleEngine(ruleEngineEntity, request);
        return new GovernanceResult(flag);
    }

    @PostMapping("/start")
    public GovernanceResult startRuleEngine(@RequestBody RuleEngineEntity ruleEngineEntity, @CookieValue("MGR_ACCOUNT_ID") Integer accountId, HttpServletRequest request,
                                            HttpServletResponse response) throws GovernanceException {
        log.info("update  ruleEngineStatus service ,ruleEngineEntity:{}", ruleEngineEntity);
        ruleEngineEntity.setUserId(accountId);
        boolean flag = ruleEngineService.startRuleEngine(ruleEngineEntity, request, response);
        return new GovernanceResult(flag);
    }

    @PostMapping("/detail")
    public GovernanceResult getRuleEngineDetail(@RequestBody RuleEngineEntity ruleEngineEntity, @CookieValue("MGR_ACCOUNT_ID") Integer accountId, HttpServletRequest request,
                                                HttpServletResponse response) throws GovernanceException {
        log.info("get ruleEngineDetail service ,status:{}", ruleEngineEntity.getStatus());
        ruleEngineEntity.setUserId(accountId);
        RuleEngineEntity ruleEngineDetail = ruleEngineService.getRuleEngineDetail(ruleEngineEntity, request, response);
        return new GovernanceResult(ruleEngineDetail);
    }
}
