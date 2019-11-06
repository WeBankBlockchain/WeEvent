package com.webank.weevent.governance.entity.base;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AccountBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class AccountBase extends BaseEntity {


    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String email;

    private String oldPassword;

    private Integer isDelete;

    private Integer brokerId;


}
