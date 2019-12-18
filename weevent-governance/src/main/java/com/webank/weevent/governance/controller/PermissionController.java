package com.webank.weevent.governance.controller;

import java.util.List;

import com.webank.weevent.governance.entity.AccountEntity;
import com.webank.weevent.governance.entity.PermissionEntity;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.PermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping(value = "/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     *
     */
    @PostMapping("/permissionList")
    public GovernanceResult permissionList(@RequestBody AccountEntity accountEntity) {
        List<PermissionEntity> accountEntities = permissionService.permissionList(accountEntity);
        return new GovernanceResult(accountEntities);
    }
}
