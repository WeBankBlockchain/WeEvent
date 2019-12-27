package com.webank.weevent.governance.repository;

import com.webank.weevent.governance.entity.TopicHistoricalEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TopicHistoricalRepository extends JpaRepository<TopicHistoricalEntity, Long> {


    @Transactional
    @Modifying
    @Query(value = "delete from t_topic_historical where brokerId =:brokerId", nativeQuery = true)
    void deleteTopicHistoricalByBrokerId(@Param("brokerId") Integer brokerId);

}
