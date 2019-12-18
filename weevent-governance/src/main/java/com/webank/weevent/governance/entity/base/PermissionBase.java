package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * PermissionBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class PermissionBase extends BaseEntity {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "broker_id")
    private Integer brokerId;

}
