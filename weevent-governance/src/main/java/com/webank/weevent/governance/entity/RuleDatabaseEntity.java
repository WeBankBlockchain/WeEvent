package com.webank.weevent.governance.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.webank.weevent.governance.entity.base.RuleDatabaseBase;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * RuleDatabaseEntity class
 *
 * @since 2019/09/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@ToString
@Table(name = "t_rule_database")
public class RuleDatabaseEntity extends RuleDatabaseBase {


    public RuleDatabaseEntity() {
    }



    public RuleDatabaseEntity(Integer userId, Integer brokerId,
                              String username, String password,
                              String datasourceName, String optionalParameter,
                              String databaseUrl, String tableName,
                              String systemTag) {
        super(userId, brokerId, username, password, datasourceName, optionalParameter, databaseUrl, tableName, systemTag);
    }
}
