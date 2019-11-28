package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.BrokerEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrokerRepository extends JpaRepository<BrokerEntity,Long> {

    BrokerEntity findById(Integer id);

    List<BrokerEntity> findAllByUserId(Integer userId);



}
