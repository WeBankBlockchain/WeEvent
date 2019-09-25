package com.webank.weevent.processor.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.webank.weevent.processor.cache.CEPRuleCache;
import com.webank.weevent.processor.utils.BaseRspEntity;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.service.CEPRuleServiceImpl;
import com.webank.weevent.processor.utils.Constants;
import com.webank.weevent.processor.utils.RetCode;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
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
//    @Autowired
//    private CEPRuleCache initRule;
//
//    @RequestMapping("/refresh")
//    public BaseRspEntity refresh() {
//        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
//        List<CEPRule> cepRule = initRule.init();
//        resEntity.setData(cepRule);
//        log.info("cepRule:{}", JSONArray.toJSON(cepRule));
//        return resEntity;
//    }

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
        System.out.println("example:" + ruleName.toString());
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
        resEntity.setErrorCode(ret.getErrorCode());
        resEntity.setErrorMsg(ret.getErrorMsg());
        return resEntity;
    }

    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity insert(@Valid @RequestBody CEPRule rule) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        RetCode ret = cepRuleService.insert(rule);
        resEntity.setErrorCode(ret.getErrorCode());
        resEntity.setErrorMsg(ret.getErrorMsg());

        return resEntity;
    }

    @RequestMapping(value = "/deleteCEPRuleById", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity deleteCEPRuleById(@RequestParam(name = "id") String id) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        RetCode ret = cepRuleService.setCEPRule(id, Constants.RULE_STATUS_DELETE);
        if (!ret.getErrorCode().equals(Constants.SUCCESS_CODE)) {
            resEntity.setErrorCode(ret.getErrorCode());
            resEntity.setErrorMsg(ret.getErrorMsg());
        }
        log.info("cepRule:{}", JSONArray.toJSON(ret));
        return resEntity;
    }

    @RequestMapping(value = "/startCEPRule", method = RequestMethod.POST)
    @ResponseBody
    public BaseRspEntity startCEPRule(@RequestParam(name = "id") String id) {
        BaseRspEntity resEntity = new BaseRspEntity(Constants.RET_SUCCESS);
        RetCode ret = cepRuleService.setCEPRule(id, Constants.RULE_STATUS_START);
        if (!ret.getErrorCode().equals(Constants.SUCCESS_CODE)) {
            resEntity.setErrorCode(ret.getErrorCode());
            resEntity.setErrorMsg(ret.getErrorMsg());
        }
        log.info("cepRule:{}", JSONArray.toJSON(ret));
        return resEntity;
    }
}
