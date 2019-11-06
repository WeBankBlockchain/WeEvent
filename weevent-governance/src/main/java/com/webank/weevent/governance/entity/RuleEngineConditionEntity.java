package com.webank.weevent.governance.entity;

import com.webank.weevent.governance.entity.base.RuleEngineConditionBase;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * RuleEngineConditionEntity class
 *
 * @since 2019/10/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RuleEngineConditionEntity extends RuleEngineConditionBase {

    private String connectionOperator;

    private String conditionalOperator;

    private String columnName;

    private String sqlCondition;

}
