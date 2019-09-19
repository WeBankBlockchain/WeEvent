package com.webank.weevent.governance.entity;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User class
 *
 * @since 2019/04/28
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class AccountEntity extends BaseEntity {


    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @Email
    private String email;

    private String oldPassword;

    private Integer isDelete;

    private List<Integer> permissionIdList;

    private Integer brokerId;


}
