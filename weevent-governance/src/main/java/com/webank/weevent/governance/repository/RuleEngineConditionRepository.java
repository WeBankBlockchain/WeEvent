package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.RuleEngineConditionEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RuleEngineConditionRepository extends JpaRepository<RuleEngineConditionEntity, Long> {

    List<RuleEngineConditionEntity> findAllByRuleId(Integer ruleId);

    @Transactional
    @Modifying
    @Query(value = "  delete from t_rule_engine_condition where rule_id =:ruleId", nativeQuery = true)
    void deleteRuleEngineCondition(@Param("ruleId") Integer ruleId);
}
