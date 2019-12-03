package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.BrokerEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BrokerRepository extends JpaRepository<BrokerEntity, Long> {

    BrokerEntity findByIdAndDeleteAt(Integer id, String deleteAt);

    @Transactional
    @Modifying
    @Query(value = "update t_broker set delete_at=:deleteAt where id =:id", nativeQuery = true)
    void deleteById(@Param("id") Integer id, @Param("deleteAt") String deleteAt);

    @Query(value = "select distinct id as id,create_date as createDate,last_update as lastUpdate, user_id as userId, name, broker_url as brokerUrl, webase_url as webaseUrl" +
            " from t_broker where delete_at=0 and (user_id=:userId" +
            " or id in(select distinct ps.broker_id from t_permission ps where ps.user_id =:userId))", nativeQuery = true)
    List<BrokerEntity> findAllByUserId(@Param("userId") Integer userId);


}
