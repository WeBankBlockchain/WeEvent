package com.webank.weevent.governance.repository;

import com.webank.weevent.governance.entity.TopicHistoricalEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicHistoricalRepository extends JpaRepository<TopicHistoricalEntity,Long> {

}
