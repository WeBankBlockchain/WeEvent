package com.webank.weevent.processor.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.mq.CEPRuleMQ;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Slf4j
public class CEPRuleCache {
    private static Map<String, CEPRule> ruleMap = new ConcurrentHashMap<>();
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private CEPRuleMapper cEPRuleMapper;

    @PostConstruct
    public void init() {
        // get all rule
        List<CEPRule> dynamicRuleList = cEPRuleMapper.getDynamicCEPRuleList();
        log.info("dynamic rule list {}", dynamicRuleList.size());
        if (!CollectionUtils.isEmpty(dynamicRuleList)) {
            ruleMap = dynamicRuleList.stream()
                    .collect(Collectors.toMap(CEPRule::getId, dynamicRule -> dynamicRule));
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Iterator<Map.Entry<String, CEPRule>> iter = ruleMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, CEPRule> entry = iter.next();
                        log.info("start subscribe all topic...");
                        CEPRuleMQ.subscribeMsg(entry.getValue(), ruleMap);
                    }
                }
            });
        }
    }

    public static void addCEPRule(CEPRule rule) {
        ruleMap.put(rule.getId(), rule);
        // add subscription
        CEPRuleMQ.subscribeMsg(rule, ruleMap);
    }

    public static void deleteCEPRuleById(String ruleId) {
        // cancel the subscription
        CEPRuleMQ.unSubscribeMsg(ruleMap.get(ruleId), CEPRuleMQ.subscriptionIdMap.get(ruleId));
        ruleMap.remove(ruleId);
    }

    public static void updateCEPRule(CEPRule rule) throws BrokerException {
        CEPRuleMQ.updateSubscribeMsg(rule, ruleMap);
    }

    public static CEPRule getCEPRule(String ruleId) {
        return ruleMap.get(ruleId);
    }

    public static Map<String, CEPRule> getCEPRuleMap() {
        return ruleMap;
    }
}
