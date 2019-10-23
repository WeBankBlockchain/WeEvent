package com.webank.weevent.processor.cache;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.mq.CEPRuleMQ;
import com.webank.weevent.sdk.BrokerException;

import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
public class CEPRuleCache {

    public static void updateCEPRule(CEPRule rule, Map<String, CEPRule> ruleMap) throws BrokerException {
        CEPRuleMQ.updateSubscribeMsg(rule, ruleMap);
    }

}

