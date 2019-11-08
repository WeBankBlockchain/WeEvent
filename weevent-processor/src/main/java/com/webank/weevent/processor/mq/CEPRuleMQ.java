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
    // <subscriptionId-->client session>
    private static Map<String, IWeEventClient> subscriptionClientMap = new ConcurrentHashMap<>();


    public static void updateSubscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) throws BrokerException {
        // when is in run status. update the rule map

        if (1 == rule.getStatus()) {
            ruleMap.put(rule.getId(), rule);
            // update subscribe
            subscribeMsg(rule, ruleMap);
            log.info("start rule ,and subscribe rule:{}", rule.getId());
        }
        if (0 == rule.getStatus() || 2 == rule.getStatus()) {
            log.info("stop,update,delete rule subscriptionIdMap.size:{}", subscriptionIdMap.size());

            // update unsubscribe
            String subId = subscriptionIdMap.get(rule.getId());
            log.info("stop,update,delete rule ,and unsubscribe,subId :{}", subId);
            IWeEventClient client = subscriptionClientMap.get(subId);
            boolean flag = client.unSubscribe(subId);
            log.info("stop,update,delete rule ,and unsubscribe return {}",flag);
        }
    }

    private static IWeEventClient getClient(CEPRule rule) {
        try {
            Map<String, String> mapRequest = CommonUtil.uRLRequest(rule.getBrokerUrl());
            String baseUrl = CommonUtil.urlPage(rule.getBrokerUrl());
            IWeEventClient client;
            if (null != mapRequest.get("groupId")) {
                client = IWeEventClient.build(baseUrl, mapRequest.get("groupId"));
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
            String subscriptionId;
            if (StringUtils.isEmpty(rule.getOffSet())) {
                subscriptionId = client.subscribe(rule.getFromDestination(), WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                    @Override
                    public void onEvent(WeEvent event) {
                        try {

                            String content = new String(event.getContent());
                            log.info("on event:{},content:{}", event.toString(), content);

                            if (CommonUtil.checkValidJson(content)) {
                                handleOnEvent(event, ruleMap);
                            }
                        } catch (JSONException e) {
                            log.error(e.toString());
                        }
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.info("on event:{}", e.toString());
                    }
                });
            } else {
                subscriptionId = client.subscribe(rule.getFromDestination(), rule.getOffSet(), new IWeEventClient.EventListener() {
                    @Override
                    public void onEvent(WeEvent event) {
                        try {

                            String content = new String(event.getContent());
                            log.info("on event:{},content:{}", event.toString(), content);

                            if (CommonUtil.checkValidJson(content)) {
                                handleOnEvent(event, ruleMap);
                            }
                        } catch (JSONException e) {
                            log.error(e.toString());
                        }
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.info("on event:{}", e.toString());
                    }
                });
            }
            log.info("subscriptionIdMap:{},rule.getId() :{}--->subscriptionId:{}", subscriptionIdMap.size(), rule.getId(), subscriptionId);
            subscriptionIdMap.put(rule.getId(), subscriptionId);
            subscriptionClientMap.put(subscriptionId,client);
            log.info("after add success subscriptionIdMap:{}", subscriptionIdMap.size());

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

    private static void sendMessageToDB(String groupId, WeEvent eventContent, CEPRule rule) {
        try {
            try (Connection conn = CommonUtil.getConnection(rule.getDatabaseUrl());) {

                if (conn != null) {
                    // get the sql params
                    Map<String, String> urlParamMap = CommonUtil.uRLRequest(rule.getDatabaseUrl());

                    // get the insert sql
                    StringBuffer insertExpression = new StringBuffer("insert into ");
                    insertExpression.append(urlParamMap.get("tableName"));
                    insertExpression.append("(");
                    StringBuffer values = new StringBuffer(" values (");

                    // select key and value
                    Map<String, String> sqlvalue = CommonUtil.contactsql(rule.getBrokerId(), groupId, eventContent, rule.getSelectField(), rule.getPayload());

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
            }
        } catch (SQLException e) {
            log.info(e.toString());
        }

    }

    private static String getGroupId(CEPRule rule) {
        // get the groupId
        String groupId = "";
        Map<String, String> mapRequest = CommonUtil.uRLRequest(rule.getBrokerUrl());
        if (null != mapRequest.get("groupId")) {
            groupId = mapRequest.get("groupId");
        }
        return groupId;
    }

    private static void handleOnEvent(WeEvent event, Map<String, CEPRule> ruleMap) {
        log.info("handleOnEvent ruleMapsize :{}", ruleMap.size());

        // match the rule and send message
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            if (!StringUtils.isEmpty(entry.getValue().getSelectField()) && !(StringUtils.isEmpty(entry.getValue().getPayload()))) {

                log.info("check the josn and return fine !");
                if (hitRuleEngine(entry.getValue().getPayload(), event, entry.getValue().getConditionField())) {
                    try {
                        // get the system parameter
                        String groupId = getGroupId(entry.getValue());
                        // parsing the payload && match the content,if true and hit it
                        if (entry.getValue().getConditionType().equals(2)) {
                            sendMessageToDB(groupId, event, entry.getValue());

                        } else if (entry.getValue().getConditionType().equals(1)) {
                            // select the field and publish the message to the toDestination
                            String eventContent = setWeEventContent(entry.getValue().getBrokerId(), groupId, event, entry.getValue().getSelectField(), entry.getValue().getPayload());
                            log.info("publish select: {},eventContent:{}", entry.getValue().getSelectField(), eventContent);

                            // publish the message
                            Map<String, String> extensions = new HashMap<>();
                            extensions.put("weevent-type", "ifttt");
                            WeEvent weEvent = new WeEvent(entry.getValue().getToDestination(), eventContent.getBytes(StandardCharsets.UTF_8), extensions);
                            log.info("after hitRuleEngine weEvent event {}", weEvent.toString());
                            IWeEventClient client = getClient(entry.getValue());
                            client.publish(weEvent);
                        }
                    } catch (BrokerException e) {
                        log.error(e.toString());
                    }
                }
            }

        }

    }

    private static String setWeEventContent(String brokerId, String groupId, WeEvent eventMessage, String selectField, String payload) {
        String content = new String(eventMessage.getContent());
        JSONObject eventContent = JSONObject.parseObject(content);
        JSONObject payloadContent = JSONObject.parseObject(payload);

        // match the table
        JSONObject iftttContent = new JSONObject();
        // check the star and get all parameters
        if ("*".equals(selectField)) {
            for (Map.Entry<String, Object> entry : payloadContent.entrySet()) {
                if (eventContent.containsKey(entry.getKey())) {
                    iftttContent.put(entry.getKey(), eventContent.get(entry.getKey()));
                }
            }
        }
        // get all fields
        String[] result = selectField.split(",");
        // event content must contain the select message
        for (int i = 0; i < result.length; i++) {
            if (eventContent.containsKey(result[i])) {
                iftttContent.put(result[i], eventContent.get(result[i]));
            }
            // if contain the eventId ,need  add the field
            if (ConstantsHelper.EVENT_ID.equals(result[i])) {
                iftttContent.put(result[i], eventMessage.getEventId());
            }
            if (ConstantsHelper.TOPIC_NAME.equals(result[i])) {
                iftttContent.put(result[i], eventMessage.getTopic());
            }
            if (ConstantsHelper.BROKER_ID.equals(result[i])) {
                iftttContent.put(result[i], brokerId);
            }
            if (ConstantsHelper.GROUP_ID.equals(result[i])) {
                iftttContent.put(result[i], groupId);
            }
        }

        return iftttContent.toString();
    }

    private static boolean handleTheEqual(WeEvent eventMessage, String condition) {
        String eventContent = new String(eventMessage.getContent());
        JSONObject event = JSONObject.parseObject(eventContent);
        String[] strs = condition.split("=");
        if (strs.length == 2) {
            // event contain left key
            if (event.containsKey(strs[0]) && event.get(strs[0]).toString().equals(strs[1])) {
                log.info("get the a=1 pattern {}", "true");
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private static boolean hitRuleEngine(String payload, WeEvent eventMessage, String condition) {
        try {
            String eventContent = new String(eventMessage.getContent());
            // all parameter must be the same
            if (CommonUtil.checkJson(eventContent, payload) && (StringUtils.isEmpty(condition))) {
                // if the confition is empty, just return all message
                return true;
            } else if (CommonUtil.checkJson(eventContent, payload)) {
                List<String> eventContentKeys = CommonUtil.getKeys(payload);
                JSONObject event = JSONObject.parseObject(eventContent);
                JexlEngine jexl = new JexlBuilder().create();

                JexlContext context = new MapContext();
                for (String key : eventContentKeys) {
                    context.set(key, event.get(key));
                }
                // check the expression ,if match then true
                log.info("condition:{}", condition);
                boolean checkFlag = (Boolean) jexl.createExpression(condition).evaluate(context);
                log.info("payload:{},eventContent:{},condition:{},hit rule:{}", payload, eventContent, condition, checkFlag);
                return checkFlag;
            }
            log.info("payload:{},eventContent:{},condition:{},hit rule:false", payload, eventContent, condition);
            return false;
        } catch (Exception e) {
            if (handleTheEqual(eventMessage, condition)) {
                log.info("single equal match");
                return true;
            } else {
                log.info("error number");
                return false;
            }

        }
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
            JSONObject event = JSONObject.parseObject(payload);
            String[] strs = condition.split("=");
            boolean flag = false;
            if (strs.length == 2 && !(strs[0].contains("<") || strs[0].contains(">") || (strs[1].contains("<") || strs[1].contains(">")))) {
                flag = true;
            }
            if (flag) {
                // event contain left key
                if (event.containsKey(strs[0])) {
                    if (event.get(strs[0]) instanceof String) {
                        log.info("{}", "true1");
                        return ConstantsHelper.SUCCESS;

                    } else {
                        if (event.get(strs[0]) instanceof Number) {
                            if (strs[1].matches("[0-9]+")) {
                                log.info("{}", "true2");
                                return ConstantsHelper.SUCCESS;

                            } else {
                                log.info("{}", "false 1");
                                return ConstantsHelper.FAIL;

                            }
                        }
                    }
                } else {
                    log.info("{}", "false 2");
                    return ConstantsHelper.FAIL;
                }
            } else {
                Boolean e = (Boolean) jexl.createExpression(condition).evaluate(context);
                log.info("rusult:{}", e);
                return ConstantsHelper.SUCCESS;

            }
        } catch (Exception e) {
            log.info("error number");
            return ConstantsHelper.FAIL;
        }
        return ConstantsHelper.FAIL;
    }

}

