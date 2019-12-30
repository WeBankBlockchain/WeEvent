package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RuleEngineBase class
 *
 * @since 2019/09/23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class RuleEngineBase extends BaseEntity {

    @Column(name = "rule_name", columnDefinition = "varchar(128)")
    private String ruleName;

    /**
     * Type, the current default is json
     */
    @Column(name = "payload_type")
    private Integer payloadType;

    @Column(name = "payload")
    private String payload;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "broker_id")
    private Integer brokerId;

    @Column(name = "cep_id")
    private String cepId;

    @Column(name = "from_destination")
    private String fromDestination;

    @Column(name = "to_destination")
    private String toDestination;

    @Column(name = "select_field")
    private String selectField;

    @Column(name = "condition_field")
    private String conditionField;

    @Column(name = "condition_field_json")
    private String conditionFieldJson;

    /**
     * Trigger condition type, 1 identifies topic,
     * 2 identifies flow to relational database
     */
    @Column(name = "condition_type")
    private Integer conditionType;

    /**
     * 0 means not started, 1 means running,2 means is deleted
     */
    @Column(name = "status")
    private Integer status;

    @Column(name = "rule_database_id")
    private Integer ruleDataBaseId;

    @Column(name = "error_destination")
    private String errorDestination;

    @Column(name="function_array")
    private String functionArray;

    // 1 means the system
    @Column(name = "system_tag")
    private Boolean systemTag;

    //0 means not deleted ,others means deleted
    @Column(name = "delete_at",nullable = false, columnDefinition = "BIGINT(16)")
    private Long deleteAt = 0L;
}
