package com.webank.weevent.governance.controller;


import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.TimerSchedulerEntity;
import com.webank.weevent.governance.service.TimerSchedulerService;
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
@RequestMapping(value = "/timerScheduler")
@Slf4j
public class TimerSchedulerController {

    @Autowired
    private TimerSchedulerService timerSchedulerService;

    // get  TimerScheduler list
    @PostMapping("/list")
    public GovernanceResult getTimerSchedulerList(HttpServletRequest request, @RequestBody TimerSchedulerEntity timerSchedulerEntity) throws GovernanceException {
        log.info("get TimerScheduler , timerScheduler :{}", timerSchedulerEntity);
        timerSchedulerEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        List<TimerSchedulerEntity> timerSchedulerEntityList = timerSchedulerService.getTimerSchedulerList(request, timerSchedulerEntity);
        GovernanceResult governanceResult = new GovernanceResult(timerSchedulerEntityList);
        governanceResult.setTotalCount(timerSchedulerEntity.getTotalCount());
        return governanceResult;
    }

    // add TimerSchedulerEntity
    @PostMapping("/add")
    public GovernanceResult addTimerScheduler(@Valid @RequestBody TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request,
                                              HttpServletResponse response) throws GovernanceException {
        log.info("add  timerSchedulerEntity service into db :{}", timerSchedulerEntity);
        timerSchedulerEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        TimerSchedulerEntity rule = timerSchedulerService.addTimerScheduler(timerSchedulerEntity, request, response);
        return new GovernanceResult(rule);
    }

    @PostMapping("/update")
    public GovernanceResult updateTimerScheduler(@RequestBody TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request,
                                                 HttpServletResponse response) throws GovernanceException {
        log.info("update  timerSchedulerEntity service ,timerSchedulerEntity:{}", timerSchedulerEntity);
        timerSchedulerEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        timerSchedulerService.updateTimerScheduler(timerSchedulerEntity, request, response);
        return new GovernanceResult(true);
    }


    @PostMapping("/delete")
    public GovernanceResult deleteTimerScheduler(@RequestBody TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request) throws GovernanceException {
        log.info("delete  TimerSchedulerEntity service ,id:{}", timerSchedulerEntity.getId());
        timerSchedulerEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        timerSchedulerService.deleteTimerScheduler(timerSchedulerEntity, request);
        return new GovernanceResult(true);
    }
}
