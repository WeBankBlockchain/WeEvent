package com.webank.weevent.governance.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.webank.weevent.governance.code.ErrorCode;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.BrokerService;
import com.webank.weevent.governance.service.RuleEngineService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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


    // get all broker service
    @PostMapping("/list")
    public GovernanceResult getRuleEngines(HttpServletRequest request, RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        log.info("get all ruleEngine ");
        List<RuleEngineEntity> ruleEngines = ruleEngineService.getRuleEngines(request, ruleEngineEntity);
        return new GovernanceResult(ruleEngines);
    }

    // get RuleEngineEntity service by id
    @PostMapping("/add")
    public GovernanceResult addBroker(@Valid @RequestBody RuleEngineEntity ruleEngineEntity, HttpServletRequest request,
                                      HttpServletResponse response) throws GovernanceException {
        log.info("add  ruleEngineEntity service into db " + ruleEngineEntity);
        boolean flag = ruleEngineService.addRuleEngine(ruleEngineEntity, request, response);
        return new GovernanceResult(flag);
    }

    @PostMapping("/update")
    public GovernanceResult updateBroker(@RequestBody RuleEngineEntity ruleEngineEntity, HttpServletRequest request,
                                         HttpServletResponse response) throws GovernanceException {
        log.info("update  ruleEngineEntity service ,RuleEngineEntity: " + ruleEngineEntity);
        boolean flag = ruleEngineService.updateRuleEngine(ruleEngineEntity, request, response);
        return new GovernanceResult(flag);
    }

    @PostMapping("/delete")
    public GovernanceResult deleteBroker(@RequestBody RuleEngineEntity ruleEngineEntity, HttpServletRequest request) throws GovernanceException {
        log.info("delete  ruleEngineEntity service ,id: " + ruleEngineEntity.getId());
        boolean flag = ruleEngineService.deleteRuleEngine(ruleEngineEntity, request);
        return new GovernanceResult(flag);

    }
}
