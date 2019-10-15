package com.webank.weevent.governance.entity.base;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User class
 *
 * @since 2019/04/28
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class AccountBase extends BaseEntity {


    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @Email
    private String email;

    private String oldPassword;

    private Integer isDelete;

    private Integer brokerId;


}
