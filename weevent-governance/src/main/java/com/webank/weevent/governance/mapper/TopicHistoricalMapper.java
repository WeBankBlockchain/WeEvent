package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.TopicTopicHistoricalEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicHistoricalMapper {

    List<TopicTopicHistoricalEntity> historicalDataList(TopicTopicHistoricalEntity topicHistoricalEntity);

    List<TopicTopicHistoricalEntity> eventList(TopicTopicHistoricalEntity topicHistoricalEntity);

}
