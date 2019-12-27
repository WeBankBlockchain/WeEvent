package com.webank.weevent.governance.repository;


import java.util.List;

import com.webank.weevent.governance.entity.RuleDatabaseEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleDatabaseRepository extends JpaRepository<RuleDatabaseEntity, Long> {

    RuleDatabaseEntity findById(Integer id);

    List<RuleDatabaseEntity> findAllByBrokerIdAndSystemTag(Integer brokerId, Boolean tag);

}
