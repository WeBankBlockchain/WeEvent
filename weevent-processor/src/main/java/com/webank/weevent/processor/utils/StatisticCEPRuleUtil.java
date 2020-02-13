package com.webank.weevent.processor.utils;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.StatisticRule;
import com.webank.weevent.processor.model.StatisticWeEvent;
import com.webank.weevent.processor.utils.ConstantsHelper;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatisticCEPRuleUtil {

    /**
     * statistic message
     *
     * @param ruleMap
     * @return
     */
    public static StatisticWeEvent statistic(StatisticWeEvent statisticWeEvent, Map<String, CEPRule> ruleMap) {
        Map<String, StatisticRule> statisticRuleMap = new HashMap<>();

        // get all rule details
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            CEPRule rule = entry.getValue();
            StatisticRule statisticRule = new StatisticRule();
            statisticRule.setId(rule.getId());
            statisticRule.setBrokerId(rule.getBrokerId());
            statisticRule.setRuleName(rule.getRuleName());
            statisticRule.setStatus(rule.getStatus());
            statisticRule.setStartTime(rule.getCreatedTime());
            statisticRule.setDestinationType(rule.getConditionType());
            statisticRuleMap.put(rule.getId(), statisticRule);
        }

        statisticWeEvent.setStatisticRuleMap(statisticRuleMap);
        return statisticWeEvent;
    }

    public static StatisticWeEvent statisticOrderType(StatisticWeEvent statisticWeEvent, Pair<String, String> type) {
        if (!("".equals(type.getValue()))) {
            StatisticRule statisticRule = statisticWeEvent.getStatisticRuleMap().get(type.getValue());
            switch (type.getKey()) {
                case ConstantsHelper.HIT_TIMES:
                    statisticRule.setHitTimes(statisticRule.getHitTimes() + 1);
                    break;

                case ConstantsHelper.NOT_HIT_TIMES:
                    statisticRule.setNotHitTimes(statisticRule.getNotHitTimes() + 1);
                    break;

                case ConstantsHelper.WRITE_DB_SUCCESS:
                case ConstantsHelper.PUBLISH_EVENT_SUCCESS:
                    statisticRule.setDataFlowSuccess(statisticRule.getDataFlowSuccess() + 1);
                    break;

                case ConstantsHelper.WRITE_DB_FAIL:
                case ConstantsHelper.PUBLISH_EVENT_FAIL:
                    statisticRule.setDataFlowFail(statisticRule.getDataFlowFail() + 1);
                    break;

                case ConstantsHelper.LAST_FAIL_REASON:
                    statisticRule.setLastFailReason(statisticRule.getLastFailReason());
                    break;

                default:
                    log.info("other type:{}", type);
                    break;
            }
        }
        return statisticWeEvent;
    }
}
