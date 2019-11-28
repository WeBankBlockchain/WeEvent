package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.PermissionEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 *
 * @author puremilkfan
 * @since 2019-08-27
 */
@Mapper
public interface PermissionMapper{

    List<PermissionEntity> permissionList(@Param("permissionEntity") PermissionEntity permissionEntity);


    void deletePermission(@Param("brokerId") Integer brokerId);

    List<Integer> findUserIdByBrokerId(Integer brokerId);


}
