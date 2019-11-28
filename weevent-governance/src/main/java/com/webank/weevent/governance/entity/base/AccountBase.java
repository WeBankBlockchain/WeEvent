package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AccountBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class AccountBase extends BaseEntity {


    @NotBlank
    @Column(name = "username")
    private String username;

    @NotBlank
    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "old_password")
    private String oldPassword;

    @Column(name = "delete_at")
    private String deleteAt = "0";

}
