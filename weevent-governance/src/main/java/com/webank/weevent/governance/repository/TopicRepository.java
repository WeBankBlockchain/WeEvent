package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.TopicEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<TopicEntity, Long> {

  //  List<TopicEntity> getCreator(@Param("brokerId") Integer brokerId, @Param("groupId") String groupId, @Param("topicNameList") List<String> topicNameList);

}
