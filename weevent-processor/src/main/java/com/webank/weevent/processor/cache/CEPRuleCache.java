package com.webank.weevent.processor.cache;

import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.mq.CEPRuleMQ;
import com.webank.weevent.processor.service.RedisService;
import com.webank.weevent.sdk.BrokerException;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class CEPRuleCache {
    private static Map<String, CEPRule> ruleMapRam = new ConcurrentHashMap<>();
    private static List<String> idList = new ArrayList<>();

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    // block data cached in redis
    private static RedisService redisService;

    @Autowired
    private CEPRuleMapper cEPRuleMapper;

    private void initRedisService() {
        if (redisService == null) {
            // load redis service if needed
            String redisServerIp = ProcessorApplication.processorConfig.getRedisServerIp();
            Integer redisServerPort = ProcessorApplication.processorConfig.getRedisServerPort();
            if (StringUtils.isNotBlank(redisServerIp) && redisServerPort > 0) {
                log.info("init redis service");

                redisService = ProcessorApplication.applicationContext.getBean(RedisService.class);
            }
        }
    }
    @PostConstruct
    public void init() {
        // get all rule
        List<CEPRule> dynamicRuleList = cEPRuleMapper.getDynamicCEPRuleAllParamList();
        initRedisService();
        log.info("dynamic rule list {}", dynamicRuleList.size());

        if (!CollectionUtils.isEmpty(dynamicRuleList)) {

            for (CEPRule aDynamicRule : dynamicRuleList) {
                log.info("------------------++{}", JSONObject.toJSONString(aDynamicRule));
                redisService.writeRulesToRedis(aDynamicRule.getId(), aDynamicRule);
                log.info("+++++++++++++++++++++++{}", aDynamicRule.getId());
                idList.add(aDynamicRule.getId());
                ruleMapRam.put(aDynamicRule.getId(),aDynamicRule);
            }

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Iterator<Map.Entry<String, CEPRule>> iter = redisService.readAllRulesFromRedis(idList).entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, CEPRule> entry = iter.next();

                        log.info("start subscribe all topic...:{}",JSONObject.toJSONString(entry.getValue()));
                        CEPRuleMQ.subscribeMsg(entry.getValue(), redisService.readAllRulesFromRedis(idList));
                    }
                }
            });
        }
    }

    public static void addOrUpdateCEPRule(CEPRule rule) throws BrokerException {
        if(redisService.isRuleExistInRedis(rule.getId())){
            // if exist , just update
            CEPRuleCache.updateCEPRule(rule);
        }else{
            //ruleMap.put(rule.getId(), rule);
            redisService.writeRulesToRedis(rule.getId(), rule);

            idList.add(rule.getId());
            // add subscription
            CEPRuleMQ.subscribeMsg(rule, redisService.readAllRulesFromRedis(idList));
        }

    }

    public static void deleteCEPRuleById(String ruleId) {
        // cancel the subscription
        CEPRuleMQ.unSubscribeMsg((CEPRule) redisService.readRulesFromRedis(ruleId), CEPRuleMQ.subscriptionIdMap.get(ruleId));
        redisService.deleteRulesToRedis(ruleId);
    }

    public static void deleteCEPRuleById(CEPRule rule) {
        // cancel the subscription
        String ruleId = rule.getId();
        if (redisService.readRulesFromRedis(ruleId)!=null) {
            CEPRuleMQ.unSubscribeMsg((CEPRule) redisService.readRulesFromRedis(rule.getId()), CEPRuleMQ.subscriptionIdMap.get(ruleId));
        }
        redisService.deleteRulesToRedis(ruleId);
    }

    public static void updateCEPRule(CEPRule rule) throws BrokerException {
        CEPRuleMQ.updateSubscribeMsg(rule, redisService.readAllRulesFromRedis(idList));
    }



    public static CEPRule getCEPRule(String ruleId) {
        return (CEPRule) redisService.readRulesFromRedis(ruleId);
    }

    public static Map<String, CEPRule> getCEPRuleMap() {
        return redisService.readAllRulesFromRedis(idList);
    }
}

