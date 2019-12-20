package com.webank.weevent.processor.model;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

@Data
public class StatisticWeEvent implements Serializable {
    private Integer systemAmount;
    private Integer userAmount;
    private Integer runAmount;
    private Map<String, StatisticRule> statisticRuleMap;
}
