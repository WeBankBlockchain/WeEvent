package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;
import lombok.ToString;

/**
 *
 * @author puremilkfan
 * @since 2019-08-28
 */
@Data
@ToString
public class PermissionEntity extends BaseEntity {

    private Integer userId;

    private Integer brokerId;

}
