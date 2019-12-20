package com.webank.weevent.governance.service;

import java.util.List;
import java.util.stream.Collectors;

import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.entity.PermissionEntity;
import com.webank.weevent.governance.repository.PermissionRepository;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

/**
 * @author puremilkfan
 * @since 2019-08-27
 */
@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public List<PermissionEntity> permissionList(AccountEntity accountEntity) {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setBrokerId(accountEntity.getBrokerId());
        // execute select
        Example<PermissionEntity> entityExample = Example.of(permissionEntity);
        return permissionRepository.findAll(entityExample);
    }

    public Boolean verifyPermissions(Integer brokerId, String userId) {
        List<Integer> userIds = permissionRepository.findUserIdByBrokerId(brokerId);
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
