package com.webank.weevent.governance.entity.base;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

/**
 * RuleDatabaseBase class
 *
 * @since 2019/10/15
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class RuleDatabaseBase extends BaseEntity {

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "broker_id")
    private Integer brokerId;

    @Column(name = "database_url")
    private String databaseUrl;

    @Column(name = "database_ip")
    private String databaseIp;

    @Column(name = "database_port")
    private String databasePort;

    @Column(name = "database_name")
    private String databaseName;

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

    // 1 means the system
    @Column(name = "system_tag")
    private Boolean systemTag;

    @NotBlank
    @Column(name = "database_type")
    private String databaseType;

    public RuleDatabaseBase() {
    }


    public RuleDatabaseBase(Integer userId, Integer brokerId,
                            @NotBlank String databaseUrl, @NotBlank String username,
                            @NotBlank String password, @NotBlank String datasourceName,
                            @Length(max = 256) String optionalParameter,
                            Boolean systemTag, @NotBlank String databaseType) {
        this.userId = userId;
        this.brokerId = brokerId;
        this.databaseUrl = databaseUrl;
        this.username = username;
        this.password = password;
        this.datasourceName = datasourceName;
        this.optionalParameter = optionalParameter;
        this.systemTag = systemTag;
        this.databaseType = databaseType;
    }
}
