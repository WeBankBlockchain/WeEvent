package com.webank.weevent.governance.repository;

import com.webank.weevent.governance.entity.RuleEngineEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleEngineRepository extends JpaRepository<RuleEngineEntity,Long> {

    RuleEngineEntity findById(Integer id);

}
