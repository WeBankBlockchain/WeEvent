package com.webank.weevent.governance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.service.RuleEngineService;
import com.webank.weevent.governance.utils.JwtUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public GovernanceResult<Map<String,Object>> getRuleEngines(HttpServletRequest request, @RequestBody RuleEngineEntity ruleEngineEntity) throws GovernanceException {
        log.info("get ruleEngines , ruleEngineEntity :{}", ruleEngineEntity);
        ruleEngineEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        List<RuleEngineEntity> ruleEngines = ruleEngineService.getRuleEngines(request, ruleEngineEntity);
        Map<String,Object> map = new HashMap<>();
        map.put("ruleEngines", ruleEngines);
        map.put("totalCount", ruleEngineEntity.getTotalCount());
        return new GovernanceResult<>(map);
    }

    // add RuleEngineEntity
    @PostMapping("/add")
    public GovernanceResult<RuleEngineEntity> addRuleEngine(@Valid @RequestBody RuleEngineEntity ruleEngineEntity, HttpServletRequest request,
                                          HttpServletResponse response) throws GovernanceException {
        log.info("add  ruleEngineEntity service into db :{}", ruleEngineEntity);
        ruleEngineEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        RuleEngineEntity rule = ruleEngineService.addRuleEngine(ruleEngineEntity, request, response);
        return new GovernanceResult<>(rule);
    }

    @PostMapping("/update")
    public GovernanceResult<Boolean> updateRuleEngine(@RequestBody RuleEngineEntity ruleEngineEntity, HttpServletRequest request,
                                             HttpServletResponse response) throws GovernanceException {
        log.info("update  ruleEngineEntity service ,ruleEngineEntity:{}", ruleEngineEntity);
        ruleEngineEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        boolean flag = ruleEngineService.updateRuleEngine(ruleEngineEntity, request, response);
        return new GovernanceResult<>(flag);
    }

    @PostMapping("/updateStatus")
    public GovernanceResult<Boolean> updateRuleEngineStatus(@RequestBody RuleEngineEntity ruleEngineEntity, HttpServletRequest request,
                                                   HttpServletResponse response) throws GovernanceException {
        log.info("update  ruleEngineStatus service ,status:{}", ruleEngineEntity.getStatus());
        ruleEngineEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        boolean flag = ruleEngineService.updateRuleEngineStatus(ruleEngineEntity, request, response);
        return new GovernanceResult<>(flag);
    }

    @PostMapping("/delete")
    public GovernanceResult<Boolean> deleteBroker(@RequestBody RuleEngineEntity ruleEngineEntity, HttpServletRequest request) throws GovernanceException {
        log.info("delete  ruleEngineEntity service ,id:{}", ruleEngineEntity.getId());
        ruleEngineEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        boolean flag = ruleEngineService.deleteRuleEngine(ruleEngineEntity, request);
        return new GovernanceResult<>(flag);
    }

    @PostMapping("/start")
    public GovernanceResult<Boolean> startRuleEngine(@RequestBody RuleEngineEntity ruleEngineEntity, HttpServletRequest request,
                                            HttpServletResponse response) throws GovernanceException {
        log.info("update  ruleEngineStatus service ,ruleEngineEntity:{}", ruleEngineEntity);
        ruleEngineEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        boolean flag = ruleEngineService.startRuleEngine(ruleEngineEntity, request, response);
        return new GovernanceResult<>(flag);
    }

    @PostMapping("/detail")
    public GovernanceResult<RuleEngineEntity> getRuleEngineDetail(@RequestBody RuleEngineEntity ruleEngineEntity, HttpServletRequest request,
                                                HttpServletResponse response) throws GovernanceException {
        log.info("get ruleEngineDetail service ,status:{}", ruleEngineEntity.getStatus());
        ruleEngineEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        RuleEngineEntity ruleEngineDetail = ruleEngineService.getRuleEngineDetail(ruleEngineEntity, request, response);
        return new GovernanceResult<>(ruleEngineDetail);
    }
}
