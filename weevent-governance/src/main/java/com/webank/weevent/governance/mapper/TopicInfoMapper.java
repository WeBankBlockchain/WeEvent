package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.TopicEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TopicInfoMapper {

    List<TopicEntity> getCreator(@Param("brokerId") Integer brokerId,@Param("groupId") String groupId, @Param("topicNameList") List<String> topicNameList);

    //delete brokerService first delete topicInfo 
    Boolean deleteTopicInfo(@Param("id") Integer Id,@Param("deleteAt") String deleteAt);

    Boolean deleteByBrokerId(@Param("brokerId") Integer brokerId,@Param("deleteAt") String deleteAt);
}
