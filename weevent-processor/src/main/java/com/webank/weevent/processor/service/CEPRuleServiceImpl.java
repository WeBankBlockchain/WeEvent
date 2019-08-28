package com.webank.weevent.processor.service;

import java.util.List;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CEPRuleServiceImpl implements CEPRuleService {
    @Override
    public CEPRule selectByRuleName(String ruleName) {
        return cepRuleMapper.selectByRuleName(ruleName);
    }

    @Override
    public CEPRule selectByPrimaryKey(Integer id) {
        return cepRuleMapper.selectByPrimaryKey(id);
    }


    @Override
    public int updateByPrimaryKey(CEPRule record) {
        cepRuleMapper.updateByPrimaryKey(record);
        return 0;
    }

    @Override
    public List<CEPRule> getCEPRuleList(String ruleName) {
        List<CEPRule> CEPRuleList = cepRuleMapper.getCEPRuleList(ruleName);
        return CEPRuleList;
    }


    @Autowired
    CEPRuleMapper cepRuleMapper;

    @Override
    public int insert(CEPRule record) {
        return cepRuleMapper.insert(record);
    }



}
