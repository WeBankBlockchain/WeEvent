package com.webank.weevent.governance.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.webank.weevent.governance.entity.base.RuleEngineBase;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RuleEngineEntity class
 *
 * @since 2019/09/23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_rule_engine",
        uniqueConstraints = {@UniqueConstraint(name = "ruleNameDeleteAt",
                columnNames = {"rule_name", "delete_at"})})
public class RuleEngineEntity extends RuleEngineBase {

    @Transient
    private String databaseUrl;

    @Transient
    private Map<String, Object> payloadMap = new HashMap<>();

    @Transient
    private String brokerUrl;

    @Transient
    private String fullSQL;

    @Transient
    private Integer pageSize;

    @Transient
    private Integer pageNumber;

    @Transient
    private Integer startIndex;

    @Transient
    private Integer endIndex;

    @Transient
    private Integer totalCount;

    @Transient
    private String createDateStr;

    @Transient
    private String offSet;

    @Transient
    private String tableName;

}


