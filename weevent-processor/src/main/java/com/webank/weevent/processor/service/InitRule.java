package com.webank.weevent.processor.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class InitRule {
    @Autowired
    private CEPRuleMapper cEPRuleMapper;

    @PostConstruct
    public void init() {
        initMap();
    }

    //<id--CEPRULR>
    private static Map<String, CEPRule> ruleMap = new HashMap<>();
    static List<CEPRule> dynamicRuleList;

    public List<CEPRule> initMap() {
        // get all rule
        dynamicRuleList = cEPRuleMapper.getDynamicCEPRuleList();
        for (int i = 0; i < dynamicRuleList.size(); i++) {
            ruleMap.put(dynamicRuleList.get(i).getId(), dynamicRuleList.get(i));
        }
        log.info("rulemap size", ruleMap.size());
        subscriptionTopic();
        return dynamicRuleList;
    }

    private void subscriptionTopic() {
        InitRuleThread initRule = new InitRuleThread();

        new Thread(initRule).run();
    }

    private class InitRuleThread implements Runnable {
        // subscription  all topic
        public void run() {
            try {

                for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
                    log.info("key  {} value = {} ", entry.getKey(), entry.getValue());
                    IWeEventClient client = IWeEventClient.build(entry.getValue().getBrokerUrl());

                    // subscribe topic
                    client.subscribe(entry.getValue().getToDestination(), WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                        @Override
                        public void onEvent(WeEvent event) {
                            System.out.println("received event: " + event.toString());
                        }

                        @Override
                        public void onException(Throwable e) {

                        }
                    });
                }
            } catch (BrokerException e) {
                log.info("BrokerException{}", e.toString());
            }
        }

    }

}

// Thread-->线程池

