package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.RuleDatabaseBase;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CirculationDatabaseEntity class
 *
 * @since 2019/09/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleDatabaseEntity extends RuleDatabaseBase {


    public RuleDatabaseEntity() {
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public RuleDatabaseEntity(Integer userId, Integer brokerId,
                              String username, String password,
                              String datasourceName, String optionalParameter,
                              String databaseUrl, String tableName,
                              String systemTag) {
        super(userId, brokerId, username, password, datasourceName, optionalParameter, databaseUrl, tableName, systemTag);
    }
}
