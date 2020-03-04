package com.webank.weevent.processor.model;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StatisticWeEvent {
    private Integer systemAmount;
    private Integer userAmount;
    private Integer runAmount;
    private Map<String, StatisticRule> statisticRuleMap;
}
