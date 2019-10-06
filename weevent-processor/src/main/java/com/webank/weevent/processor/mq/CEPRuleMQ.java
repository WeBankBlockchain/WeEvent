package com.webank.weevent.processor.mq;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.Util;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.json.JSONException;
import org.springframework.util.StringUtils;

@Slf4j
public class CEPRuleMQ {
    // <ruleId <--> subscriptionId>
    public static Map<String, String> subscriptionIdMap = new ConcurrentHashMap<>();

    public static void updateSubscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) throws BrokerException{
        // unsubscribe old the topic
        ruleMap.get(rule.getId()).getToDestination();
        IWeEventClient client = IWeEventClient.build(rule.getBrokerUrl());
        client.unSubscribe(subscriptionIdMap.get(rule.getId()));
        // update the rule map
        ruleMap.put(rule.getId(),rule);
        // update subscribe
        subscribeMsg(rule,ruleMap);
    }

    public static void subscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) {
        try {
            IWeEventClient client = IWeEventClient.build(rule.getBrokerUrl());
            // subscribe topic
            String subscriptionId = client.subscribe(rule.getFromDestination(), WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    try {
                        String content = new String(event.getContent());
                        log.info("on event:{},content:{}", event.toString(), content);
                        if (Util.checkValidJson(content)) {
                            handleOnEvent(event, client, ruleMap);
                        }
                    } catch (JSONException e) {
                        log.error(e.toString());
                    }

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
            log.info("id:{},sunid:{}",rule.getId(),subscriptionId);
            client.unSubscribe(subscriptionId);
        } catch (BrokerException e) {
            log.info("BrokerException{}", e.toString());
        }
    }


    private static void handleOnEvent(WeEvent event, IWeEventClient client, Map<String, CEPRule> ruleMap) throws JSONException {
        log.info("handleOnEvent ruleMapsize :{}", ruleMap.size());
        // get the content ,and parsing it  byte[]-->String
        String content = new String(event.getContent());
        // match the rule and send message
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            if (!StringUtils.isEmpty(entry.getValue().getPayload())
                    && !StringUtils.isEmpty(entry.getValue().getConditionField())) {
                log.info("handleOnEvent isEmpty");
                // parsing the payload && match the content,if true and hit it
                if (checkJson(content, entry.getValue().getPayload())) {
                    log.info("handleOnEvent checkJson");
                    if (matchRule(content, entry.getValue().getConditionField())) {
                        log.info("handleOnEvent matchRule");
                        // select the field and publish the message to the toDestination
                        try {
                            // :TODO select the field
                            log.info(entry.getValue().getSelectField());
                            // publish the message
                            log.info("publish topic {}", entry.getValue().getSelectField());
                            client.publish(entry.getValue().getToDestination(), content.getBytes());
                        } catch (BrokerException e) {
                            log.error(e.toString());
                        }
                    }
                }
            }
        }

    }

    private static boolean checkJson(String content, String objJson) {
        boolean tag = true;
        //parsing and match
        if (!StringUtils.isEmpty(content)
                && !StringUtils.isEmpty(objJson)) {
            List<String> contentKeys = Util.getKeys(content);
            List<String> objJsonKeys = Util.getKeys(objJson);

            // objJsonKeys must longer than the contentKeys
            if (contentKeys.size() > objJsonKeys.size()) {
                tag = false;
            } else {
                for (String contentKey : contentKeys) {
                    if (!objJsonKeys.contains(contentKey)) {
                        tag = false;
                    }
                }
            }
        }
        log.info("checkJson tag:{}", tag);
        return tag;
    }

    /**
     * paring where condition ，and get the key and value
     *
     * @param eventContent event message
     * @param condition where condition
     * @return true false
     * @throws JSONException
     */
    private static boolean parsingCondition(String eventContent, String condition) throws JSONException {
        log.info("parsingCondition eventContent {},condition {}", eventContent, condition);
        boolean flag = false;
        List<String> contentKeys = Util.getKeys(eventContent);
        String temp = "SELECT * FROM table WHERE ";
        String trigger = temp.concat(" ").concat(condition);
        log.info("trigger: {}", trigger);
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        String operationStr = "OTHER";
        PlainSelect plainSelect = null;
        try {
            Select select = (Select) parserManager.parse(new StringReader(trigger));
            plainSelect = (PlainSelect) select.getSelectBody();

            List<String> oper = new ArrayList<>(Arrays.asList("=", "<>", "!=", "<", ">", ">=", "<=", "BETWEEN", "LIKE", "IN"));

            for (int i = 0; i < oper.size(); i++) {
                if (trigger.contains(oper.get(i))) {
                    log.info("current:{}", oper.get(i));
                    operationStr = oper.get(i);
                }
            }
        } catch (Exception e) {
            log.info("exception: {}", e.toString());
        }

        // parsing the operation
        switch (operationStr) {
            case "=":
                log.info("EQUALS_TO:{}", operationStr);
                log.info("left: {},right: {}", (((EqualsTo) plainSelect.getWhere()).getLeftExpression()).toString(), (((EqualsTo) plainSelect.getWhere()).getRightExpression()).toString());
                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, "=");
                break;

            case "<>":
                log.info("NOT_QUALS_TO:{}", operationStr);
                log.info("check:{}", (((NotEqualsTo) plainSelect.getWhere()).getRightExpression()).toString());

                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, "!=");
                break;

            case "!=":
                log.info("NOT_QUALS_TO:{}", operationStr);
                log.info("check:{}", (((NotEqualsTo) plainSelect.getWhere()).getRightExpression()).toString());
                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, "!=");
                break;

            case "<":
                log.info("MINOR_THAN:{}", operationStr);
                log.info("check:{}", (((MinorThan) plainSelect.getWhere()).getRightExpression()).toString());
                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, "<");
                break;

            case "<=":
                log.info("MINOR_THAN_EQUAL:{}", operationStr);
                log.info("check:{}", (((MinorThanEquals) plainSelect.getWhere()).getRightExpression()).toString());
                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, "<=");
                break;

            case ">":
                log.info("GREATER_THAN:{}", operationStr);
                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, ">");

                break;

            case ">=":
                log.info("GREATER_THAN_EQUAL:{}", operationStr);
                log.info("check:{}", (((GreaterThanEquals) plainSelect.getWhere()).getRightExpression()).toString());
                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, ">=");

                break;

            case "BETWEEN":
                log.info("BETWEEN:{}", operationStr);
                log.info("check:start: {},end: {}", ((Between) plainSelect.getWhere()).getBetweenExpressionStart().toString(), ((Between) plainSelect.getWhere()).getBetweenExpressionEnd().toString());
                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, "BETWEEN");
                break;

            case "LIKE":
                log.info("LIKE:{}", operationStr);
                log.info("check:like: ", ((LikeExpression) plainSelect.getWhere()).toString());
                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, "LIKE");
                break;

            case "IN":
                log.info("IN:{}", operationStr);
                log.info("check:IN: ", ((InExpression) plainSelect.getWhere()).toString());
                flag = Util.compareNumber(plainSelect, contentKeys, eventContent, "IN");
                break;

            default:
                log.error("other ", operationStr);


        }
        return flag;
    }

    private static boolean matchRule(String eventContent, String condition) throws JSONException {
        log.info("matchRule eventContent {},condition {}", eventContent, condition);

        boolean hitRule = parsingCondition(eventContent, condition);
        return hitRule;
    }
}
