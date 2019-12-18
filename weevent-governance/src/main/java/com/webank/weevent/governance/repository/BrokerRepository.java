package com.webank.weevent.governance.repository;

import com.webank.weevent.governance.entity.BrokerEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BrokerRepository extends JpaRepository<BrokerEntity, Long> {

    BrokerEntity findByIdAndDeleteAt(Integer id, Long deleteAt);


    @Transactional
    @Modifying
    @Query(value = "update t_broker set delete_at=:deleteAt where id =:id", nativeQuery = true)
    void deleteById(@Param("id") Integer id, @Param("deleteAt") Long deleteAt);

    @Transactional
    @Modifying
    @Query(value = "update t_broker set delete_at=:deleteAt where broker_url =:brokerUrl", nativeQuery = true)
    void deleteByBrokerUrl(@Param("brokerUrl") String brokerUrl, @Param("deleteAt") Long deleteAt);


}
