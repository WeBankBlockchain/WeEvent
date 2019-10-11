package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RuleEngineConditionEntity extends BaseEntity {

    private Integer ruleId;

    private String connectionOperator;

    private String conditionalOperator;

    private String columnName;

    private String sqlCondition;
}
