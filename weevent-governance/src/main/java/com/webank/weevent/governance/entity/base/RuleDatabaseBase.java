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
    private String databaseUrl;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String datasourceName;

    @Length(max = 256)
    private String optionalParameter;
    @NotBlank
    private String tableName;

    // 1 means the system
    private String systemTag;

    public RuleDatabaseBase() {
    }


    public RuleDatabaseBase(Integer userId, Integer brokerId,
                            String username, String password,
                            String datasourceName, String optionalParameter,
                            String databaseUrl, String tableName, String systemTag) {
        this.userId = userId;
        this.brokerId = brokerId;
        this.username = username;
        this.password = password;
        this.datasourceName = datasourceName;
        this.optionalParameter = optionalParameter;
        this.databaseUrl = databaseUrl;
        this.tableName = tableName;
        this.systemTag = systemTag;
    }


}
