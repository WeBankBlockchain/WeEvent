package com.webank.weevent.governance.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import com.webank.weevent.governance.entity.base.AccountBase;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * AccountEntity class
 *
 * @since 2019/04/28
 */
@Getter
@Setter
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

    public AccountEntity(@NotBlank String username) {
        super(username);
    }

    public AccountEntity() {
        super();
    }
}
