package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.TopicEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TopicInfoMapper {

    // get creater by topicName
 //   String getCreaterByName(@Param(value = "topicName") String topicName);

    List<TopicEntity> getCreator(@Param("brokerId") Integer brokerId,@Param("topicNameList") List<String> topicNameList);

    // save creater into database
  //  Boolean openTopic(@Param(value = "topicName") String topicName, @Param(value = "creater") String creater);


    Boolean openBrokeTopic(@Param("topicEntity") TopicEntity topicEntity);

    //delete brokerService first delete topicInfo 
    Boolean deleteTopicInfo(@Param("id") Integer Id);

    Boolean deleteByBrokerId(@Param("brokerId") Integer brokerId);
}
