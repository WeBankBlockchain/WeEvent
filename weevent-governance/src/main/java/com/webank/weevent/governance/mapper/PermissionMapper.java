package com.webank.weevent.governance.mapper;

import java.util.List;

import com.webank.weevent.governance.entity.Permission;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 *
 * @author puremilkfan
 * @since 2019-08-27
 */
@Mapper
public interface PermissionMapper{

    List<Permission> permissionList(@Param("permission") Permission permission);

    void batchDelete(@Param("permission")Permission permission);

    void batchInsert(@Param("permissionList")List<Permission> permissionList);


    void deletePermission(@Param("brokerId") Integer brokerId);

    void updatePermission(@Param("permission") Permission permission);



}
