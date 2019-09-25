package com.webank.weevent.processor.cache;
import	java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.mq.CEPRuleMQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class CEPRuleCache {
    private static Map<String, CEPRule> ruleMap = new ConcurrentHashMap<>();
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private CEPRuleMapper cEPRuleMapper;

    @PostConstruct
    public void init() {
        // get all rule
        List<CEPRule> dynamicRuleList = cEPRuleMapper.getDynamicCEPRuleList();
        if (!CollectionUtils.isEmpty(dynamicRuleList)) {
            ruleMap = dynamicRuleList.stream()
                    .collect(Collectors.toMap(CEPRule::getId, dynamicRule -> dynamicRule));
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Iterator<Map.Entry<String, CEPRule>> iter = ruleMap.entrySet().iterator();
                    while(iter.hasNext()) {
                        Map.Entry<String, CEPRule> entry = iter.next();
                        CEPRuleMQ.subscribeMsg(entry.getValue(), ruleMap);
                    }
                }
            });
        }
    }

    public static void addCEPRule(CEPRule rule) {
        ruleMap.put(rule.getId(), rule);
        CEPRuleMQ.subscribeMsg(rule, ruleMap); // 订阅消息
    }

    public static void deleteCEPRuleById(String ruleId) {
        CEPRuleMQ.unSubscribeMsg(ruleMap.get(ruleId), CEPRuleMQ.subscriptionIdMap.get(ruleId)); // 取消订阅
        ruleMap.remove(ruleId);
    }

    public static CEPRule getCEPRule(String ruleId){
        return ruleMap.get(ruleId);
    }

    public static Map<String, CEPRule> getCEPRuleMap() {
        return ruleMap;
    }
}
