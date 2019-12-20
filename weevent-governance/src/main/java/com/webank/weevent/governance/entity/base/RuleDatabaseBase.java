package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

/**
 * RuleDatabaseBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class RuleDatabaseBase extends BaseEntity {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "broker_id")
    private Integer brokerId;

    @NotBlank
    @Column(name = "database_url")
    private String databaseUrl;

    @NotBlank
    @Column(name = "username")
    private String username;

    @NotBlank
    @Column(name = "password")
    private String password;

    @NotBlank
    @Column(name = "datasource_name")
    private String datasourceName;

    @Length(max = 256)
    @Column(name = "optional_parameter")
    private String optionalParameter;

    @NotBlank
    @Column(name = "table_name")
    private String tableName;

    // 1 means the system
    @Column(name = "system_tag")
    private Boolean systemTag;

    public RuleDatabaseBase() {
    }


    public RuleDatabaseBase(Integer userId, Integer brokerId,
                            @NotBlank String databaseUrl,
                            @NotBlank String username,
                            @NotBlank String password,
                            @NotBlank String datasourceName,
                            @Length(max = 256) String optionalParameter,
                            @NotBlank String tableName, Boolean systemTag) {
        this.userId = userId;
        this.brokerId = brokerId;
        this.databaseUrl = databaseUrl;
        this.username = username;
        this.password = password;
        this.datasourceName = datasourceName;
        this.optionalParameter = optionalParameter;
        this.tableName = tableName;
        this.systemTag = systemTag;
    }
}
