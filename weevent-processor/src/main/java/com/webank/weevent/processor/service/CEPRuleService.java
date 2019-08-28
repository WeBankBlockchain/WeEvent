package com.webank.weevent.processor.service;
import java.util.List;
import com.webank.weevent.processor.model.CEPRule;

public interface CEPRuleService {
    CEPRule selectByPrimaryKey(Integer id);

    CEPRule selectByRuleName(String ruleName);

    int insert(CEPRule record);

    List<CEPRule> getCEPRuleList(String ruleName);

    int updateByPrimaryKey(CEPRule record);
}
