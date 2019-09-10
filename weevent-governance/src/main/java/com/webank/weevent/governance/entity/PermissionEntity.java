package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;

/**
 * @author puremilkfan
 * @since 2019-08-28
 */
@Data
public class PermissionEntity extends BaseEntity {

    private Integer userId;

    private Integer brokerId;

}
