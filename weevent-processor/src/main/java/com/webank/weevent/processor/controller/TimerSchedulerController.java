package com.webank.weevent.processor.controller;

import java.io.IOException;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.service.TimerSchedulerService;
import com.webank.weevent.processor.timer.TimerSchedulerJob;
import com.webank.weevent.processor.utils.BaseRspEntity;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.JsonUtil;
import com.webank.weevent.processor.utils.RetCode;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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

    @RequestMapping("/insert")
    public BaseRspEntity insertTimerScheduler(@Validated @RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        try {
            JobDataMap timerSchedulerMap = new JobDataMap();
            timerSchedulerMap.put("id", timerScheduler.getId());
            timerSchedulerMap.put("type", "createTimerTask");
            timerSchedulerMap.put("timer", JsonUtil.toJSONString(timerScheduler));
            RetCode retCode = timerSchedulerService.createTimerScheduler(timerScheduler.getId(), "timer", "timer",
                    "timer-trigger", TimerSchedulerJob.class, timerSchedulerMap, timerScheduler);
            if (1 == retCode.getErrorCode()) { //fail
                resEntity.setErrorCode(retCode.getErrorCode());
                resEntity.setErrorMsg(retCode.getErrorMsg());
            }
        } catch (IOException e) {
            log.error("insert timerScheduler fail", e);
            throw new BrokerException("insert timerScheduler fail", e);
        }

        return resEntity;
    }

    @RequestMapping("/update")
    public BaseRspEntity updateTimerScheduler(@Validated @RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        try {
            JobDataMap timerSchedulerMap = new JobDataMap();
            timerSchedulerMap.put("id", timerScheduler.getId());
            timerSchedulerMap.put("type", "updateTimerTask");
            timerSchedulerMap.put("timer", JsonUtil.toJSONString(timerScheduler));
            RetCode retCode = timerSchedulerService.createTimerScheduler(timerScheduler.getId(), "timer", "timer",
                    "timer-trigger", TimerSchedulerJob.class, timerSchedulerMap, timerScheduler);
            if (1 == retCode.getErrorCode()) { //fail
                resEntity.setErrorCode(retCode.getErrorCode());
                resEntity.setErrorMsg(retCode.getErrorMsg());
            }
        } catch (IOException e) {
            log.error("update timerScheduler fail", e);
            throw new BrokerException("update timerScheduler fail", e);
        }
        return resEntity;
    }

    @RequestMapping("/delete")
    public BaseRspEntity deleteTimerScheduler(@RequestBody TimerScheduler timerScheduler) throws BrokerException {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        this.timerSchedulerService.deleteTimerScheduler(timerScheduler);
        return resEntity;
    }

    @RequestMapping("/checkCorn")
    public BaseRspEntity checkCorn(@RequestParam("corn") String corn) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        boolean validExpression = CronExpression.isValidExpression(corn);
        resEntity.setData(validExpression);
        return resEntity;
    }
}
