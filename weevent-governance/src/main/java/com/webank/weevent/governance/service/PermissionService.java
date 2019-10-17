package com.webank.weevent.governance.service;

import java.util.List;
import java.util.stream.Collectors;

import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.entity.PermissionEntity;
import com.webank.weevent.governance.exception.GovernanceException;
import com.webank.weevent.governance.mapper.PermissionMapper;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author puremilkfan
 * @since 2019-08-27
 */
@Service
public class PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    public List<PermissionEntity> permissionList(AccountEntity accountEntity) throws GovernanceException {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setBrokerId(accountEntity.getBrokerId());
        // execute select
        List<PermissionEntity> permissionEntities = permissionMapper.permissionList(permissionEntity);
        return permissionEntities;
    }

    public Boolean verifyPermissions(Integer brokerId, String userId) {
        List<Integer> userIds = permissionMapper.findUserIdByBrokerId(brokerId);
        if (CollectionUtils.isEmpty(userIds)) {
            return false;
        }
        List<Integer> collect = userIds.stream().filter(it -> it.toString().equals(userId)).collect(Collectors.toList());
        if (!collect.isEmpty()) {
            return true;
        }
        return false;
    }


}
