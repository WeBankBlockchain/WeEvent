package com.webank.weevent.processor.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.mq.CEPRuleMQ;
import com.webank.weevent.processor.service.RedisService;
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
//    private static Map<String, CEPRule> ruleMap = new ConcurrentHashMap<>();
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    // block data cached in redis
    private static RedisService redisService;

    @Autowired
    private CEPRuleMapper cEPRuleMapper;

    @PostConstruct
    public void init() {
        // get all rule
        List<CEPRule> dynamicRuleList = cEPRuleMapper.getDynamicCEPRuleList();
        log.info("dynamic rule list {}", dynamicRuleList.size());
        if (!CollectionUtils.isEmpty(dynamicRuleList)) {

            for (CEPRule aDynamicRuleList : dynamicRuleList) {
                redisService.writeRulesToRedis(aDynamicRuleList.getId(), aDynamicRuleList);
            }

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Iterator<Map.Entry<String, CEPRule>> iter = redisService.readAllRulesFromRedis().entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, CEPRule> entry = iter.next();
                        log.info("start subscribe all topic...");
                        CEPRuleMQ.subscribeMsg(entry.getValue(), redisService.readAllRulesFromRedis());
                    }
                }
            });
        }
    }

    public static void addCEPRule(CEPRule rule) {
        //ruleMap.put(rule.getId(), rule);
        redisService.writeRulesToRedis(rule.getId(), rule);
        // add subscription
        CEPRuleMQ.subscribeMsg(rule, redisService.readAllRulesFromRedis());
    }

    public static void deleteCEPRuleById(String ruleId) {
        // cancel the subscription
        CEPRuleMQ.unSubscribeMsg(redisService.readRulesFromRedis(ruleId), CEPRuleMQ.subscriptionIdMap.get(ruleId));
        redisService.deleteRulesToRedis(ruleId);
    }

    public static void deleteCEPRuleById(CEPRule rule) {
        // cancel the subscription
        String ruleId = rule.getId();
        if (redisService.readRulesFromRedis(ruleId)!=null) {
            CEPRuleMQ.unSubscribeMsg(redisService.readRulesFromRedis(rule.getId()), CEPRuleMQ.subscriptionIdMap.get(ruleId));
        }
//        ruleMap.remove(ruleId);
        redisService.deleteRulesToRedis(ruleId);
    }

    public static void updateCEPRule(CEPRule rule) throws BrokerException {
        CEPRuleMQ.updateSubscribeMsg(rule, redisService.readAllRulesFromRedis());
    }

    public static CEPRule getCEPRule(String ruleId) {
        return redisService.readRulesFromRedis(ruleId);
    }

    public static Map<String, CEPRule> getCEPRuleMap() {
        return redisService.readAllRulesFromRedis();
    }
}
