package com.webank.weevent.processor.controller;

import java.util.List;

import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.scheduler.TimerSchedulerJob;
import com.webank.weevent.processor.utils.BaseRspEntity;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/timerScheduler")
public class TimerSchedulerController {

    @Autowired
    private TimerSchedulerJob timerSchedulerJob;

    @RequestMapping("/insert")
    public BaseRspEntity insertTimerScheduler(@RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        TimerScheduler scheduler = timerSchedulerJob.insertTimerScheduler(timerScheduler);
        resEntity.setData(scheduler);
        return resEntity;
    }

    @RequestMapping("/list")
    public BaseRspEntity timerSchedulerList(@RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        List<TimerScheduler> timerSchedulerList = timerSchedulerJob.timerSchedulerList(timerScheduler);
        resEntity.setData(timerSchedulerList);
        return resEntity;
    }


    @RequestMapping("/update")
    public BaseRspEntity updateTimerScheduler(@RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        TimerScheduler scheduler = timerSchedulerJob.updateTimerScheduler(timerScheduler);
        resEntity.setData(scheduler);
        return resEntity;
    }

    @RequestMapping("/delete")
    public BaseRspEntity deleteTimerScheduler(@RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        timerSchedulerJob.deleteTimerScheduler(timerScheduler);
        return resEntity;
    }

}
