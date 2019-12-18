package com.webank.weevent.governance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.webank.weevent.governance.entity.base.RuleDatabaseBase;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * RuleDatabaseEntity class
 *
 * @since 2019/09/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_rule_database")
public class RuleDatabaseEntity extends RuleDatabaseBase {



    public RuleDatabaseEntity(Integer userId, Integer brokerId,
                              @NotBlank String databaseUrl, @NotBlank String username,
                              @NotBlank String password, @NotBlank String datasourceName,
                              @Length(max = 256) String optionalParameter,
                              @NotBlank String tableName, Boolean systemTag) {
        super(userId, brokerId, databaseUrl, username, password, datasourceName, optionalParameter, tableName, systemTag);
    }

    public RuleDatabaseEntity() {
    }
}
