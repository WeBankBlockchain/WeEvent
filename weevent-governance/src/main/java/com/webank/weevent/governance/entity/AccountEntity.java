package com.webank.weevent.governance.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.webank.weevent.governance.entity.base.AccountBase;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AccountEntity class
 *
 * @since 2019/04/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_account")
public class AccountEntity extends AccountBase {

    @Transient
    private Integer brokerId;

    @Transient
    private String oldPassword;

    @Transient
    private List<Integer> permissionIdList;


}
