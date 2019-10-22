package com.webank.weevent.processor.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.job.CRUDJobs;
import com.webank.weevent.processor.utils.BaseRspEntity;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.service.CEPRuleServiceImpl;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.RetCode;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RestController
public class CEPRuleController {

    @Autowired
    private CEPRuleServiceImpl cepRuleService;

    @RequestMapping("/getCEPRuleById")
    public BaseRspEntity getCEPRuleById(@RequestParam(name = "id") String id) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        CEPRule cepRule = cepRuleService.selectByPrimaryKey(id);
        resEntity.setData(cepRule);
        createJob(cepRule, "getCEPRuleById");
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }

    @RequestMapping("/getCEPRuleByName")
    public BaseRspEntity getCEPRuleByName(@RequestParam(name = "ruleName") String ruleName) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        if (StringUtils.isBlank(ruleName) || ruleName.isEmpty()) {
            resEntity.setErrorCode(280001);
            resEntity.setErrorMsg("fail");
            return resEntity;
        }
        List<CEPRule> cepRule = cepRuleService.selectByRuleName(ruleName);
        createJob(cepRule, "getCEPRuleByName");
        resEntity.setData(cepRule);
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }

    // get user's rules
    @RequestMapping("/getRulesByUserId")
    public BaseRspEntity getRulesByUserId(@RequestParam(name = "userId") String userId) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        if (StringUtils.isBlank(userId) || userId.isEmpty()) {
            resEntity.setErrorCode(280001);
            resEntity.setErrorMsg("fail");
            return resEntity;
        }
        List<CEPRule> cepRule = cepRuleService.getRulesByUserId(userId);
        resEntity.setData(cepRule);
        createJob(cepRule, "getRulesByUserId");
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }

    // user has some rules in the broker
    @RequestMapping("/getRulesByUserIdAndBroker")
    public BaseRspEntity getRulesByUserIdAndBroker(@RequestParam(name = "userId") String userId, @RequestParam(name = "brokerId") String brokerId) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        if (StringUtils.isBlank(userId) || userId.isEmpty()) {
            resEntity.setErrorCode(280001);
            resEntity.setErrorMsg("fail");
            return resEntity;
        }
        List<CEPRule> cepRule = cepRuleService.getRulesByUserIdAndBroker(userId, brokerId);
        resEntity.setData(cepRule);
        createJob(cepRule, "getRulesByUserIdAndBroker");
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }


    @RequestMapping("/getCEPRuleListByPage")
    public BaseRspEntity getCEPRuleListByPage(int currPage, int pageSize) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        if (currPage <= 0 || pageSize <= 0 || pageSize > 50) {
            resEntity.setErrorCode(ConstantsHelper.SUCCESS_CODE);
            resEntity.setErrorMsg("the currPage or pageSize is not valid");
            resEntity.setData(new ArrayList<>());
            return resEntity;
        }
        List<CEPRule> cepRule = cepRuleService.getCEPRuleListByPage(currPage, pageSize);
        resEntity.setData(cepRule);
        if (cepRule == null) {
            resEntity.setErrorCode(ConstantsHelper.SUCCESS_CODE);
            resEntity.setErrorMsg("fail");
        } else {
            createJob(cepRule, "getCEPRuleListByPage");
        }
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }

    // use rule name to get rule list
    @RequestMapping("/getCEPRuleList")
    public BaseRspEntity getCEPRuleList(@RequestParam(name = "ruleName") String ruleName) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        List<CEPRule> cepRule = cepRuleService.getCEPRuleList(ruleName);
        resEntity.setData(cepRule);
        createJob(cepRule, "getCEPRuleList");
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }

    // use the rule id to get rule detail
    @RequestMapping(value = "/updateCEPRuleById", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity updateCEPRuleById(@Valid @RequestBody CEPRule rule) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        RetCode ret = cepRuleService.updateByPrimaryKeySelective(rule);
        if (!ret.getErrorCode().equals(1)) { //fail
            resEntity.setErrorCode(ConstantsHelper.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(ConstantsHelper.RET_FAIL.getErrorMsg());
        } else {
            createJob(rule, "updateCEPRuleById");
        }
        return resEntity;

    }

    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity insert(@RequestBody CEPRule rule) {
        // insert status must be 0
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        String ret = cepRuleService.insert(rule);
        if ("-1".equals(ret)) { //fail
            resEntity.setErrorCode(ConstantsHelper.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(ConstantsHelper.RET_FAIL.getErrorMsg());
        } else {
            resEntity.setData(ret);
            createJob(rule, "insert");
        }
        return resEntity;
    }

    @RequestMapping(value = "/insertByParam", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity insert(@Param("ruleName") String ruleName, @Param("createdTime") long createdTime,
                                @Param("updatedTime") long updatedTime, @Param("userId") String userId, @Param("brokerId") String brokerId) {
        // insert status must be 0
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        CEPRule rule = new CEPRule();
        rule.setRuleName(ruleName);
        rule.setUserId(userId);
        rule.setBrokerId(brokerId);
        rule.setUpdatedTime(new Date(updatedTime));
        rule.setCreatedTime(new Date(createdTime));
        String ret = cepRuleService.insert(rule);
        if ("-1".equals(ret)) { //fail
            resEntity.setErrorCode(ConstantsHelper.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(ConstantsHelper.RET_FAIL.getErrorMsg());
        } else {
            resEntity.setData(ret);
            createJob(rule, "insertByParam");
        }
        return resEntity;
    }


    @RequestMapping(value = "/deleteCEPRuleById", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity deleteCEPRuleById(@RequestParam(name = "id") String id) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        RetCode ret = cepRuleService.setCEPRule(id, ConstantsHelper.RULE_STATUS_DELETE);

        if (!ret.getErrorCode().equals(1)) { //fail
            resEntity.setErrorCode(ret.getErrorCode());
            resEntity.setErrorMsg(ret.getErrorMsg());
        } else {
            // delete the job
            deleteJob(id);
        }

        log.info("cepRule:{}", JSONArray.toJSON(ret));
        return resEntity;
    }

    @RequestMapping(value = "/startCEPRule", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity startCEPRule(@Valid @RequestBody CEPRule rule) {
        BaseRspEntity resEntity = new BaseRspEntity(ConstantsHelper.RET_SUCCESS);
        RetCode ret = cepRuleService.setCEPRule(rule);

        if (!ret.getErrorCode().equals(1)) { //fail
            resEntity.setErrorCode(ConstantsHelper.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(ConstantsHelper.RET_FAIL.getErrorMsg());
        } else {
            createJob(rule, "startCEPRule");
        }
        log.info("cepRule:{}", JSONArray.toJSON(ret));
        return resEntity;
    }

    private void createJob(CEPRule rule, String type) {
        try {
            JobDataMap ruleMap = new JobDataMap();
            //String type = "write";
            ruleMap.put("rule", rule);
            ruleMap.put("type", type);
            ruleMap.put("id", rule.getId());
            JobKey jobkey = new JobKey(new Date().toString(), type);
            JobDetail jobDetail = JobBuilder.newJob(CRUDJobs.class)
                    .withIdentity(jobkey)
                    .requestRecovery()
                    .setJobData(ruleMap)
                    .build();
            // if exist ,then replace
            if (ProcessorApplication.scheduler.checkExists(jobkey)) {
                ProcessorApplication.scheduler.addJob(jobDetail, true);
            } else {
                // if not exist ,then insert,and can not replace
                ProcessorApplication.scheduler.addJob(jobDetail, false);
            }


        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private void createJob(List<CEPRule> rule, String type) {
        try {
            JobDataMap ruleMap = new JobDataMap();
            ruleMap.put("rule", rule);
            ruleMap.put("type", type);
            JobKey jobkey = new JobKey(type.concat(new Date().toString()), type);
            JobDetail jobDetail = JobBuilder.newJob(CRUDJobs.class)
                    .withIdentity(jobkey)
                    .requestRecovery()
                    .setJobData(ruleMap)
                    .build();
            // if exist ,then replace
            if (ProcessorApplication.scheduler.checkExists(jobkey)) {
                ProcessorApplication.scheduler.addJob(jobDetail, true);
            } else {
                // if not exist ,then insert,and can not replace
                ProcessorApplication.scheduler.addJob(jobDetail, false);
            }


        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    private void deleteJob(String id) {
        try {
            JobKey jobkey = new JobKey(id, "write");
            if (ProcessorApplication.scheduler.checkExists(jobkey)) {
                ProcessorApplication.scheduler.deleteJob(jobkey);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
