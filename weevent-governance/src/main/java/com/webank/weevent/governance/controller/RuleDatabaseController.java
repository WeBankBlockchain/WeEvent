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

    // get all circulationDatabaseEntity service
    @PostMapping("/list")
    public GovernanceResult getCirculationDatabases(HttpServletRequest request, @RequestBody RuleDatabaseEntity ruleDatabaseEntity) throws GovernanceException {
        log.info("get circulationDatabaseEntity:{}", ruleDatabaseEntity);
        List<RuleDatabaseEntity> circulationDatabases = ruleDatabaseService.circulationDatabaseList(request, ruleDatabaseEntity);

        return new GovernanceResult(circulationDatabases);
    }

    // add circulationDatabaseEntity
    @PostMapping("/add")
    public GovernanceResult addCirculationDatabase(@Valid @RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request,
                                                   HttpServletResponse response) throws GovernanceException {
        log.info("add  circulationDatabaseEntity service into db :{}", ruleDatabaseEntity);
        RuleDatabaseEntity rule = ruleDatabaseService.addCirculationDatabase(ruleDatabaseEntity, request, response);
        return new GovernanceResult(rule);
    }

    @PostMapping("/update")
    public GovernanceResult updateCirculationDatabase(@RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request,
                                                      HttpServletResponse response) throws GovernanceException {
        log.info("update  circulationDatabaseEntity service ,circulationDatabaseEntity:{}", ruleDatabaseEntity);
        boolean flag = ruleDatabaseService.updateCirculationDatabase(ruleDatabaseEntity, request, response);
        return new GovernanceResult(flag);
    }


    @PostMapping("/delete")
    public GovernanceResult deleteCirculationDatabase(@RequestBody RuleDatabaseEntity ruleDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        log.info("delete  circulationDatabaseEntity service ,id:{}", ruleDatabaseEntity.getId());
        boolean flag = ruleDatabaseService.deleteCirculationDatabase(ruleDatabaseEntity, request);
        return new GovernanceResult(flag);
    }
}
