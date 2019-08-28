package com.webank.weevent.processor.controller;

import java.util.List;

import javax.validation.Valid;

import com.webank.weevent.processor.utils.BaseRspEntity;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.service.CEPRuleServiceImpl;

import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.lang3.StringUtils;

import static com.webank.weevent.processor.utils.Constants.SUCCESS;

@RestController
public class CEPRuleController extends BaseController {

    @Autowired
    private CEPRuleServiceImpl cepRuleService;

    @RequestMapping("/getCEPRuleById")
    @ResponseBody
    public BaseRspEntity getCEPRuleById(@RequestParam(name = "id") Integer id) {
        BaseRspEntity resEntity = new BaseRspEntity(SUCCESS);
        CEPRule cepRule = cepRuleService.selectByPrimaryKey(id);
        resEntity.setData(cepRule);
        resEntity.setErrorCode(0);
        resEntity.setErrorMsg("success");
        System.out.println(JSONArray.toJSON(cepRule));
        if (cepRule == null) {
            resEntity.setErrorCode(0);
            resEntity.setErrorMsg("success");
            System.out.println(JSONArray.toJSON(cepRule));
        }
        return resEntity;
    }

    @RequestMapping("/getCEPRuleByName")
    @ResponseBody
    public BaseRspEntity getCEPRuleByName(@RequestParam(name = "ruleName") String ruleName) {
        BaseRspEntity resEntity = new BaseRspEntity(SUCCESS);
        if (StringUtils.isBlank(ruleName) || ruleName.isEmpty()) {
            resEntity.setErrorCode(280001);
            resEntity.setErrorMsg("fail");
            return resEntity;
        }
        CEPRule cepRule = cepRuleService.selectByRuleName(ruleName);
        resEntity.setData(cepRule);
        resEntity.setErrorCode(0);
        resEntity.setErrorMsg("success");
        System.out.println(JSONArray.toJSON(cepRule));
        if (cepRule == null) {
            resEntity.setErrorCode(0);
            resEntity.setErrorMsg("fail");
            System.out.println(JSONArray.toJSON(cepRule));
        }
        return resEntity;
    }

    @RequestMapping("/getCEPRuleList")
    @ResponseBody
    public BaseRspEntity getCEPRuleList(@RequestParam(name = "ruleName") String ruleName) {
        System.out.println("example:" + ruleName.toString());
        BaseRspEntity resEntity = new BaseRspEntity(SUCCESS);
        //List<cepRule> a= new List();
        List<CEPRule> cepRule = cepRuleService.getCEPRuleList(ruleName);
        resEntity.setData(cepRule);
        resEntity.setErrorCode(0);
        resEntity.setErrorMsg("success");
        System.out.println(JSONArray.toJSON(cepRule));
        if (cepRule == null) {
            resEntity.setErrorCode(0);
            resEntity.setErrorMsg("success");
            System.out.println(JSONArray.toJSON(cepRule));
        }
        return resEntity;
    }

    @RequestMapping("/updateCEPById")
    @ResponseBody
    public BaseRspEntity updateCEPById(@Valid @RequestBody CEPRule rule) {
        BaseRspEntity resEntity = new BaseRspEntity(SUCCESS);
        Integer cepRule = cepRuleService.updateByPrimaryKey(rule);
        resEntity.setData(cepRule);
        resEntity.setErrorCode(0);
        resEntity.setErrorMsg("success");
        System.out.println(JSONArray.toJSON(cepRule));
        if (cepRule == null) {
            resEntity.setErrorCode(0);
            resEntity.setErrorMsg("success");
            System.out.println(JSONArray.toJSON(cepRule));
        }
        return resEntity;
    }

    @RequestMapping("/insert")
    @ResponseBody
    public BaseRspEntity insert(@Valid @RequestBody CEPRule rule) {
        //  需要对参数进行解析
        BaseRspEntity resEntity = new BaseRspEntity(SUCCESS);
        Integer cepRule = cepRuleService.insert(rule);
        resEntity.setErrorCode(0);
        resEntity.setErrorMsg("success");
        System.out.println(JSONArray.toJSON(cepRule));
        if (cepRule == null) {
            resEntity.setErrorCode(0);
            resEntity.setErrorMsg("success");
            System.out.println(JSONArray.toJSON(cepRule));
        }
        return resEntity;
    }
}
