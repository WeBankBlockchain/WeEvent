package com.webank.weevent.processor.service;

import java.text.SimpleDateFormat;
import java.util.List;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.CEPRuleExample;
import com.webank.weevent.processor.utils.Constants;
import com.webank.weevent.processor.utils.RetCode;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CEPRuleServiceImpl implements CEPRuleService {

    @Override
    public int getCountByCondition(CEPRuleExample cEPRuleExample) {
        return new Long(cepRuleMapper.countByExample(cEPRuleExample)).intValue();
    }

    @Override
    public RetCode setCEPRule(String id, int type) {
        CEPRule rule = cepRuleMapper.selectByPrimaryKey(id);
        if (type == Constants.RULE_STATUS_START) {
            rule.setStatus(Constants.RULE_STATUS_START);// 1 is represent start status
        }else{
            rule.setStatus(Constants.RULE_STATUS_DELETE);// 2 is represent delete status
        }
        int ret = cepRuleMapper.updateByPrimaryKeySelective(rule);
        if (ret != Constants.SUCCESS_CODE) {
            return RetCode.mark(Constants.FAIL_CODE, "delete fail");
        }
        return RetCode.mark(Constants.SUCCESS_CODE, "success");
    }

    @Override
    public List<CEPRule> selectByRuleName(String ruleName) {
        return cepRuleMapper.selectByRuleName(ruleName);
    }

    @Override
    public CEPRule selectByPrimaryKey(String id) {
        return cepRuleMapper.selectByPrimaryKey(id);
    }


    @Override
    public RetCode updateByPrimaryKey(CEPRule record) {
        int count = cepRuleMapper.updateByPrimaryKey(record);
        if (count > 0) {
            return RetCode.mark(Constants.SUCCESS_CODE,Constants.MESSAGE_SUCCESS);
        }
        return RetCode.mark(Constants.FAIL_CODE,Constants.MESSAGE_FAIL);
    }


    @Override
    public RetCode updateByPrimaryKeySelective(CEPRule record) {
        int count = cepRuleMapper.updateByPrimaryKeySelective(record);
        if (count > 0) {
            return RetCode.mark(Constants.SUCCESS_CODE,Constants.MESSAGE_SUCCESS);
        }
        return RetCode.mark(Constants.FAIL_CODE,Constants.MESSAGE_FAIL);
    }

    @Override
    public List<CEPRule> getCEPRuleList(String ruleName) {
        List<CEPRule> CEPRuleList = cepRuleMapper.getCEPRuleList(ruleName);
        return CEPRuleList;
    }


    @Autowired
    CEPRuleMapper cepRuleMapper;

    public CEPRuleMapper getCepRuleMapper() {
        return cepRuleMapper;
    }

    public void setCepRuleMapper(CEPRuleMapper cepRuleMapper) {
        this.cepRuleMapper = cepRuleMapper;
    }

    @Override
    public RetCode insert(CEPRule record) {
        // checkPayload
        if (StringUtils.isBlank(record.getRuleName()) || record.getRuleName().isEmpty()) {
            return RetCode.mark(280001, "rule name is blank");
        }
        String payload = record.getPayloay();
        if (payload.isEmpty() || StringUtils.isBlank(payload)) {
            return RetCode.mark(280002, "payload  is blank");
        }
        // check payloay
        boolean isRight = isJSON2(record.getPayloay());
        if (!isRight) {
            return RetCode.mark(270007, "payload is not a json");
        }
        // check topic
        if (!checkTopic(record.getFromDestination())) {
            log.info("the topic is not exist");
            return RetCode.mark(270006, "the topic is not exist");
        }
        // check the broker

        // check the database


        record.setId(getGuid());
        record.setStatus(0); //default the status
        Integer ret = cepRuleMapper.insert(record);
        if (ret != 1) {
            return RetCode.mark(270005, "insert fail");
        }
        return RetCode.mark(0, "success");
    }


    private boolean checkTopic(String topicName) {
        try {
            IWeEventClient weEventClient = IWeEventClient.build("http://182.254.159.91:8090/weevent");
            return weEventClient.exist(topicName);
        } catch (BrokerException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isJSON2(String str) {
        boolean result = false;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    //  generator the id
    private volatile int guid = 100;

    private String getGuid() {
        guid += 1;
        long now = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        String time = dateFormat.format(now);
        String info = now + "";
        int ran = 0;
        if (guid > 999) {
            guid = 100;
        }
        ran = guid;
        return time + info.substring(2, info.length()) + ran;
    }

}
