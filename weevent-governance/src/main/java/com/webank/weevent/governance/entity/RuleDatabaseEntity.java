package com.webank.weevent.governance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import com.webank.weevent.governance.entity.base.RuleDatabaseBase;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/**
 * RuleDatabaseEntity class
 *
 * @since 2019/09/28
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_rule_database")
public class RuleDatabaseEntity extends RuleDatabaseBase {


    @Transient
    private Integer checkType;

    @Transient
    private String tableName;

    public RuleDatabaseEntity(Integer userId, Integer brokerId,
                              @NotBlank String databaseUrl, @NotBlank String username,
                              @NotBlank String password, @NotBlank String datasourceName,
                              @Length(max = 256) String optionalParameter,
                              Boolean systemTag, Integer databaseType) {
        super(userId, brokerId, databaseUrl, username, password, datasourceName, optionalParameter, systemTag, databaseType);
    }

    public RuleDatabaseEntity() {
    }
}
