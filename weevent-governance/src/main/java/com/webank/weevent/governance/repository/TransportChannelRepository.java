package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.FileTransportChannelEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportChannelRepository extends JpaRepository<FileTransportChannelEntity, Long> {

    List<FileTransportChannelEntity> queryByBrokerIdAndGroupId(Integer brokerId, String groupId);
}
