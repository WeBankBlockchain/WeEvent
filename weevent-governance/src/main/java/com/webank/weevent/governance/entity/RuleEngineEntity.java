package com.webank.weevent.governance.entity;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RuleEngineEntity class
 *
 * @since 2019/09/23
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleEngineEntity extends BaseEntity {

    private String ruleName;
    /**
     * Type, the current default is json
     */
    private Integer payloadType;

    private String payload;

    private Map<String, Object> payloadMap = new HashMap<>();

    private Integer userId;

    private Integer brokerId;

    private Integer cepId;

    private String brokerUrl;

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

    private String errorDestination;

    private String errorMessage;

    private Integer pageSize;

    private Integer pageNumber;

    private Integer startIndex;

    private Integer endIndex;
}
