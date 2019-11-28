package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.RuleEngineEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleEngineRepository extends JpaRepository<RuleEngineEntity,Long> {

    RuleEngineEntity findById(Integer id);

}
