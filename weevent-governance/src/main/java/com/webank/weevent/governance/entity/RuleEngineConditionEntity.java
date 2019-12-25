package com.webank.weevent.governance.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Entity
@Table(name = "t_rule_engine_condition")
public class RuleEngineConditionEntity extends RuleEngineConditionBase {

    @Transient
    private String connectionOperator;

    @Transient
    private String conditionalOperator;

    @Transient
    private String columnName;

    @Transient
    private String columnMark;

    @Transient
    private String sqlCondition;

    @Transient
    private String functionType;

    @Transient
    private List<RuleEngineConditionEntity> children;

}
