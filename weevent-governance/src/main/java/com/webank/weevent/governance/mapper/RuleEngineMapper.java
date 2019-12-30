package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.RuleEngineEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RuleEngineMapper {

    // get RuleEngineEntityList
    List<RuleEngineEntity> getRuleEngines(RuleEngineEntity ruleEngineEntity);

    List<RuleEngineEntity> getRuleEnginePage(@Param("ruleEngineEntity") RuleEngineEntity ruleEngineEntity, @Param("startIndex") Integer startIndex, @Param("endIndex") Integer endIndex);


    int countRuleEngine(RuleEngineEntity ruleEngineEntity);

}
