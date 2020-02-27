package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class TimerSchedulerBase extends BaseEntity {

    @NotNull(message = "brokerId cannot be empty")
    @Column(name = "broker_id")
    private Integer brokerId;

    @Column(name = "user_id")
    private Integer userId;

    @NotNull(message = "schedulerName cannot be empty")
    @Column(name = "scheduler_name")
    private String schedulerName;

    @NotNull(message = "ruleDataBaseId cannot be empty")
    @Column(name = "rule_database_id")
    private Integer ruleDatabaseId;

    @NotNull(message = "periodParams cannot be empty")
    @Column(name = "period_params")
    private String periodParams;

    @NotNull(message = "parsingSql cannot be empty")
    @Column(name = "parsing_sql")
    private String parsingSql;

}
