package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.TopicHistoricalEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicHistoricalMapper {

    List<TopicHistoricalEntity> historicalDataList(TopicHistoricalEntity topicHistoricalEntity);

    List<TopicHistoricalEntity> eventList(TopicHistoricalEntity topicHistoricalEntity);

}
