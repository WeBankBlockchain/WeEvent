package com.webank.weevent.governance.entity;

import java.util.List;

import com.webank.weevent.governance.entity.base.AccountBase;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AccountEntity class
 *
 * @since 2019/04/28
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class AccountEntity extends AccountBase {

    private String oldPassword;

    private List<Integer> permissionIdList;


}
