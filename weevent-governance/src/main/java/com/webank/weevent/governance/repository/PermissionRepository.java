package com.webank.weevent.governance.repository;

import java.util.List;

import com.webank.weevent.governance.entity.PermissionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {

  //  void deletePermissionByBrokerId(Integer brokerId);

    List<Integer> findUserIdByBrokerId(Integer brokerId);

}
