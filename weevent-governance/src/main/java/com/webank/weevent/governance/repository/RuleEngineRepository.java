package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.RuleEngineEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RuleEngineRepository extends JpaRepository<RuleEngineEntity, Long> {

    RuleEngineEntity findById(Integer id);


    List<RuleEngineEntity> findAllByBrokerIdAndSystemTagAndDeleteAt(Integer brokerId,Boolean flag, Long deleteAt);


    List<RuleEngineEntity> findAllByBrokerIdAndDeleteAt(Integer brokerId, Long deleteAt);


    @Transactional
    @Modifying
    @Query(value = "update t_rule_engine  set status=2 , delete_at=:deleteAt where id =:id", nativeQuery = true)
    void deleteRuleEngine(@Param("id") Integer id, @Param("deleteAt") Long deleteAt);


    @Query(value = "select distinct from_destination as fromDestination,to_destination as toDestination from t_rule_engine" +
            " where broker_id=:brokerId and group_id=:groupId and status != 2 and condition_type=1 and delete_at=0 and system_tag=0", nativeQuery = true)
    List<RuleEngineEntity> getRuleTopicList(@Param("brokerId") Integer brokerId, @Param("groupId") String groupId);

}
