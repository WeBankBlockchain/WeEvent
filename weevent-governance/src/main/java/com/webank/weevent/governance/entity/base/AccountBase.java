package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * AccountBase class
 *
 * @since 2019/10/15
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class AccountBase extends BaseEntity {


    @NotBlank
    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    //0 means not deleted ,others means deleted
    @Column(name = "delete_at", nullable = false, columnDefinition = "BIGINT(16)")
    private Long deleteAt = 0L;

    public AccountBase(@NotBlank String username) {
        this.username = username;
    }

    public AccountBase() {
    }
}
