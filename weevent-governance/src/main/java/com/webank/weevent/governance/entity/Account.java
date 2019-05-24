package com.webank.weevent.governance.entity;

import java.util.Date;

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

	private Long id;

    private String username;

    private String password;

    private String email;

    private Date lastUpdate;
}
