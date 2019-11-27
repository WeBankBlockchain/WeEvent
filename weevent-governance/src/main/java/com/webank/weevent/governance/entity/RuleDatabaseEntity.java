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

    private String databaseUrl;

    public RuleDatabaseEntity() {
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public RuleDatabaseEntity(Integer userId, Integer brokerId,
                              String ip, String port,String username,
                              String password,String databaseName,
                              String optionalParameter,String datasourceName,
                              String tableName, String systemTag) {
        super(userId, brokerId, ip, port, username, password, databaseName, optionalParameter, datasourceName, tableName, systemTag);
    }
}
