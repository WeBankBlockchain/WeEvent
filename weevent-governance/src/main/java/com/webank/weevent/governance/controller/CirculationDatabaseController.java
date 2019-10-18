package com.webank.weevent.governance.controller;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.webank.weevent.governance.entity.CirculationDatabaseEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.CirculationDatabaseService;

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
public class CirculationDatabaseController {

    @Autowired
    private CirculationDatabaseService circulationDatabaseService;

    // get all circulationDatabaseEntity service
    @PostMapping("/list")
    public GovernanceResult getCirculationDatabases(HttpServletRequest request, @RequestBody CirculationDatabaseEntity circulationDatabaseEntity) throws GovernanceException {
        log.info("get circulationDatabaseEntity:{}", circulationDatabaseEntity);
        List<CirculationDatabaseEntity> circulationDatabases = circulationDatabaseService.circulationDatabaseList(request, circulationDatabaseEntity);

        return new GovernanceResult(circulationDatabases);
    }

    // add circulationDatabaseEntity
    @PostMapping("/add")
    public GovernanceResult addCirculationDatabase(@Valid @RequestBody CirculationDatabaseEntity circulationDatabaseEntity, HttpServletRequest request,
                                                   HttpServletResponse response) throws GovernanceException {
        log.info("add  circulationDatabaseEntity service into db :{}", circulationDatabaseEntity);
        CirculationDatabaseEntity rule = circulationDatabaseService.addCirculationDatabase(circulationDatabaseEntity, request, response);
        return new GovernanceResult(rule);
    }

    @PostMapping("/update")
    public GovernanceResult updateCirculationDatabase(@RequestBody CirculationDatabaseEntity circulationDatabaseEntity, HttpServletRequest request,
                                                      HttpServletResponse response) throws GovernanceException {
        log.info("update  circulationDatabaseEntity service ,circulationDatabaseEntity:{}", circulationDatabaseEntity);
        boolean flag = circulationDatabaseService.updateCirculationDatabase(circulationDatabaseEntity, request, response);
        return new GovernanceResult(flag);
    }


    @PostMapping("/delete")
    public GovernanceResult deleteCirculationDatabase(@RequestBody CirculationDatabaseEntity circulationDatabaseEntity, HttpServletRequest request) throws GovernanceException {
        log.info("delete  circulationDatabaseEntity service ,id:{}", circulationDatabaseEntity.getId());
        boolean flag = circulationDatabaseService.deleteCirculationDatabase(circulationDatabaseEntity, request);
        return new GovernanceResult(flag);
    }
}
