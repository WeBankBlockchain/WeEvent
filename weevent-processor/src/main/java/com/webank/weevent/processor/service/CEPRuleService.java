package com.webank.weevent.processor.service;

import java.util.List;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.CEPRuleExample;

public interface CEPRuleService {
    CEPRule selectByPrimaryKey(Integer id);
    CEPRule selectByRuleName(String ruleName);
    CEPRule selectByRuleNameTest(String ruleName);
    int insert(CEPRule record);
    Integer updateCEPById(CEPRule rule);
    List<CEPRule> getCEPListByName(String ruleName);
    List<CEPRule> getCEPList(Integer start,Integer pages);
    List<CEPRule> getCEPList(CEPRuleExample example);
    List<CEPRule> getCEPRuleList(String ruleName);

    int updateByPrimaryKey(CEPRule record);
    int deleteCEPById(Integer id);

}
