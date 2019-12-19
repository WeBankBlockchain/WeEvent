package com.webank.weevent.processor.service;

import com.webank.weevent.processor.model.StatisticWeEvent;
import com.webank.weevent.processor.mq.CEPRuleMQ;

import org.springframework.stereotype.Service;


@Service
public class StatisticRuleService {
    public StatisticWeEvent getWeEventCollection() {
        return CEPRuleMQ.getStatisticWeEvent();
    }
}
