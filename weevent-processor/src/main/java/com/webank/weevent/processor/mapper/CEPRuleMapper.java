package com.webank.weevent.processor.mapper;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.CEPRuleExample;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface CEPRuleMapper {
    long countByExample(CEPRuleExample example);

    int deleteByExample(CEPRuleExample example);

    int deleteByPrimaryKey(String id);

    int insert(CEPRule record);

    int insertSelective(CEPRule record);

    List<CEPRule> selectByExample(CEPRuleExample example);

    List<CEPRule> getCEPRuleList(@Param("ruleName") String ruleName);

    List<CEPRule> getRulesByBrokerId(String brokerId);

    List<CEPRule> getRulesByUserId( String userId);

    List<CEPRule>  getRulesByUserIdAndBroker( String userId,String brokerId);

    List<CEPRule> getDynamicCEPRuleList();

    List<CEPRule> getDynamicCEPRuleAllParamList();

    List<CEPRule> getCEPRuleListByPage(Map<String, Integer> data);

    CEPRule selectByPrimaryKey(String id);

    List<CEPRule> selectByRuleName(@Param("ruleName") String ruleName);

    int updateByPrimaryKey(CEPRule record);

    int updateByExampleSelective(@Param("record") CEPRule record, @Param("example") CEPRuleExample example);

    int updateByExample(@Param("record") CEPRule record, @Param("example") CEPRuleExample example);

    int updateByPrimaryKeySelective(CEPRule record);

    int updateFieldById(CEPRule record);

}