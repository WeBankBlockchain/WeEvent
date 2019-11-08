package com.webank.weevent.governance.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class RuleEngineEntity extends RuleEngineBase {


    private Map<String, Object> payloadMap = new HashMap<>();

    private String brokerUrl;

    private String fullSQL;

    private Integer pageSize;

    private Integer pageNumber;

    private Integer startIndex;

    private Integer endIndex;

    private Integer totalCount;

    private String createDateStr;

    private String offSet;

    private List<RuleEngineConditionEntity> ruleEngineConditionList = new ArrayList<>();
}
