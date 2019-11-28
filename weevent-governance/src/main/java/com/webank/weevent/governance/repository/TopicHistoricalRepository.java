package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.TopicHistoricalEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicHistoricalRepository extends JpaRepository<TopicHistoricalEntity,Long> {


    List<TopicHistoricalEntity> historicalDataList(TopicHistoricalEntity topicHistoricalEntity);

    List<TopicHistoricalEntity> eventList(TopicHistoricalEntity topicHistoricalEntity);
}
