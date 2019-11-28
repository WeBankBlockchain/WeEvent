package com.webank.weevent.governance.repository;

import com.webank.weevent.governance.entity.RuleEngineConditionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleEngineConditionRepository extends JpaRepository<RuleEngineConditionEntity,Long> {
}
