package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.FileTransportEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportRepository extends JpaRepository<FileTransportEntity, Long> {

    List<FileTransportEntity> queryByBrokerIdAndGroupId(Integer brokerId, String groupId);
}
