package com.webank.weevent.governance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.webank.weevent.governance.entity.base.PermissionBase;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *  PermissionEntity class
 * @since 2019/08/28
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_permission")
public class PermissionEntity extends PermissionBase {

}
