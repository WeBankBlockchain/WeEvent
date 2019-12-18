package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.PermissionEntity;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

    @Transactional
    @Modifying
    @Query(value = "delete from t_permission where broker_id =:brokerId", nativeQuery = true)
    void deletePermissionByBrokerId(@Param("brokerId") Integer brokerId);

    @Query(value = "  select distinct user_id from t_broker where id =:brokerId and delete_at = 0 union select distinct user_id as userId " +
            "from t_permission  where broker_id  =:brokerId", nativeQuery = true)
    List<Integer> findUserIdByBrokerId(@Param("brokerId") Integer brokerId);


}
