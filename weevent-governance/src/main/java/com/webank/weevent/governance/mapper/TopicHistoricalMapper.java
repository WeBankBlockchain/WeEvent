package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.TopicEventCountEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TopicHistoricalMapper {

    List<TopicEventCountEntity> historicalDataList(TopicEventCountEntity topicEventCountEntity);

    List<TopicEventCountEntity> eventList(TopicEventCountEntity topicEventCountEntity);

}
