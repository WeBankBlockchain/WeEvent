package com.webank.weevent.governance.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResponse;
import com.webank.weevent.governance.entity.TimerSchedulerEntity;
import com.webank.weevent.governance.service.TimerSchedulerService;
import com.webank.weevent.governance.utils.JwtUtils;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin
@RestController
@RequestMapping(value = "/timerScheduler")
@Slf4j
public class TimerSchedulerController {

    @Autowired
    private TimerSchedulerService timerSchedulerService;

    // get  TimerScheduler list
    @PostMapping("/list")
    public GovernanceResponse<Map<String, Object>> getTimerSchedulerList(HttpServletRequest request, @RequestBody TimerSchedulerEntity timerSchedulerEntity) throws GovernanceException {
        log.info("get TimerScheduler , timerScheduler :{}", timerSchedulerEntity);
        timerSchedulerEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        List<TimerSchedulerEntity> timerSchedulerEntityList = timerSchedulerService.getTimerSchedulerList(request, timerSchedulerEntity);
        Map<String, Object> map = new HashMap<>();
        map.put("timerSchedulerEntityList", timerSchedulerEntityList);
        map.put("totalCount", timerSchedulerEntity.getTotalCount());
        return new GovernanceResponse<>(map);
    }

    // add TimerSchedulerEntity
    @PostMapping("/add")
    public GovernanceResponse<TimerSchedulerEntity> addTimerScheduler(@Valid @RequestBody TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request,
                                              HttpServletResponse response) throws GovernanceException {
        log.info("add  timerSchedulerEntity service into db :{}", timerSchedulerEntity);
        timerSchedulerEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        TimerSchedulerEntity rule = timerSchedulerService.addTimerScheduler(timerSchedulerEntity, request, response);
        return new GovernanceResponse<>(rule);
    }

    @PostMapping("/update")
    public GovernanceResponse<Boolean> updateTimerScheduler(@RequestBody TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request,
                                                 HttpServletResponse response) throws GovernanceException {
        log.info("update  timerSchedulerEntity service ,timerSchedulerEntity:{}", timerSchedulerEntity);
        timerSchedulerEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        timerSchedulerService.updateTimerScheduler(timerSchedulerEntity, request, response);
        return new GovernanceResponse<>(true);
    }


    @PostMapping("/delete")
    public GovernanceResponse<Boolean> deleteTimerScheduler(@RequestBody TimerSchedulerEntity timerSchedulerEntity, HttpServletRequest request) throws GovernanceException {
        log.info("delete  TimerSchedulerEntity service ,id:{}", timerSchedulerEntity.getId());
        timerSchedulerEntity.setUserId(Integer.valueOf(JwtUtils.getAccountId(request)));
        timerSchedulerService.deleteTimerScheduler(timerSchedulerEntity, request);
        return new GovernanceResponse<>(true);
    }
}
