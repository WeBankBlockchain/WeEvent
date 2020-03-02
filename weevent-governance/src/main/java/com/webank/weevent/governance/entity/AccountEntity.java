package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.AccountBase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * AccountEntity class
 *
 * @since 2019/04/28
 */
@Setter
@Getter
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
