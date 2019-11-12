package com.webank.weevent.governance.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RuleEngineBase class
 *
 * @since 2019/09/23
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleEngineBase extends BaseEntity {

    private String ruleName;
    /**
     * Type, the current default is json
     */
    private Integer payloadType;

    private String payload;

    private Integer userId;

    private String groupId;

    private Integer brokerId;

    private String cepId;

    private String fromDestination;

    private String toDestination;

    private String selectField;

    private String conditionField;

    /**
     * Trigger condition type, 1 identifies topic,
     * 2 identifies flow to relational database
     */
    private Integer conditionType;

    /**
     * 0 means not started, 1 means running,2 means is deleted
     */
    private Integer status;

    private String databaseUrl;

    private Integer ruleDataBaseId;

    private String errorDestination;

    private String errorMessage;

    // 2 means the system
    private String systemTag;
}
