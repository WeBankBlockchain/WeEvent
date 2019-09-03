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

    void batchDelete(@Param("permissionEntity") PermissionEntity permissionEntity);

    void batchInsert(@Param("permissionEntityList")List<PermissionEntity> permissionEntityList);


    void deletePermission(@Param("brokerId") Integer brokerId);

    void updatePermission(@Param("permissionEntity") PermissionEntity permissionEntity);



}
