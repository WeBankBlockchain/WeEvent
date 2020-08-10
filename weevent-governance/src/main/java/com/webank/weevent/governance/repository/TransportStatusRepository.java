package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.FileTransportStatusEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TransportStatusRepository extends JpaRepository<FileTransportStatusEntity, Long> {

	List<FileTransportStatusEntity> queryByBrokerIdAndGroupIdAndTopicName(Integer brokerId, String groupId,
			String topicName);

	FileTransportStatusEntity queryByBrokerIdAndGroupIdAndTopicNameAndFileName(Integer brokerId, String groupId, String topicName, String fileName);

	@Transactional
	@Modifying
	@Query(value = "update t_file_transport_status set transport_status=:status where id =:id", nativeQuery = true)
	void updateTransportStatus(@Param("transport_status") String status, @Param("id") Long id);

	@Transactional
	@Modifying
	@Query(value = "update t_file_transport_status set speed=:speed where id =:id", nativeQuery = true)
	void updateTransportSpeed(@Param("speed") String speed, @Param("id") Long id);
}
