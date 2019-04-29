package com.webank.weevent.governance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TopicInfoMapper {

    //get creater by topicName
    String getCreaterByName(@Param(value = "topicName") String topicName);

    //save creater into database
    Boolean openTopic(@Param(value = "topicName") String topicName, @Param(value = "creater") String creater);
}
