package com.webank.weevent.processor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
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
    private List<CEPRule> dynamicRuleList;

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
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(5));
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            InitRuleThread InitRuleThread = new InitRuleThread(entry.getValue());
            executor.execute(InitRuleThread);
            log.info("thread pool number:{}，queue waiting number:{}，finish number:" ,executor.getPoolSize(),
                    executor.getQueue().size() , executor.getCompletedTaskCount());
        }
        executor.shutdown();
    }

    private class InitRuleThread implements Runnable {
        private CEPRule rule;

        // subscription  all topic
         InitRuleThread(CEPRule rule) {
            this.rule = rule;
        }

        public void run() {
            try {
                // for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
                log.info("rulr ", this.rule.toString());
                IWeEventClient client = IWeEventClient.build(this.rule.getBrokerUrl());

                // subscribe topic
                client.subscribe(this.rule.getToDestination(), WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                    @Override
                    public void onEvent(WeEvent event) {
                        System.out.println("received event: " + event.toString());
                    }

                    @Override
                    public void onException(Throwable e) {

                    }
                });
//                }
            } catch (BrokerException e) {
                log.info("BrokerException{}", e.toString());
            }
        }

    }

}

// 1. Thread-->线程池

