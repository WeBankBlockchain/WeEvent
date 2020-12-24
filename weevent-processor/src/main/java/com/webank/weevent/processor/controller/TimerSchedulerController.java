package com.webank.weevent.processor.controller;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.service.TimerSchedulerService;
import com.webank.weevent.processor.timer.TimerSchedulerJob;
import com.webank.weevent.processor.utils.BaseRspEntity;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.RetCode;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/timerScheduler")
public class TimerSchedulerController {

    @Autowired
    private TimerSchedulerService timerSchedulerService;

    public TimerSchedulerController(TimerSchedulerService timerSchedulerService) {
        this.timerSchedulerService = timerSchedulerService;
    }

    @PostMapping("/insert")
    public BaseRspEntity insertTimerScheduler(@Validated @RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        JobDataMap timerSchedulerMap = new JobDataMap();
        timerSchedulerMap.put("id", timerScheduler.getId());
        timerSchedulerMap.put("type", "createTimerTask");
        timerSchedulerMap.put("timer", JsonHelper.object2Json(timerScheduler));
        RetCode retCode = timerSchedulerService.createTimerScheduler(timerScheduler.getId(), "timer", "timer",
                "timer-trigger", TimerSchedulerJob.class, timerSchedulerMap, timerScheduler);
        if (ConstantsHelper.RET_FAIL.getErrorCode() == retCode.getErrorCode()) { //fail
            resEntity.setErrorCode(retCode.getErrorCode());
            resEntity.setErrorMsg(retCode.getErrorMsg());
        }

        return resEntity;
    }

    @PostMapping("/update")
    public BaseRspEntity updateTimerScheduler(@Validated @RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        JobDataMap timerSchedulerMap = new JobDataMap();
        timerSchedulerMap.put("id", timerScheduler.getId());
        timerSchedulerMap.put("type", "updateTimerTask");
        timerSchedulerMap.put("timer", JsonHelper.object2Json(timerScheduler));
        RetCode retCode = timerSchedulerService.createTimerScheduler(timerScheduler.getId(), "timer", "timer",
                "timer-trigger", TimerSchedulerJob.class, timerSchedulerMap, timerScheduler);
        if (ConstantsHelper.RET_FAIL.getErrorCode() == retCode.getErrorCode()) { //fail
            resEntity.setErrorCode(retCode.getErrorCode());
            resEntity.setErrorMsg(retCode.getErrorMsg());
        }
        return resEntity;
    }

    @PostMapping("/delete")
    public BaseRspEntity deleteTimerScheduler(@RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        this.timerSchedulerService.deleteTimerScheduler(timerScheduler);
        return resEntity;
    }

    @PostMapping("/checkCorn")
    public BaseRspEntity checkCorn(@RequestParam("corn") String corn) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        boolean validExpression = CronExpression.isValidExpression(corn);
        resEntity.setData(validExpression);
        return resEntity;
    }
}
