package com.webank.weevent.processor.mq;

import java.util.Map;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.StatisticWeEvent;
import com.webank.weevent.processor.utils.JsonUtil;
import com.webank.weevent.processor.utils.StatisticCEPRuleUtil;
import com.webank.weevent.client.IWeEventClient;
import com.webank.weevent.client.IWeEventClient.EventListener;
import com.webank.weevent.client.WeEvent;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExtendEventLister implements EventListener {
    private IWeEventClient client;
    private Map<String, CEPRule> ruleMap;
    private StatisticWeEvent statisticWeEvent = new StatisticWeEvent();

    ExtendEventLister(IWeEventClient client, Map<String, CEPRule> ruleMap, StatisticWeEvent statisticWeEvent) {
        this.client = client;
        this.ruleMap = ruleMap;
        this.statisticWeEvent = statisticWeEvent;
    }

    @Override
    public void onEvent(WeEvent event) {
        try {
            String content = new String(event.getContent());
            log.info("on event:{},content:{}", event.toString(), content);
            Pair<String, String> type;
            // check the content
            if (JsonUtil.isValid(content)) {
                type = CEPRuleMQ.handleOnEvent(client, event, ruleMap);
            } else {
                type = CEPRuleMQ.handleOnEventOtherPattern(client, event, ruleMap);
            }
            CEPRuleMQ.statisticWeEvent = StatisticCEPRuleUtil.statisticOrderType(statisticWeEvent, type);

        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    @Override
    public void onException(Throwable e) {
        log.info("on event:{}", e.toString());
    }

}
