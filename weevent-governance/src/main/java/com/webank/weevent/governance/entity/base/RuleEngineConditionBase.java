package com.webank.weevent.governance.entity.base;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class RuleEngineConditionBase extends BaseEntity {

    private Integer ruleId;

    private String connectionOperator;

    private String conditionalOperator;

    private String columnName;

    private String sqlCondition;
}
