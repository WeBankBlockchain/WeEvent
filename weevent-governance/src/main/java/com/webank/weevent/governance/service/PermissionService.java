package com.webank.weevent.governance.service;

import java.util.List;

import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.entity.PermissionEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.PermissionMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author puremilkfan
 * @since 2019-08-27
 */
@Service
public class PermissionService{

    @Autowired
    private PermissionMapper permissionMapper;

    public List<PermissionEntity>  permissionList(AccountEntity accountEntity) throws GovernanceException {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setBrokerId(accountEntity.getBrokerId());
        // execute select
        List<PermissionEntity> permissionEntities = permissionMapper.permissionList(permissionEntity);
        return permissionEntities;
    }




}
