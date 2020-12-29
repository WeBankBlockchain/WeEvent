package com.webank.weevent.processor.controller;

import java.util.List;

import javax.validation.Valid;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.StatisticWeEvent;
import com.webank.weevent.processor.mq.CEPRuleMQ;
import com.webank.weevent.processor.quartz.CRUDJobs;
import com.webank.weevent.processor.quartz.QuartzManager;
import com.webank.weevent.processor.service.StatisticRuleService;
import com.webank.weevent.processor.utils.BaseRspEntity;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.RetCode;
import com.webank.weevent.processor.utils.StatusCode;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CEPRuleController {

    @Autowired
    private StatisticRuleService statisticRuleService;
    @Autowired
    private QuartzManager quartzManager;


    // use the rule id to get rule detail
    @RequestMapping(value = "/updateCEPRuleById", method = RequestMethod.POST)
    public BaseRspEntity updateCEPRuleById(@Valid @RequestBody CEPRule rule) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        RetCode ret = createJob(rule, "updateCEPRuleById");
        if (ConstantsHelper.RET_FAIL.getErrorCode() != ret.getErrorCode()) { //fail
            resEntity.setErrorCode(ConstantsHelper.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(ConstantsHelper.RET_FAIL.getErrorMsg());
        }
        return resEntity;

    }

    @RequestMapping(value = "/stopCEPRuleById", method = RequestMethod.POST)
    public BaseRspEntity stopCEPRuleById(@Valid @RequestBody CEPRule rule) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        RetCode ret = createJob(rule, "stopCEPRuleById");
        if (ConstantsHelper.RET_FAIL.getErrorCode() != ret.getErrorCode()) { //fail
            resEntity.setErrorCode(ConstantsHelper.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(ConstantsHelper.RET_FAIL.getErrorMsg());
        }
        return resEntity;

    }

    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public BaseRspEntity insert(@RequestBody CEPRule rule) {
        // insert status must be 0
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        RetCode ret = createJob(rule, "insert");
        if (ConstantsHelper.RET_FAIL.getErrorCode() != ret.getErrorCode()) { //fail
            resEntity.setErrorCode(ConstantsHelper.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(ConstantsHelper.RET_FAIL.getErrorMsg());
        } else {
            resEntity.setData(rule.getId());
        }
        return resEntity;
    }


    @RequestMapping(value = "/deleteCEPRuleById", method = RequestMethod.POST)
    public BaseRspEntity deleteCEPRuleById(@RequestParam(name = "id") String id) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        createJob(id, "deleteCEPRuleById");
        try {
            RetCode ret = deleteJob(id);

            if (ConstantsHelper.RET_FAIL.getErrorCode() != ret.getErrorCode()) { //fail
                resEntity.setErrorCode(ret.getErrorCode());
                resEntity.setErrorMsg(ret.getErrorMsg());
            }
            return resEntity;
        } catch (SchedulerException e) {
            resEntity.setErrorCode(StatusCode.SCHEDULE_ERROR.getCode());
            resEntity.setErrorMsg(StatusCode.SCHEDULE_ERROR.getCodeDesc());
            return resEntity;
        }
    }

    @RequestMapping(value = "/statistic", method = RequestMethod.GET)
    public BaseRspEntity statistic(@RequestParam List<String> idList) {

        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        StatisticWeEvent getWeEventCollecttion = statisticRuleService.getStatisticWeEvent(idList);
        if (null == getWeEventCollecttion) { //fail
            resEntity.setErrorCode(ConstantsHelper.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(ConstantsHelper.RET_FAIL.getErrorMsg());
        } else { // set rule
            resEntity.setData(getWeEventCollecttion);
        }
        return resEntity;
    }


    @RequestMapping(value = "/startCEPRule", method = RequestMethod.POST)
    public BaseRspEntity startCEPRule(@Valid @RequestBody CEPRule rule) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        RetCode ret = createJob(rule, "startCEPRule");

        if (ConstantsHelper.RET_FAIL.getErrorCode() != ret.getErrorCode()) { //fail
            resEntity.setErrorCode(ConstantsHelper.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(ConstantsHelper.RET_FAIL.getErrorMsg());
        }
        return resEntity;
    }

    @GetMapping(value = "/checkWhereCondition")
    public BaseRspEntity checkWhereCondition(@RequestParam(name = "payload") String payload, @RequestParam(name = "condition") String condition) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        RetCode ret = CEPRuleMQ.checkCondition(payload, condition);

        if (ConstantsHelper.RET_FAIL.getErrorCode() != ret.getErrorCode()) { //fail
            resEntity.setErrorCode(ret.getErrorCode());
            resEntity.setErrorMsg(ret.getErrorMsg());
        }

        return resEntity;
    }

    @GetMapping(value = "/getJobDetail")
    public BaseRspEntity getJobDetail(@RequestParam(name = "id") String id) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        try {
            CEPRule rule = quartzManager.getJobDetail(id);
            resEntity.setData(rule);
            return resEntity;
        } catch (SchedulerException | BrokerException e) {
            resEntity.setErrorCode(StatusCode.SCHEDULE_ERROR.getCode());
            resEntity.setErrorMsg(StatusCode.SCHEDULE_ERROR.getCodeDesc());
            return resEntity;
        }
    }

    private RetCode createJob(CEPRule rule, String type) {

        JobDataMap jobMap = new JobDataMap();
        jobMap.put("rule", rule);
        jobMap.put("type", type);
        // set the original instance
        jobMap.put("instance", ProcessorApplication.processorConfig.getSchedulerInstanceName());
        return quartzManager.addModifyJob(rule.getId(), "rule", "rule", "rule-trigger", CRUDJobs.class, jobMap);

    }

    private RetCode createJob(String id, String type) {

        JobDataMap jobmap = new JobDataMap();
        jobmap.put("id", id);
        jobmap.put("type", type);
        // set the original instance
        jobmap.put("instance", ProcessorApplication.processorConfig.getSchedulerInstanceName());
        return quartzManager.addModifyJob(id, "rule", "rule", "rule-trigger", CRUDJobs.class, jobmap);
    }

    private RetCode deleteJob(String id) throws SchedulerException {
        return quartzManager.removeJob(id, "rule", "rule", "rule-trigger");
    }
}
