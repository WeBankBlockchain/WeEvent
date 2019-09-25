package com.webank.weevent.processor.mq;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.Util;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.WeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CEPRuleMQ {
    public static Map<String, String> subscriptionIdMap = new ConcurrentHashMap<>();

    public static void subscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) {
        try {
            IWeEventClient client = IWeEventClient.build(rule.getBrokerUrl());
            // subscribe topic
            String subscriptionId = client.subscribe(rule.getToDestination(), WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    log.info("on event{}",event.toString());
                    handleOnEvent(event, client, ruleMap);
                }

                @Override
                public void onException(Throwable e) {

                }
            });
            subscriptionIdMap.put(rule.getId(), subscriptionId);
        } catch (BrokerException e) {
            log.info("BrokerException{}", e.toString());
        }
    }

    public static void unSubscribeMsg(CEPRule rule, String subscriptionId) {
        try {
            IWeEventClient client = IWeEventClient.build(rule.getBrokerUrl());
            client.unSubscribe(subscriptionId);
        } catch (BrokerException e) {
            log.info("BrokerException{}", e.toString());
        }
    }

    private static void handleOnEvent(WeEvent event, IWeEventClient client, Map<String, CEPRule> ruleMap) {
        // get the content ,and parsing it  byte[]-->String
        String content = new String(event.getContent());
        // match the rule and send message
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            if (!StringUtils.isEmpty(entry.getValue().getPayload())
                    && !StringUtils.isEmpty(entry.getValue().getConditionField())) {
                // parsing the payload && match the content
                if (checkJson(content, entry.getValue().getPayload())) {
                    // select the field and publish the message to the toDestination
                    try {
                        // :TODO select the field
                        log.info(entry.getValue().getSelectField());
                        // publish the message
                        log.info("publish topic {}",entry.getValue().getSelectField());
                        client.publish(entry.getValue().getToDestination(), content.getBytes());
                    } catch (BrokerException e) {
                        log.error(e.toString());
                    }
                }
            }
        }

    }

    private static boolean checkJson(String content, String objJson) {
        boolean tag = false;
        //parsing and match
        if (!StringUtils.isEmpty(content)
                && !StringUtils.isEmpty(objJson)) {
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
}
