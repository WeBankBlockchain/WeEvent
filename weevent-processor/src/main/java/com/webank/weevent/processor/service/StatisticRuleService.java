package com.webank.weevent.processor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // check the id list
        if (null == idList || 0 == idList.size()) {
            statisticWeEvent.setStatisticRuleMap(statisticRuleMap);
        } else {
            for (String id : idList) {
                // check the id
                if (!statisticWeEvent.getStatisticRuleMap().containsKey(id)) {
                    statisticWeEvent.getStatisticRuleMap().remove(id);
                }
            }
        }

        StatisticWeEvent statisticJobs = quartzManager.getStatisticJobs(statisticWeEvent, idList);
        return statisticJobs;
    }
}
