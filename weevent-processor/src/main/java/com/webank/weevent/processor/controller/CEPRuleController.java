package com.webank.weevent.processor.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import com.webank.weevent.processor.utils.BaseRspEntity;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.service.CEPRuleServiceImpl;
import com.webank.weevent.processor.utils.Constants;
import com.webank.weevent.processor.utils.RetCode;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
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
public class CEPRuleController extends BaseController {

    @Autowired
    private CEPRuleServiceImpl cepRuleService;

    @RequestMapping("/getCEPRuleById")
    @ResponseBody
    public BaseRspEntity getCEPRuleById(@RequestParam(name = "id") String id) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        CEPRule cepRule = cepRuleService.selectByPrimaryKey(id);
        resEntity.setData(cepRule);
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }

    @RequestMapping("/getCEPRuleByName")
    @ResponseBody
    public BaseRspEntity getCEPRuleByName(@RequestParam(name = "ruleName") String ruleName) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        if (StringUtils.isBlank(ruleName) || ruleName.isEmpty()) {
            resEntity.setErrorCode(280001);
            resEntity.setErrorMsg("fail");
            return resEntity;
        }
        List<CEPRule> cepRule = cepRuleService.selectByRuleName(ruleName);
        resEntity.setData(cepRule);
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }

    @RequestMapping("/getCEPRuleListByPage")
    @ResponseBody
    public BaseRspEntity getCEPRuleListByPage(int currPage, int pageSize) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        if (currPage <= 0 || pageSize <= 0 || pageSize > 50) {
            resEntity.setErrorCode(Constants.SUCCESS_CODE);
            resEntity.setErrorMsg("the currPage or pageSize is not valid");
            resEntity.setData(new ArrayList<>());
            return resEntity;
        }
        List<CEPRule> cepRule = cepRuleService.getCEPRuleListByPage(currPage, pageSize);
        resEntity.setData(cepRule);
        if (cepRule == null) {
            resEntity.setErrorCode(Constants.SUCCESS_CODE);
            resEntity.setErrorMsg("fail");
        }
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }

    @RequestMapping("/getCEPRuleList")
    @ResponseBody
    public BaseRspEntity getCEPRuleList(@RequestParam(name = "ruleName") String ruleName) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        List<CEPRule> cepRule = cepRuleService.getCEPRuleList(ruleName);
        resEntity.setData(cepRule);
        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
        return resEntity;
    }

    @RequestMapping(value = "/updateCEPRuleById", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity updateCEPRuleById(@Valid @RequestBody CEPRule rule) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        RetCode ret = cepRuleService.updateByPrimaryKeySelective(rule);
        if (!ret.getErrorCode().equals(1)) { //fail
            resEntity.setErrorCode(Constants.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(Constants.RET_FAIL.getErrorMsg());
        }
        return resEntity;

    }

    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity insert(@Param("ruleName") String ruleName, @Param("createdTime") long createdTime, @Param("updatedTime") long updatedTime) {
        // insert status must be 0
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        CEPRule rule = new CEPRule();
        rule.setRuleName(ruleName);
        rule.setUpdatedTime(new Date(updatedTime));
        rule.setCreatedTime(new Date(createdTime));

        String ret = cepRuleService.insert(rule);
        if ("-1".equals(ret)) { //fail
            resEntity.setErrorCode(Constants.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(Constants.RET_FAIL.getErrorMsg());
        } else {
            resEntity.setData(ret);
        }

        return resEntity;
    }


    @RequestMapping(value = "/deleteCEPRuleById", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity deleteCEPRuleById(@RequestParam(name = "id") String id) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        RetCode ret = cepRuleService.setCEPRule(id, Constants.RULE_STATUS_DELETE);

        if (!ret.getErrorCode().equals(1)) { //fail
            resEntity.setErrorCode(ret.getErrorCode());
            resEntity.setErrorMsg(ret.getErrorMsg());
        }

        log.info("cepRule:{}", JSONArray.toJSON(ret));
        return resEntity;
    }

    @RequestMapping(value = "/startCEPRule", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity startCEPRule(@Valid @RequestBody CEPRule rule) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        RetCode ret = cepRuleService.setCEPRule(rule);

        if (!ret.getErrorCode().equals(1)) { //fail
            resEntity.setErrorCode(Constants.RET_FAIL.getErrorCode());
            resEntity.setErrorMsg(Constants.RET_FAIL.getErrorMsg());
        }

        log.info("cepRule:{}", JSONArray.toJSON(ret));
        return resEntity;
    }
}
