package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.RuleEngineConditionEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RuleEngineConditionMapper {


    List<RuleEngineConditionEntity> ruleEngineConditionList(RuleEngineConditionEntity ruleEngineConditionEntity);

    int addRuleEngineCondition(RuleEngineConditionEntity ruleEngineConditionEntity);

    int deleteRuleEngineCondition(RuleEngineConditionEntity ruleEngineConditionEntity);

    void batchInsert(@Param("ruleEngineConditionList") List<RuleEngineConditionEntity> ruleEngineConditionEntities);


}
