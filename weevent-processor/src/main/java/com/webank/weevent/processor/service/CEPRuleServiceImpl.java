package com.webank.weevent.processor.service;

import java.util.List;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;

import com.webank.weevent.processor.model.CEPRuleExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CEPRuleServiceImpl implements CEPRuleService {
    @Override
    public CEPRule selectByRuleName(String ruleName) {
        return cepRuleMapper.selectByRuleName(ruleName);
    }

    @Override
    public CEPRule selectByRuleNameTest(String ruleName) {
        return cepRuleMapper.selectByRuleNameTest(ruleName);
    }

    @Override
    public CEPRule selectByPrimaryKey(Integer id) {
        return cepRuleMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<CEPRule> getCEPListByName(String ruleName) {
        // 需要修改mapper文件
        return null;
    }

    @Override
    public int updateByPrimaryKey(CEPRule record) {
        cepRuleMapper.updateByPrimaryKey(record);
        return 0;
    }

    @Override
    public List<CEPRule> getCEPList(Integer start, Integer pages) {
        return null;
    }

    @Override
    public List<CEPRule> getCEPList(CEPRuleExample example) {
        return cepRuleMapper.selectByExample(example);
    }

    @Override
    public List<CEPRule> getCEPRuleList(String ruleName) {
        List<CEPRule> CEPRuleList = cepRuleMapper.getCEPRuleList(ruleName);
        return CEPRuleList;
    }

    @Override
    public int deleteCEPById(Integer id) {
        return 0;
    }

    @Autowired
    CEPRuleMapper cepRuleMapper;

    @Override
    public int insert(CEPRule record) {
        return cepRuleMapper.insert(record);
    }

    @Override
    public Integer updateCEPById(CEPRule rule) {
        //解析参数
        return cepRuleMapper.updateByPrimaryKey(rule);
    }


}
