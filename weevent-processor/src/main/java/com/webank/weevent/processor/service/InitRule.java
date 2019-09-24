package com.webank.weevent.processor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.webank.weevent.processor.mapper.CEPRuleMapper;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.Util;
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

    //<id --> cep rule>
    private static Map<String, CEPRule> ruleMap = new HashMap<>();

    public List<CEPRule> initMap() {
        List<CEPRule> dynamicRuleList;
        // get rule detail
        dynamicRuleList = cEPRuleMapper.getDynamicCEPRuleList();
        for (CEPRule ruleList : dynamicRuleList) {
            ruleMap.put(ruleList.getId(), ruleList);
        }
        log.info("rulemap size", ruleMap.size());
        subscriptionTopic();
        return dynamicRuleList;
    }

    private void subscriptionTopic() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(5));
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            InitRuleThread InitRuleThread = new InitRuleThread(entry.getValue(), ruleMap);
            executor.execute(InitRuleThread);
            log.info("thread pool number:{}，queue waiting number:{}，finish number:", executor.getPoolSize(),
                    executor.getQueue().size(), executor.getCompletedTaskCount());
        }
        executor.shutdown();
    }

    private class InitRuleThread implements Runnable {
        private CEPRule rule;
        private Map<String, CEPRule> ruleList;

        // subscription  all topic
        InitRuleThread(CEPRule rule, Map<String, CEPRule> list) {
            this.rule = rule;
            this.ruleList = list;
        }

        private void publishToTopic(IWeEventClient client, String toDestination, String selectFields, String content) {
            try {
                // :TODO select the field
                log.info(selectFields);
                // publish the message
                log.info("publish topic {}",toDestination);
                client.publish(toDestination, content.getBytes());
            } catch (BrokerException e) {
                log.error(e.toString());
            }
        }


        private boolean checkJson(String content, String objJson) {
            boolean tag = false;
            //parsing and match
            if (!content.isEmpty() && !objJson.isEmpty()) {
                List<String> contentKeys = Util.getKeys(content);
                List<String> objJsonKeys = Util.getKeys(objJson);

                // objJsonKeys must longer than the contentKeys
                if (contentKeys.size() > objJsonKeys.size()) {
                    return tag;
                } else {
                    // check objJsonKeys contain contentKeys
                    for (String objJsonKey : objJsonKeys) {
                        for (String contentKey : contentKeys) {
                            if (!contentKey.equals(objJsonKey)) {
                                return tag;
                            }
                        }
                    }
                }
            }
            return tag;
        }

        // hit the target(rule)
        private void handleOnEvent(WeEvent event, IWeEventClient client) {

            // get the content ,and parsing it  byte[]-->String
            String content = new String(event.getContent());

            // match the rule and send message
            for (Map.Entry<String, CEPRule> entry : ruleList.entrySet()) {

                // match payload and condition
                boolean flag = false;
                if (!entry.getValue().getPayload().isEmpty() && !entry.getValue().getConditionField().isEmpty()) {

                    // parsing the payload && match the content
                    if (checkJson(content, entry.getValue().getPayload())) {
                        flag = true;
                        // :TODO   parsing sql
                        // parsing the payload && match the content,if success flag is true,then is false
//                        if (checkJson(content, entry.getValue().getConditionField())) {
//                            flag = true;
//                        }
                    }
                }

                if (flag) {
                    // select the field and publish the message to the toDestination
                    publishToTopic(client, entry.getValue().getToDestination(), entry.getValue().getSelectField(), content);
                }

            }

        }

        public void run() {
            try {

                log.info("rule ", this.rule.toString());
                IWeEventClient client = IWeEventClient.build(this.rule.getBrokerUrl());

                // subscribe topic
                client.subscribe(this.rule.getToDestination(), WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                    @Override
                    public void onEvent(WeEvent event) {
                        log.info("received event: " + event.toString());
                        // hit the rule
                        handleOnEvent(event, client);
                    }

                    @Override
                    public void onException(Throwable e) {

                    }
                });
            } catch (BrokerException e) {
                log.info("BrokerException{}", e.toString());
            }
        }

    }

}

