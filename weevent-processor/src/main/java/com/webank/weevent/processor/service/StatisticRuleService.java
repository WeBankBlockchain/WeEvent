package com.webank.weevent.processor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.StatisticRule;
import com.webank.weevent.processor.model.StatisticWeEvent;
import com.webank.weevent.processor.mq.CEPRuleMQ;
import com.webank.weevent.processor.quartz.QuartzManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class StatisticRuleService {
    @Autowired
    private QuartzManager quartzManager;

    @Autowired
    private CEPRuleMQ cEPRuleMQ;

    public StatisticWeEvent getStatisticWeEvent(List<String> idList) {
        StatisticWeEvent statisticWeEvent = cEPRuleMQ.getStatisticWeEvent();
        Map<String, StatisticRule> statisticRuleMap = new HashMap<>();

        // TODO:
        for (Map.Entry<String, StatisticRule> entry : statisticWeEvent.getStatisticRuleMap().entrySet()) {
            StatisticRule rule = entry.getValue();
            // check the brokerId
            for (String id : idList) {
                if (id.equals(rule.getId())) {
                    statisticRuleMap.put(rule.getId(), rule);
                }
            }

        }
        statisticWeEvent.setStatisticRuleMap(statisticRuleMap);
        StatisticWeEvent statisticJobs = quartzManager.getStatisticJobs(statisticWeEvent, idList);
        return statisticJobs;
    }
}
