package com.webank.weevent.processor.mq;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.Constants;
import com.webank.weevent.processor.utils.Util;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.util.StringUtils;

import static sun.plugin2.os.windows.OSVERSIONINFOA.size;

@Slf4j
public class CEPRuleMQ {
    // <ruleId <--> subscriptionId>
    public static Map<String, String> subscriptionIdMap = new ConcurrentHashMap<>();

    public static void updateSubscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap) throws BrokerException {
        // unsubscribe old the topic
        ruleMap.get(rule.getId()).getToDestination();
        IWeEventClient client = IWeEventClient.build(rule.getBrokerUrl());
        // update the rule map
        ruleMap.put(rule.getId(), rule);
        // update subscribe
        subscribeMsg(rule, ruleMap);
        client.unSubscribe(subscriptionIdMap.get(rule.getId()));
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
            log.info("id:{},sunid:{}", rule.getId(), subscriptionId);
            client.unSubscribe(subscriptionId);
        } catch (BrokerException e) {
            log.info("BrokerException{}", e.toString());
        }
    }

    private static void sendMessageToDB(String content, CEPRule rule) {
        JSONObject eventContent = JSONObject.parseObject(content);
        JSONObject payloadParams = JSONObject.parseObject(rule.getPayload());
        try {
            Connection conn = Util.getConnection(rule.getDatabaseUrl());

            if (conn != null) {
                Map<String,String> urlParamMap= Util.URLRequest(rule.getDatabaseUrl());
                String insertExpression = "insert into ".concat(urlParamMap.get("tableName").concat("("));
                String values = "values (";
                Map<String, Integer> sqlvalue = contactsql(content, rule.getPayload());
                List<String> result = new ArrayList(sqlvalue.keySet());

                //contact insert into users (first_name, last_name, date_created, is_admin, num_points)
                for (Map.Entry<String, Integer> entry : sqlvalue.entrySet()) {
                    System.out.println(entry.getKey() + ":" + entry.getValue());
                    if(entry.getValue().equals(1)){
                        if(entry.getValue().equals(result.get(result.size()-1))){
                            insertExpression.concat(entry.getKey()).concat(")");
                            values.concat("?）");
                        }else{
                            insertExpression.concat(entry.getKey()).concat(",");
                            values.concat("?，");
                        }
                    }
                }

                String query = insertExpression.concat(values);
                log.info("query:{}",query);
                PreparedStatement preparedStmt = conn.prepareStatement(query);
                for(int t=0;t<result.size();t++){
                    preparedStmt.setString (t+1, eventContent.get(result.get(t)).toString());
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
                if (checkJson(content, entry.getValue().getPayload())) {
                    if (hitRule(content, entry.getValue().getConditionField())) {
                        // select the field and publish the message to the toDestination
                        try {
                            if (entry.getValue().getConditionType().equals(2)) {
                                sendMessageToDB(content, entry.getValue());
                            } else {
                                if (entry.getValue().getConditionType().equals(1)) {
                                    // :TODO select the field
                                    // publish the message
                                    log.info("publish topic {}", entry.getValue().getSelectField());
                                    client.publish(entry.getValue().getToDestination(), content.getBytes());
                                }
                            }
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

    private static Map<String, Integer> contactsql(String content, String objJson) {
        boolean tag = true;
        Map<String, Integer> sql = new HashMap<>();
        if (!StringUtils.isEmpty(content)
                && !StringUtils.isEmpty(objJson)) {
            List<String> objJsonKeys = Util.getKeys(objJson);
            for (String key : objJsonKeys) {
                sql.put(key, 0);
                if (!content.contains(key)) {
                    sql.put(key, 1);
                }
            }
        }
        return sql;
    }

    /**
     * paring where condition ，and get the key and value
     *
     * @param eventContent event message
     * @param condition where condition
     * @return true false
     * @throws JSONException
     */
    private static boolean hitRule(String eventContent, String condition) {

        log.info("parsingCondition eventContent {},condition {}", eventContent, condition);
        String temp = "SELECT * FROM table WHERE ";
        String whereCondition = temp.concat(" ").concat(condition);
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        PlainSelect plainSelect = null;
        boolean flag = false;

        try {
            // :TODO test
            whereCondition = "SELECT * FROM mytable WHERE a = :param OR a = :param2 AND b = :param3";
            Select select = (Select) parserManager.parse(new StringReader(whereCondition));
            plainSelect = (PlainSelect) select.getSelectBody();

            // if contain Between ,like , in and only support one
            List<String> oper = new ArrayList<>(Arrays.asList(Constants.BETWEEN, Constants.LIKE, Constants.IN));
            for (int i = 0; i < oper.size(); i++) {
                if (whereCondition.contains(oper.get(i))) {
                    String operationStr = oper.get(i);
                    flag = singleMatch(eventContent, plainSelect, operationStr);
                    return flag;
                }
            }

            // according to the priority to match rule
            flag = whereConditionOrderPriority(eventContent, plainSelect);

        } catch (Exception e) {
            log.info("exception: {}", e.toString());
        }
        return flag;
    }

    /**
     * according to the priority to match rule
     *
     * @param eventContent event content
     * @param plainSelect condition message
     * @return
     */
    private static boolean whereConditionOrderPriority(String eventContent, PlainSelect plainSelect) {
        boolean flag = false;
        List<Expression> whereList = new ArrayList<>();
        List<String> operatorList = new ArrayList<>();

        Expression exp_l1 = ((BinaryExpression) plainSelect.getWhere()).getLeftExpression();
        Expression exp_r1 = ((BinaryExpression) plainSelect.getWhere()).getRightExpression();
        String exp_middle = ((BinaryExpression) plainSelect.getWhere()).getStringExpression();

        whereList.add(exp_l1);
        operatorList.add(exp_middle);

        Expression expression = exp_r1;
        while (expression.getASTNode() == null) {
            // left
            whereList.add(((BinaryExpression) expression).getLeftExpression());
            // middle
            operatorList.add(((BinaryExpression) expression).getStringExpression());
            // right
            Expression exp_rr = ((BinaryExpression) expression).getRightExpression();

            if (exp_rr.getASTNode().jjtGetNumChildren() > 0) {
                whereList.add(expression);
                log.info("whereList size:{},operatorList size:{}", whereList.size(), operatorList.size());
                break;
            }

        }
        // check the priority ,hit rule at first
        for (int t = 0; t < operatorList.size(); t++) {
            // check AND
            int current = t + 1;
            if (operatorList.get(t).equals(Constants.AND)) {
                boolean flag1 = singleMatch(whereList.get(current), eventContent);
                boolean flag2 = singleMatch(whereList.get(current + 1), eventContent);
                if (flag1 && flag2) {
                    flag = true;
                    return flag;
                }
            }
        }
        // check OR
        for (int t = 0; t < operatorList.size(); t++) {
            // and > or
            int current = t + 1;
            if (operatorList.get(t).equals(Constants.OR)) {
                boolean flag1 = singleMatch(whereList.get(current), eventContent);
                boolean flag2 = singleMatch(whereList.get(current + 1), eventContent);
                if (flag1 || flag2) {
                    flag = true;
                    return flag;
                }
            }
        }
        return flag;
    }

    private static boolean singleMatch(String eventContent, PlainSelect plainSelect, String operationStr) {

        boolean flag = false;
        List<String> contentKeys = Util.getKeys(eventContent);
        try {
            switch (operationStr) {
                case Constants.BETWEEN:
                    flag = Util.compareNumber(plainSelect, contentKeys, eventContent, Constants.BETWEEN);
                    break;

                case Constants.LIKE:
                    flag = Util.compareNumber(plainSelect, contentKeys, eventContent, Constants.LIKE);
                    break;

                case Constants.IN:
                    flag = Util.compareNumber(plainSelect, contentKeys, eventContent, Constants.IN);
                    break;

                default:
                    log.error("other ", operationStr);


            }
        } catch (Exception e) {
            log.info("exception: {}", e.toString());
        }
        return flag;
    }

    private static boolean singleMatch(Expression whereCondition, String eventContent) {

        boolean flag = false;
        List<String> contentKeys = Util.getKeys(eventContent);
        String operationStr = "OTHER";

        // parsing the operation
        List<String> oper = new ArrayList<>(Arrays.asList(Constants.QUALS_TO, Constants.NOT_QUALS_TO, Constants.NOT_QUALS_TO_TWO
                , Constants.MINOR_THAN, Constants.MINOR_THAN_EQUAL, Constants.GREATER_THAN));

        for (int i = 0; i < oper.size(); i++) {
            if (whereCondition.toString().contains(oper.get(i))) {
                log.info("current:{}", oper.get(i));
                operationStr = oper.get(i);
            }
        }
        try {
            switch (operationStr) {
                case Constants.QUALS_TO:
                    flag = Util.compareNumber(whereCondition, contentKeys, eventContent, Constants.QUALS_TO);
                    break;

                case Constants.NOT_QUALS_TO:
                    flag = Util.compareNumber(whereCondition, contentKeys, eventContent, Constants.NOT_QUALS_TO);
                    break;

                case Constants.NOT_QUALS_TO_TWO:
                    flag = Util.compareNumber(whereCondition, contentKeys, eventContent, Constants.NOT_QUALS_TO_TWO);
                    break;

                case Constants.MINOR_THAN:
                    flag = Util.compareNumber(whereCondition, contentKeys, eventContent, Constants.MINOR_THAN);
                    break;

                case Constants.MINOR_THAN_EQUAL:
                    flag = Util.compareNumber(whereCondition, contentKeys, eventContent, Constants.MINOR_THAN_EQUAL);
                    break;

                case Constants.GREATER_THAN:
                    flag = Util.compareNumber(whereCondition, contentKeys, eventContent, Constants.GREATER_THAN);

                    break;

                case Constants.GREATER_THAN_EQUAL:
                    flag = Util.compareNumber(whereCondition, contentKeys, eventContent, Constants.GREATER_THAN_EQUAL);

                    break;
                default:
                    log.error("other ", operationStr);


            }
        } catch (Exception e) {
            log.info("exception: {}", e.toString());
        }
        return flag;
    }

}
