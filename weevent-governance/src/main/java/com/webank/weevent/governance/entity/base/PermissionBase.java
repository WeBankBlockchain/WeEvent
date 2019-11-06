package com.webank.weevent.governance.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *  PermissionBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class PermissionBase extends BaseEntity {

    private Integer userId;

    private Integer brokerId;

}
