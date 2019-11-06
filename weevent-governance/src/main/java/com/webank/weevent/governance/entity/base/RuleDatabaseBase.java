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

    private String databaseUrl;

    private String databaseName;

    private String tableName;

    /**
     * 1 visible ,2 invisible
     */
    private String isVisible;


}
