package com.webank.weevent.processor.mq;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.StatisticRule;
import com.webank.weevent.processor.model.StatisticWeEvent;
import com.webank.weevent.processor.utils.CommonUtil;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.RetCode;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import javafx.util.Pair;
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
    // Pair<key, value>--><WeEvent, CEPRule>
    private static BlockingDeque<Pair<WeEvent, CEPRule>> systemMessageQueue = new LinkedBlockingDeque<>();
    // client --><brokerurl,groupId>
    private static Map<IWeEventClient, Pair<String, String>> clientGroupMap = new ConcurrentHashMap<>();

    private static CEPRuleMQ.DBThread dbThread = new CEPRuleMQ.DBThread();

    private static StatisticWeEvent statisticWeEvent = new StatisticWeEvent();

    @PostConstruct
    public void init() {
        // get all rule
        log.info("start dBThread ...");
        new Thread(dbThread).start();
    }

    public static StatisticWeEvent getStatisticWeEvent() {
        log.info("getStatisticWeEvent:{}", statisticWeEvent);
        return statisticWeEvent;
    }

    public static void updateSubscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) throws BrokerException {
        // when is in run status. update the rule map
        // update unsubscribe
        String subId = subscriptionIdMap.get(rule.getId());
        statistic(ruleMap);
        if (1 == rule.getStatus()) {
            if (null != subId) {
                IWeEventClient client = subscriptionClientMap.get(subId);
                // if they are equal
                for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
                    if (!(rule.getFromDestination().equals(entry.getValue().getFromDestination()))) {
                        boolean flag = client.unSubscribe(subId);
                        log.info("start rule ,and subscribe flag:{}", flag);
                    }
                }

                subscribeMsg(rule, ruleMap, client);

            } else {
                ruleMap.put(rule.getId(), rule);
                // update subscribe
                subscribeMsg(rule, ruleMap, null);
                log.info("start rule ,and subscribe rule:{}", rule.getId());
            }
        }
        if (0 == rule.getStatus() || 2 == rule.getStatus()) {
            log.info("stop,update,delete rule subscriptionIdMap.size:{}", subscriptionIdMap.size());

            log.info("stop,update,delete rule ,and unsubscribe,subId :{}", subId);
            if (null != subId) {
                IWeEventClient client = subscriptionClientMap.get(subId);
                boolean flag = client.unSubscribe(subId);
                if (!StringUtils.isEmpty(subscriptionClientMap.get(subId))) {
                    clientGroupMap.remove(subscriptionClientMap.get(subId));
                    subscriptionIdMap.remove(rule.getId());
                    subscriptionClientMap.remove(subId);
                }

                log.info("stop,update,delete rule ,and unsubscribe return {}", flag);
            }

        }
    }

    private static IWeEventClient getClient(CEPRule rule) {
        try {
            String baseUrl = CommonUtil.urlPage(rule.getBrokerUrl());
            // set group id
            String groupId = rule.getGroupId();
            IWeEventClient client;
            if (null != groupId) {
                client = IWeEventClient.build(baseUrl, groupId);
                Pair<String, String> brokerMessage = new Pair<>(baseUrl, groupId);
                clientGroupMap.put(client, brokerMessage);
            } else {
                client = IWeEventClient.build(baseUrl, "");
                Pair<String, String> brokerMessage = new Pair<>(baseUrl, "");
                clientGroupMap.put(client, brokerMessage);
            }
            return client;
        } catch (BrokerException e) {
            log.info("BrokerException{}", e.toString());
            return null;
        }

    }

    private static void subscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap, IWeEventClient clientOld) {
        try {
            IWeEventClient client;

            if (null == clientOld) {
                client = getClient(rule);
            } else {
                client = clientOld;
            }

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

                            Pair<String, String> type;
                            // check the content
                            if (JSONObject.isValid(content)) {
                                type = handleOnEvent(client, event, ruleMap);
                            } else {
                                type = handleOnEventOtherPattern(client, event, ruleMap);
                            }
                            statisticOrderType(type);
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
                            Pair<String, String> type;
                            // check the content
                            if (JSONObject.isValid(content)) {
                                type = handleOnEvent(client, event, ruleMap);
                            } else {
                                type = handleOnEventOtherPattern(client, event, ruleMap);
                            }
                            statisticOrderType(type);
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
            log.info("subscriptionIdMap:{},rule.getId() :{} getFromDestination:{}--->subscriptionId:{}", subscriptionIdMap.size(), rule.getId(), rule.getFromDestination(), subscriptionId);
            subscriptionIdMap.put(rule.getId(), subscriptionId);
            subscriptionClientMap.put(subscriptionId, client);
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

    private static String sendMessageToDB(String groupId, WeEvent eventContent, CEPRule rule) {
        try {
            try (Connection conn = CommonUtil.getDbcpConnection(rule.getDatabaseUrl())) {

                if (conn != null) {
                    // get the sql params
                    Map<String, String> urlParamMap = CommonUtil.uRLRequest(rule.getDatabaseUrl());

                    // get the insert sql
                    StringBuffer insertExpression = new StringBuffer("insert into ");
                    insertExpression.append(rule.getTableName());
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

                    preparedStmt.close();
                    conn.close();

                    if (res > 0) {
                        log.info("insert db success...");
                        return ConstantsHelper.WRITE_DB_SUCCESS;
                    } else {
                        return ConstantsHelper.WRITE_DB_FAIL;
                    }
                }
            }
        } catch (SQLException e) {
            statisticWeEvent.getStatisticRuleMap().get(rule.getId()).setLastFailReason(e.toString());
            log.info(e.toString());
            return ConstantsHelper.LAST_FAIL_REASON;
        }
        return "";
    }

    private static Pair<String, String> handleOnEventOtherPattern(IWeEventClient client, WeEvent event, Map<String, CEPRule> ruleMap) {
        log.info("handleOnEvent ruleMapsize :{}", ruleMap.size());

        // match the rule and send message
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            StatisticRule rule = statisticWeEvent.getStatisticRuleMap().get(entry.getValue().getId());

            log.info("group:{},client:brokerUrl:{},rule:brokerUr{}", clientGroupMap.get(client).getValue().equals(entry.getValue().getGroupId()), clientGroupMap.get(client).getKey(), CommonUtil.urlPage(entry.getValue().getBrokerUrl()));
            // check the broker and groupid
            if (!(clientGroupMap.get(client).getValue().equals(entry.getValue().getGroupId()) && clientGroupMap.get(client).getKey().equals(CommonUtil.urlPage(entry.getValue().getBrokerUrl())))) {
                // update the  statistic weevent
                rule.setNotHitTimes(rule.getNotHitTimes() + 1);
                continue;
            }
            // write the # topic to history db
            if ("1".equals(entry.getValue().getSystemTag()) && entry.getValue().getFromDestination().equals("#") && entry.getValue().getConditionType().equals(2)) {

                log.info("system insert db:{}", entry.getValue().getId());
                Pair<WeEvent, CEPRule> messagePair = new Pair<>(event, entry.getValue());
                systemMessageQueue.add(messagePair);
                // update the  statistic weevent
                return new Pair<>(ConstantsHelper.HIT_TIMES, entry.getValue().getId());
            } else {
                return new Pair<>(ConstantsHelper.NOT_HIT_TIMES, entry.getValue().getId());
            }
        }
        return new Pair<>(ConstantsHelper.OTHER, "");
    }

    private static boolean checkTheInput(Map.Entry<String, CEPRule> entry, IWeEventClient client) {
        if (StringUtils.isEmpty(subscriptionIdMap.get(entry.getValue().getId())) || StringUtils.isEmpty(subscriptionClientMap.get(subscriptionIdMap.get(entry.getValue().getId())))) {
            return true;
        }


        log.debug("client:{},group:{},client:brokerUrl:{},rule:brokerUr{}", subscriptionClientMap.get(subscriptionIdMap.get(entry.getValue().getId())).equals(client), clientGroupMap.get(client).getValue().equals(entry.getValue().getGroupId()), clientGroupMap.get(client).getKey(), CommonUtil.urlPage(entry.getValue().getBrokerUrl()));
        // check the broker and group id
        return (!(subscriptionClientMap.get(subscriptionIdMap.get(entry.getValue().getId())).equals(client) && clientGroupMap.get(client).getValue().equals(entry.getValue().getGroupId()) && clientGroupMap.get(client).getKey().equals(CommonUtil.urlPage(entry.getValue().getBrokerUrl()))));

    }


    private static Pair<String, String> handleOnEvent(IWeEventClient client, WeEvent event, Map<String, CEPRule> ruleMap) {
        log.info("handleOnEvent ruleMapsize :{}", ruleMap.size());
        // match the rule and send message
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            StatisticRule rule = statisticWeEvent.getStatisticRuleMap().get(entry.getValue().getId());

            // check the parameter
            if (checkTheInput(entry, client)) {
                continue;
            }

            // write the # topic to history db  or ifttt message
            if ("1".equals(entry.getValue().getSystemTag()) && entry.getValue().getFromDestination().equals("#") && entry.getValue().getConditionType().equals(2)) {
                log.info("system insert db:{}", entry.getValue().getId());
                Pair<WeEvent, CEPRule> messagePair = new Pair<>(event, entry.getValue());
                systemMessageQueue.add(messagePair);

                return new Pair<>(ConstantsHelper.HIT_TIMES, entry.getValue().getId());

            } else {

                if (StringUtils.isEmpty(entry.getValue().getSelectField()) || (StringUtils.isEmpty(entry.getValue().getPayload()))) {
                    continue;
                }

                if (hitRuleEngine(entry.getValue().getPayload(), event, entry.getValue().getConditionField())) {
                    try {
                        // update the  statistic weevent
                        rule.setHitTimes(rule.getHitTimes() + 1);
                        // get the system parameter
                        String groupId = entry.getValue().getGroupId();
                        // parsing the payload && match the content,if true and hit it
                        if (entry.getValue().getConditionType().equals(2)) {
                            log.info("entry: {}", entry.getValue().toString());

                            log.info("event hit the db and insert: {}", event.toString());

                            String result = sendMessageToDB(groupId, event, entry.getValue());
                            return new Pair<>(result, entry.getValue().getId());

                        } else if (entry.getValue().getConditionType().equals(1)) {
                            // select the field and publish the message to the toDestination
                            String eventContent = setWeEventContent(entry.getValue().getBrokerId(), groupId, event, entry.getValue().getSelectField(), entry.getValue().getPayload());
                            log.info("publish select: {},eventContent:{}", entry.getValue().getSelectField(), eventContent);

                            // publish the message
                            WeEvent weEvent = new WeEvent(entry.getValue().getToDestination(), eventContent.getBytes(StandardCharsets.UTF_8), event.getExtensions());
                            log.info("after hitRuleEngine weEvent  groupId: {}, event:{}", groupId, weEvent.toString());
                            IWeEventClient toDestinationClient = getClient(entry.getValue());
                            SendResult result = toDestinationClient.publish(weEvent);

                            // update the  statistic weevent
                            if ("SUCCESS".equals(result.getStatus())) {
                                return new Pair<>(ConstantsHelper.PUBLISH_EVENT_SUCCESS, entry.getValue().getId());
                            } else {
                                return new Pair<>(ConstantsHelper.PUBLISH_EVENT_FAIL, entry.getValue().getId());
                            }
                        }
                    } catch (BrokerException e) {
                        log.error(e.toString());
                        return new Pair<>(ConstantsHelper.LAST_FAIL_REASON, entry.getValue().getId());
                    }
                } else {
                    return new Pair<>(ConstantsHelper.NOT_HIT_TIMES, entry.getValue().getId());
                }
            }
        }
        return new Pair<>(ConstantsHelper.OTHER, "");
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
                        return ConstantsHelper.SUCCESS;

                    } else {
                        if (event.get(strs[0]) instanceof Number) {
                            if (strs[1].matches("[0-9]+")) {
                                return ConstantsHelper.SUCCESS;

                            } else {
                                return ConstantsHelper.FAIL;

                            }
                        }
                    }
                } else {
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

    private static StatisticWeEvent statistic(Map<String, CEPRule> ruleMap) {
        Map<String, StatisticRule> statisticRuleMap = new HashMap<>();
        Map<String, String> userRuleMap = new HashMap<>();

        int systemAmount = 0;
        int userAmount = 0;
        int runAmount = 0;

        // get all rule details
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            CEPRule rule = entry.getValue();
            StatisticRule statisticRule = new StatisticRule();
            statisticRule.setId(rule.getId());
            statisticRule.setStartTime(rule.getCreatedTime());
            statisticRuleMap.put(rule.getId(), statisticRule);
            userRuleMap.put(rule.getUserId(), rule.getId());
            if ("1".equals(rule.getSystemTag())) {
                systemAmount++;
            } else {
                userAmount++;
            }
            if ("1".equals(rule.getStatus())) {
                runAmount++;
            }


        }
        statisticWeEvent.setUserAmount(userRuleMap.size());
        statisticWeEvent.setSystemAmount(systemAmount);
        statisticWeEvent.setUserAmount(userAmount);
        statisticWeEvent.setRunAmount(runAmount);
        statisticWeEvent.setStatisticRuleMap(statisticRuleMap);
        return statisticWeEvent;
    }

    private static void statisticOrderType(Pair<String, String> type) {
        if (!("".equals(type.getValue()))) {
            StatisticRule statisticRule = statisticWeEvent.getStatisticRuleMap().get(type.getValue());
            switch (type.getKey()) {
                case ConstantsHelper.HIT_TIMES:
                    statisticRule.setHitTimes(increase(statisticRule.getHitTimes()));
                    break;

                case ConstantsHelper.NOT_HIT_TIMES:
                    statisticRule.setNotHitTimes(increase(statisticRule.getNotHitTimes()));
                    break;

                case ConstantsHelper.WRITE_DB_SUCCESS:
                    statisticRule.setWriteDBSuccess(increase(statisticRule.getWriteDBSuccess()));
                    break;

                case ConstantsHelper.WRITE_DB_FAIL:
                    statisticRule.setWriteDBFail(increase(statisticRule.getWriteDBFail()));
                    break;

                case ConstantsHelper.PUBLISH_EVENT_SUCCESS:
                    statisticRule.setPublishEventSuccess(increase(statisticRule.getPublishEventSuccess()));
                    break;

                case ConstantsHelper.PUBLISH_EVENT_FAIL:
                    statisticRule.setPublishEventFail(increase(statisticRule.getPublishEventFail()));
                    break;

                case ConstantsHelper.LAST_FAIL_REASON:
                    statisticRule.setLastFailReason(statisticRule.getLastFailReason());
                    break;

                default:
                    log.info("other type:{}", type);
                    break;
            }
        }

    }

    private static int increase(int number) {
        return new AtomicInteger(number).getAndIncrement();
    }

    private static class DBThread implements Runnable {

        public void run() {
            while (true) {
                try {
                    // if the quene is null,then the thread sleep 1s
                    long ideaTime = 1000L;
                    Pair<WeEvent, CEPRule> item = systemMessageQueue.poll(ideaTime, TimeUnit.MILLISECONDS);

                    if (null != item) {
                        log.info("auto redo thread enter,system insert db:{}", item.getValue().getId());
                        //  send to  the db
                        sendMessageToDB(item.getValue().getGroupId(), item.getKey(), item.getValue());
                    }
                } catch (InterruptedException e) {
                    log.info(e.toString());
                }
            }
        }
    }
}