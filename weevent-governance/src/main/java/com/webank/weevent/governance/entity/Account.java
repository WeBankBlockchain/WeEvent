package com.webank.weevent.governance.entity;

import java.util.Date;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.ToString;

/**
 * User class
 *
 * @since 2019/04/28
 */
@Data
@ToString
public class Account {

    private Integer id;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @Email
    private String email;

    private String oldPassword;

    private Date lastUpdate;
}
