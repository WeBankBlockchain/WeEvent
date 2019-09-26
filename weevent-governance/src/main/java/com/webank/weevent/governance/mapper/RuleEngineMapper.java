package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.RuleEngineEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RuleEngineMapper {

    // get RuleEngineEntityList
    List<RuleEngineEntity> getRuleEngines(RuleEngineEntity ruleEngineEntity);

    // add RuleEngineEntity
    Boolean addRuleEngine(RuleEngineEntity ruleEngineEntity);

    // delete RuleEngineEntity
    Boolean deleteRuleEngine(RuleEngineEntity ruleEngineEntity);

    // update RuleEngineEntity
    Boolean updateRuleEngine(RuleEngineEntity ruleEngineEntity);

    Boolean updateRuleEngineStatus(RuleEngineEntity ruleEngineEntity);

    int countRuleEngine(RuleEngineEntity ruleEngineEntity);

}
