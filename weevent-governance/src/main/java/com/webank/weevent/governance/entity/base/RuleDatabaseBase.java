package com.webank.weevent.governance.entity.base;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

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
    @NotBlank
    private String ip;
    @NotBlank
    private String port;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String databaseName;

    @Length(max = 256)
    private String optionalParameter;
    @NotBlank
    private String datasourceName;
    @NotBlank
    private String tableName;

    // 1 means the system
    private String systemTag;

    public RuleDatabaseBase() {
    }


    public RuleDatabaseBase(Integer userId, Integer brokerId, String ip, String port, String username, String password, String databaseName, String optionalParameter, String datasourceName, String tableName, String systemTag) {
        this.userId = userId;
        this.brokerId = brokerId;
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.optionalParameter = optionalParameter;
        this.datasourceName = datasourceName;
        this.tableName = tableName;
        this.systemTag = systemTag;
    }


}
