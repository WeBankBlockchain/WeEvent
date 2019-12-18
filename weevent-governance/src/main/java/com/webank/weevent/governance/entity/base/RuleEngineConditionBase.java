package com.webank.weevent.governance.entity.base;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RuleEngineConditionBase class
 *
 * @since 2019/10/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
public class RuleEngineConditionBase extends BaseEntity {


    @Column(name = "rule_id")
    private Integer ruleId;


    @Column(name = "sql_condition_json")
    private String sqlConditionJson;


}
