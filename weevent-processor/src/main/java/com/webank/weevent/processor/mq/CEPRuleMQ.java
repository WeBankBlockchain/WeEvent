package com.webank.weevent.processor.mq;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.service.AnalysisWeEventIdService;
import com.webank.weevent.processor.utils.CommonUtil;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.RetCode;
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
    private static Map<String, String> subscriptionIdMap = new ConcurrentHashMap<>();

    public static void updateSubscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) throws BrokerException {
        // unsubscribe old the topic
        IWeEventClient client = getClient(rule);
        // when is in run status. update the rule map
        if (1 == rule.getStatus()) {
            ruleMap.put(rule.getId(), rule);
            // update subscribe
            subscribeMsg(rule, ruleMap);
        }
        String subId = subscriptionIdMap.get(rule.getId());
        if (null == subId) {
            client.unSubscribe(subId);
        }
    }

    private static IWeEventClient getClient(CEPRule rule) {
        try {
            Map<String, String> mapRequest = CommonUtil.uRLRequest(rule.getBrokerUrl());
            String baseUrl = CommonUtil.urlPage(rule.getBrokerUrl());
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

    private static void subscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) {
        try {
            IWeEventClient client = getClient(rule);
            // subscribe topic
            log.info("subscribe topic:{}", rule.getFromDestination());
            String subscriptionId = client.subscribe(rule.getFromDestination(), WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    try {
                        String content = new String(event.getContent());
                        log.info("on event:{},content:{}", event.toString(), content);

                        if (CommonUtil.checkValidJson(content)) {
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
            Connection conn = CommonUtil.getConnection(rule.getDatabaseUrl());

            if (conn != null) {
                Map<String, String> urlParamMap = CommonUtil.uRLRequest(rule.getDatabaseUrl());
                StringBuffer insertExpression = new StringBuffer("insert into ");
                insertExpression.append(urlParamMap.get("tableName"));
                insertExpression.append("(");
                StringBuffer values = new StringBuffer(" values (");

                // select key and value
                Map<String, String> sqlvalue = CommonUtil.contactsql(content, rule.getSelectField(), rule.getPayload());
                // just the order key and need write in db
                List<String> keys = CommonUtil.getAllKey(sqlvalue);

                // payload just like the table
                for (int i = 0; i < keys.size(); i++) {
                    if ((keys.size() - 1) == i) {
                        // last key
                        insertExpression.append(keys.get(i)).append(")");
                        values.append("?)");
                    } else {
                        // concat the key
                        insertExpression.append(keys.get(i)).append(",");

                        values.append("?,");
                    }

                }

                StringBuffer query = insertExpression.append(values);
                log.info("query:{}", query);
                PreparedStatement preparedStmt = conn.prepareStatement(query.toString());
                for (int t = 0; t < keys.size(); t++) {
                    preparedStmt.setString(t + 1, sqlvalue.get(keys.get(t)));
                }
                log.info("preparedStmt:{}", preparedStmt.toString());
                // execute the preparedstatement
                int res = preparedStmt.executeUpdate();
                if (res > 0) {
                    System.out.println("insert db success!!!");
                }
                preparedStmt.close();

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
            if (!StringUtils.isEmpty(entry.getValue().getSelectField()) && !(StringUtils.isEmpty(entry.getValue().getPayload())) && !(StringUtils.isEmpty(entry.getValue().getConditionField()))) {

                log.info("check the josn and return fine !");
                if (hitRuleEngine(entry.getValue().getPayload(), content, entry.getValue().getConditionField())) {
                    try {
                        // parsing the payload && match the content,if true and hit it
                        if (entry.getValue().getConditionType().equals(2)) {
                            sendMessageToDB(content, entry.getValue());

                        } else if (entry.getValue().getConditionType().equals(1)) {
                            // select the field and publish the message to the toDestination
                            String eventContent = getContent(content, entry.getValue().getSelectField());
                            log.info("publish select: {},eventContent:{}", entry.getValue().getSelectField(), eventContent);

                            // publish the message
                            Map<String, String> extensions = new HashMap<>();
                            extensions.put("weevent-type", "ifttt");
                            WeEvent weEvent = new WeEvent(entry.getValue().getToDestination(), eventContent.getBytes(StandardCharsets.UTF_8), extensions);
                            log.info("weEvent event {}", weEvent.toString());

                            client.publish(weEvent);
                        }
                    } catch (BrokerException e) {
                        log.error(e.toString());
                    }
                }
            }

        }

    }

    private static String getContent(String content, String selectField) {
        JSONObject eventContent = JSONObject.parseObject(content);
        String[] result = selectField.split(",");
        JSONObject iftttContent = new JSONObject();
        for (int i = 0; i < result.length; i++) {
            if (eventContent.containsKey(result[i])) {
                iftttContent.put(result[i], eventContent.get(result[i]));
            }
        }

        return iftttContent.toString();
    }

    private static boolean hitRuleEngine(String payload, String eventContent, String condition) {
        if (CommonUtil.checkJson(eventContent, payload)) {
            List<String> eventContentKeys = CommonUtil.getKeys(payload);
            JSONObject event = JSONObject.parseObject(eventContent);
            JexlEngine jexl = new JexlBuilder().create();

            JexlContext context = new MapContext();
            for (String key : eventContentKeys) {
                context.set(key, event.get(key));
            }
            // Create an expression  "a>10"
            boolean checkFlag = (Boolean) jexl.createExpression(condition).evaluate(context);
            log.info("payload:{},eventContent:{},condition:{},hit rule:{}", payload, eventContent, condition, checkFlag);
            return checkFlag;
        }
        return false;
    }

    public static RetCode checkCondition(String payload, String condition) {
        try {
            List<String> payloadContentKeys = CommonUtil.getKeys(payload);
            JSONObject payloadJson = JSONObject.parseObject(payload);
            JexlEngine jexl = new JexlBuilder().create();

            JexlContext context = new MapContext();
            for (String key : payloadContentKeys) {
                context.set(key, payloadJson.get(key));
            }
            Boolean e = (Boolean) jexl.createExpression(condition).evaluate(context);
            log.info(e.toString());
            // if can check the true or false,is must be the right number
            return ConstantsHelper.SUCCESS;
        } catch (Exception e) {
            log.info("error number");
            return ConstantsHelper.FAIL;
        }

    }
}

