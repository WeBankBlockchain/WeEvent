package com.webank.weevent.processor.mq;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.service.AnalysisWeEventIdService;
import com.webank.weevent.processor.utils.Util;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.springframework.util.StringUtils;

@Slf4j
public class CEPRuleMQ {
    // <ruleId <--> subscriptionId>
    public static Map<String, String> subscriptionIdMap = new ConcurrentHashMap<>();

    public static void updateSubscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) throws BrokerException {
        // unsubscribe old the topic
        ruleMap.get(rule.getId()).getToDestination();
        IWeEventClient client = getClient(rule);
        // update the rule map
        ruleMap.put(rule.getId(), rule);
        // update subscribe
        subscribeMsg(rule, ruleMap);
        client.unSubscribe(subscriptionIdMap.get(rule.getId()));
    }

    private static IWeEventClient getClient(CEPRule rule) {
        try {
            Map<String, String> mapRequest = Util.uRLRequest(rule.getBrokerUrl());
            String baseUrl = Util.urlPage(rule.getBrokerUrl());
            IWeEventClient client;
            if (null != mapRequest.get("groupId")) {
                //  client = IWeEventClient.build(baseUrl,mapRequest.get("groupId"));
                client = IWeEventClient.build(baseUrl);
            } else {
                client = IWeEventClient.build(baseUrl);
            }
            return client;
        } catch (BrokerException e) {
            log.info("BrokerException{}", e.toString());
            return null;
        }

    }

    public static void subscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) {
        try {
            IWeEventClient client = getClient(rule);;
            // subscribe topic
            log.info("subscribe topic:{}", rule.getFromDestination());
            String subscriptionId = client.subscribe(rule.getFromDestination(), WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    try {
                        String content = new String(event.getContent());
                        log.info("on event:{},content:{}", event.toString(), content);
                        if (Util.checkValidJson(content)) {
                            handleOnEvent(event, client, ruleMap);
                        }
                        //Analysis WeEventId  to the governance database
                        AnalysisWeEventIdService.analysisWeEventId(rule, event.getEventId());
                    } catch (JSONException | BrokerException e) {
                        log.error(e.toString());
                    }
                }

                @Override
                public void onException(Throwable e) {
                    log.info("on event:{}", e.toString());
                }
            });
            subscriptionIdMap.put(rule.getId(), subscriptionId);
        } catch (BrokerException e) {
            log.info("BrokerException{}", e.toString());
        }
    }

    public static void unSubscribeMsg(CEPRule rule, String subscriptionId) {
        try {
            IWeEventClient client = getClient(rule);
            log.info("id:{},sunid:{}", rule.getId(), subscriptionId);
            client.unSubscribe(subscriptionId);
        } catch (BrokerException e) {
            log.info("BrokerException{}", e.toString());
        }
    }

    private static void sendMessageToDB(String content, CEPRule rule) {
        JSONObject eventContent = JSONObject.parseObject(content);
        try {
            Connection conn = Util.getConnection(rule.getDatabaseUrl());

            if (conn != null) {
                Map<String, String> urlParamMap = Util.uRLRequest(rule.getDatabaseUrl());
                String insertExpression = "insert into ".concat(urlParamMap.get("tableName").concat("("));
                String values = "values (";
                Map<String, Integer> sqlvalue = Util.contactsql(content, rule.getPayload());
                List<String> result = new ArrayList(sqlvalue.keySet());

                //contact insert into users (first_name, last_name, date_created, is_admin, num_points)
                for (Map.Entry<String, Integer> entry : sqlvalue.entrySet()) {
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                    if (entry.getValue().equals(1)) {
                        if (entry.getValue().equals(result.get(result.size() - 1))) {
                            insertExpression.concat(entry.getKey()).concat(")");
                            values.concat("?）");
                        } else {
                            insertExpression.concat(entry.getKey()).concat(",");
                            values.concat("?，");
                        }
                    }
                }

                String query = insertExpression.concat(values);
                log.info("query:{}", query);
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                for (int t = 0; t < result.size(); t++) {
                    preparedStmt.setString(t + 1, eventContent.get(result.get(t)).toString());
                }
                // execute the preparedstatement
                preparedStmt.execute();
                conn.close();
            }
        } catch (SQLException e) {
            log.info(e.toString());
        }

    }

    private static void handleOnEvent(WeEvent event, IWeEventClient client, Map<String, CEPRule> ruleMap) {
        log.info("handleOnEvent ruleMapsize :{}", ruleMap.size());

        // get the content ,and parsing it  byte[]-->String
        String content = new String(event.getContent());

        // match the rule and send message
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            if (!StringUtils.isEmpty(entry.getValue().getPayload())
                    && !StringUtils.isEmpty(entry.getValue().getConditionField())) {

                // parsing the payload && match the content,if true and hit it
                if (entry.getValue().getConditionType().equals(2)) {
                    sendMessageToDB(content, entry.getValue());

                } else if (hitRuleEngine(entry.getValue().getPayload(), content, entry.getValue().getConditionField())) {

                    // select the field and publish the message to the toDestination
                    try {
                        if (entry.getValue().getConditionType().equals(1)) {
                            // publish the message
                            log.info("publish topic {}", entry.getValue().getSelectField());
                            client.publish(entry.getValue().getToDestination(), content.getBytes());
                        }
                    } catch (BrokerException e) {
                        log.error(e.toString());
                    }
                }
            }

        }

    }


    private static boolean hitRuleEngine(String payload, String eventContent, String condition) {
        if (Util.checkJson(eventContent, payload)) {
            List<String> eventContentKeys = Util.getKeys(payload);
            JSONObject event = JSONObject.parseObject(eventContent);
            JexlEngine jexl = new JexlBuilder().create();

            JexlContext context = new MapContext();
            for (String key : eventContentKeys) {
                context.set(key, event.get(key));
            }
            // Create an expression  "a>10"
            return (Boolean) jexl.createExpression(condition).evaluate(context);
        }
        return Boolean.FALSE;
    }

}

