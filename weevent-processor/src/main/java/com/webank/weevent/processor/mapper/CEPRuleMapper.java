package com.webank.weevent.processor.mapper;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.CEPRuleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CEPRuleMapper {
    long countByExample(CEPRuleExample example);

    int deleteByExample(CEPRuleExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(CEPRule record);

    int insertSelective(CEPRule record);

    List<CEPRule> selectByExample(CEPRuleExample example);

    List<CEPRule> getCEPRuleList(String ruleName);
    CEPRule selectByPrimaryKey(Integer id);
    CEPRule selectByRuleName(String ruleName);

    CEPRule selectByRuleNameTest(String ruleName);

    int updateByExampleSelective(@Param("record") CEPRule record, @Param("example") CEPRuleExample example);

    int updateByExample(@Param("record") CEPRule record, @Param("example") CEPRuleExample example);

    int updateByPrimaryKeySelective(CEPRule record);

    int updateByPrimaryKey(CEPRule record);
}