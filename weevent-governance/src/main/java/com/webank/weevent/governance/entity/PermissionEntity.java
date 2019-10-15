package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.BaseEntity;
import com.webank.weevent.governance.entity.base.PermissionBase;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *  PermissionEntity class
 * @since 2019-08-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PermissionEntity extends PermissionBase {

    private Integer userId;

    private Integer brokerId;

}
