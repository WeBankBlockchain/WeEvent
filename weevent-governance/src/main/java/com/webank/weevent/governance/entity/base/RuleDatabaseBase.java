package com.webank.weevent.governance.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * CirculationDatabaseBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleDatabaseBase extends BaseEntity {

    private Integer userId;

    private Integer brokerId;

    private String ip;

    private String port;

    private String userName;

    private String password;

    private String databaseName;

    private String optionalParameter;

    private String dataSourceName;

    private String tableName;

    // 1 means the system
    private String systemTag;


}
