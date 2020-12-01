package com.webank.weevent.processor.mq;

import java.nio.charset.StandardCharsets;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.IWeEventClient;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.processor.enums.ConditionTypeEnum;
import com.webank.weevent.processor.enums.RuleStatusEnum;
import com.webank.weevent.processor.enums.SystemTagEnum;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.model.StatisticRule;
import com.webank.weevent.processor.model.StatisticWeEvent;
import com.webank.weevent.processor.quartz.QuartzManager;
import com.webank.weevent.processor.utils.CommonUtil;
import com.webank.weevent.processor.utils.ConstantsHelper;
import com.webank.weevent.processor.utils.DataBaseUtil;
import com.webank.weevent.processor.utils.RetCode;
import com.webank.weevent.processor.utils.SaveTopicDataUtil;
import com.webank.weevent.processor.utils.StatisticCEPRuleUtil;
import com.webank.weevent.processor.utils.SystemFunctionUtil;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.quartz.SchedulerException;
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

    private static DBThread dbThread = new DBThread();

    // statistic weevent
    public static StatisticWeEvent statisticWeEvent = new StatisticWeEvent();

    @PostConstruct
    public void init() throws BrokerException {
        // get all rule
        log.info("start dBThread ...");
        new Thread(dbThread).start();
    }

    public static void updateSubscribeMsg(CEPRule rule, Pair<CEPRule, CEPRule> ruleBak) throws BrokerException, SchedulerException {
        // when is in run status. update the rule map
        // update unsubscribe
        String subId = subscriptionIdMap.get(rule.getId());

        Map<String, CEPRule> ruleMap = QuartzManager.getJobList();
        statisticWeEvent = StatisticCEPRuleUtil.statistic(statisticWeEvent, ruleMap);

        if (RuleStatusEnum.RUNNING.getCode().equals(rule.getStatus())) {
            if (null != subId) {
                IWeEventClient client = subscriptionClientMap.get(subId);
                // check the FromDestination whether is or not,ruleList have all message and ruleMap has latest message
                if (!CommonUtil.compareMessage(ruleBak)) {
                    boolean flag = client.unSubscribe(subId);
                    log.info("start old rule ,and subscribe subId:{}。start rule ,and subscribe flag:{}", subId, flag);
                    if (flag) {
                        subscribeMsg(rule, ruleMap, client, subId);
                    }
                }

            } else {
                ruleMap.put(rule.getId(), rule);
                // update subscribe
                subscribeMsg(rule, ruleMap, null, null);
                log.info("start rule ,and subscribe rule:{}", rule.getId());
            }
        }
        if (RuleStatusEnum.NOT_STARTED.getCode().equals(rule.getStatus()) || RuleStatusEnum.IS_DELETED.getCode().equals(rule.getStatus())) {
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
            IWeEventClient.Builder builder = IWeEventClient.builder();
            builder.brokerUrl(baseUrl);
            if (null != groupId) {
                builder.groupId(groupId);
                client = builder.build();
                clientGroupMap.put(client, new Pair<>(baseUrl, groupId));
            } else {
                client = builder.build();
                clientGroupMap.put(client, new Pair<>(baseUrl, ""));
            }
            return client;
        } catch (BrokerException e) {
            log.info("BrokerException{}", e.toString());
            return null;
        }

    }

    private static void subscribeMsg(CEPRule rule, Map<String, CEPRule> ruleMap, IWeEventClient clientOld, String subId) {
        try {
            IWeEventClient client;
            Map<String, String> ext = new HashMap<>();

            if (null == clientOld) {
                client = getClient(rule);
            } else {
                client = clientOld;
            }

            // subscribe topic
            String subscriptionId;
            String offSet;
            ExtendEventLister eventLister = new ExtendEventLister(client, ruleMap, statisticWeEvent);
            if (!StringUtils.isEmpty(subId)) {
                log.info("update use old subId:{}", subId);
                ext.put(WeEvent.WeEvent_SubscriptionId, subId);

                // if empty,get the new
                offSet = StringUtils.isEmpty(rule.getOffSet()) ? WeEvent.OFFSET_LAST : rule.getOffSet();
                subscriptionId = client.subscribe(rule.getFromDestination(), offSet, ext, eventLister);
            } else {
                // if empty,get the new
                offSet = StringUtils.isEmpty(rule.getOffSet()) ? WeEvent.OFFSET_LAST : rule.getOffSet();
                subscriptionId = client.subscribe(rule.getFromDestination(), offSet, ext, eventLister);
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

    public static Pair<String, String> handleOnEventOtherPattern(IWeEventClient client, WeEvent event, Map<String, CEPRule> ruleMap) {
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
            if (SystemTagEnum.BUILT_IN_SYSTEM.getCode().equals(entry.getValue().getSystemTag()) && entry.getValue().getFromDestination().equals("#") && entry.getValue().getConditionType().equals(ConditionTypeEnum.DATABASE.getCode())) {

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

        log.debug("client:{}group:{},client:brokerUrl:{},rule:brokerUr{}", subscriptionClientMap.get(subscriptionIdMap.get(entry.getValue().getId())).equals(client), clientGroupMap.get(client).getValue().equals(entry.getValue().getGroupId()), clientGroupMap.get(client).getKey(), CommonUtil.urlPage(entry.getValue().getBrokerUrl()));
        return (!(subscriptionClientMap.get(subscriptionIdMap.get(entry.getValue().getId())).equals(client) && clientGroupMap.get(client).getValue().equals(entry.getValue().getGroupId()) && clientGroupMap.get(client).getKey().equals(CommonUtil.urlPage(entry.getValue().getBrokerUrl()))));

    }

    public static Pair<String, String> handleOnEvent(IWeEventClient client, WeEvent event, Map<String, CEPRule> ruleMap) {
        log.info("handleOnEvent ruleMapsize :{}", ruleMap.size());
        // match the rule and send message
        for (Map.Entry<String, CEPRule> entry : ruleMap.entrySet()) {
            StatisticRule rule = statisticWeEvent.getStatisticRuleMap().get(entry.getValue().getId());

            // check the parameter
            if (checkTheInput(entry, client)) {
                continue;
            }

            // write the # topic to history db  or ifttt message
            if (SystemTagEnum.BUILT_IN_SYSTEM.getCode().equals(entry.getValue().getSystemTag()) && entry.getValue().getFromDestination().equals("#") && ConditionTypeEnum.DATABASE.getCode().equals(entry.getValue().getConditionType())) {
                log.info("system insert db:{}", entry.getValue().getId());
                Pair<WeEvent, CEPRule> messagePair = new Pair<>(event, entry.getValue());
                systemMessageQueue.add(messagePair);

                return new Pair<>(ConstantsHelper.HIT_TIMES, entry.getValue().getId());

            } else {

                if (StringUtils.isEmpty(entry.getValue().getSelectField()) || (StringUtils.isEmpty(entry.getValue().getPayload()))) {
                    continue;
                }
                try {
                    // hit the rule engine
                    if (hitRuleEngine(entry.getValue(), event)) {
                        // update the  statistic weevent
                        rule.setHitTimes(rule.getHitTimes() + 1);
                        // get the system parameter
                        String groupId = entry.getValue().getGroupId();

                        // parsing the payload && match the content,if true and hit it
                        if (ConditionTypeEnum.DATABASE.getCode().equals(entry.getValue().getConditionType())) {

                            log.info("entry: {},event hit the db and insert: {}", entry.getValue().toString(), event.toString());

                            // send to database
                            String ret;
                            if (SystemTagEnum.BUILT_IN_SYSTEM.getCode().equals(entry.getValue().getSystemTag()) && entry.getValue().getFromDestination().equals("#")) {
                                ret = SaveTopicDataUtil.saveTopicData(event, entry.getValue());
                            } else {
                                ret = DataBaseUtil.sendMessageToDB(event, entry.getValue());
                            }
                            return new Pair<>(ret, entry.getValue().getId());
                        } else if (ConditionTypeEnum.TOPIC.getCode().equals(entry.getValue().getConditionType())) {

                            // select the field and publish the message to the toDestination
                            String eventContent = CommonUtil.setWeEventContent(entry.getValue().getBrokerId(), groupId, event, entry.getValue().getSelectField(), entry.getValue().getPayload());

                            // publish the message
                            WeEvent weEvent = new WeEvent(entry.getValue().getToDestination(), eventContent.getBytes(StandardCharsets.UTF_8), event.getExtensions());
                            log.info("after hitRuleEngine weEvent  groupId: {}, event:{}", groupId, weEvent.toString());
                            IWeEventClient toDestinationClient = getClient(entry.getValue());
                            SendResult result = toDestinationClient.publish(weEvent);

                            // update the  statistic weevent
                            if (SendResult.SendResultStatus.SUCCESS.equals(result.getStatus())) {
                                return new Pair<>(ConstantsHelper.PUBLISH_EVENT_SUCCESS, entry.getValue().getId());
                            } else {
                                return new Pair<>(ConstantsHelper.PUBLISH_EVENT_FAIL, entry.getValue().getId());
                            }
                        }

                    } else {
                        return new Pair<>(ConstantsHelper.NOT_HIT_TIMES, entry.getValue().getId());
                    }
                } catch (BrokerException e) {
                    log.error(e.toString());
                    return new Pair<>(ConstantsHelper.LAST_FAIL_REASON, entry.getValue().getId());
                }
            }
        }
        return new Pair<>(ConstantsHelper.OTHER, "");
    }

    private static boolean handleTheEqual(WeEvent eventMessage, String condition) throws BrokerException {
        String eventContent = new String(eventMessage.getContent());
        Map event = JsonHelper.json2Object(eventContent, Map.class);
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

    private static boolean hitRuleEngine(CEPRule rule, WeEvent eventMessage) throws BrokerException {
        // String payload, WeEvent eventMessage, String condition
        String payload = rule.getPayload();
        String condition = rule.getConditionField();
        try {
            String eventContent = new String(eventMessage.getContent());
            // all parameter must be the same
            if (CommonUtil.checkJson(eventContent, payload) && (StringUtils.isEmpty(condition))) {
                // if the condition is empty, just return all message
                return true;
            } else if (CommonUtil.checkJson(eventContent, payload)) {
                List<String> eventContentKeys = CommonUtil.getKeys(payload);
                Map event = JsonHelper.json2Object(eventContent, Map.class);
                JexlEngine jexl = new JexlBuilder().create();
                JexlContext context = setContext(event, eventContentKeys);

                // check the expression ,if match then true
                log.info("condition:{},systemFunctionMessage：{}", condition, rule.getFunctionArray());
                if (!StringUtils.isEmpty(rule.getFunctionArray())) {
                    String[][] systemFunctionDetail = SystemFunctionUtil.stringConvertArray(rule.getFunctionArray());
                    if (0 != systemFunctionDetail.length && 0 != systemFunctionDetail[0].length && 0 != systemFunctionDetail[0][0].length()) {
                        condition = SystemFunctionUtil.analysisSystemFunction(systemFunctionDetail, eventContent, condition);
                        log.info("condition:{}", condition);
                    }
                }

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

    public static JexlContext setContext(Map payloadJson, List<String> payloadContentKeys) {
        JexlContext context = new MapContext();
        for (String key : payloadContentKeys) {
            if (CommonUtil.isDate(String.valueOf(payloadJson.get(key)))) {
                String timeStr = String.valueOf(payloadJson.get(key));
                long time = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(timeStr, new ParsePosition(0)).getTime() / 1000;
                context.set(key, time);
            } else if (CommonUtil.isSimpleDate(String.valueOf(payloadJson.get(key)))) {
                String newString = payloadJson.get(key).toString().replace("-", "");
                context.set(key, Integer.valueOf(newString));
            } else if (CommonUtil.isTime(String.valueOf(payloadJson.get(key)))) {
                String newString = payloadJson.get(key).toString().replace(":", "");
                context.set(key, Integer.valueOf(newString));
            } else {
                context.set(key, payloadJson.get(key));
            }
        }
        return context;
    }

    /**
     * check the condition field
     *
     * @param payload payload message
     * @param condition condition details
     * @return result code
     */
    public static RetCode checkCondition(String payload, String condition) {
        try {
            List<String> payloadContentKeys = CommonUtil.getKeys(payload);
            Map payloadJson = JsonHelper.json2Object(payload, Map.class);
            JexlEngine jexl = new JexlBuilder().create();

            JexlContext context = setContext(payloadJson, payloadContentKeys);
            Map event = JsonHelper.json2Object(payload, Map.class);
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
                log.info("result:{}", e);
                return ConstantsHelper.SUCCESS;

            }
        } catch (Exception e) {
            log.info("error number");
            return ConstantsHelper.FAIL;
        }
        return ConstantsHelper.FAIL;
    }

    public static StatisticWeEvent getStatisticWeEvent() {
        log.info("getStatisticWeEvent:{}", statisticWeEvent);
        return statisticWeEvent;
    }

    private static class DBThread implements Runnable {
        private volatile boolean flag = false;

        public void run() {
            while (!flag) {
                try {
                    // if the quene is null,then the thread sleep 1s
                    long ideaTime = 1000L;
                    Pair<WeEvent, CEPRule> item = systemMessageQueue.poll(ideaTime, TimeUnit.MILLISECONDS);

                    if (null != item) {
                        log.info("auto redo thread enter,system insert db:{}", item.getValue().getId());
                        //  send to  the db
                        if (SystemTagEnum.BUILT_IN_SYSTEM.getCode().equals(item.getValue().getSystemTag()) && item.getValue().getFromDestination().equals("#")) {
                            SaveTopicDataUtil.saveTopicData(item.getKey(), item.getValue());
                        } else {
                            DataBaseUtil.sendMessageToDB(item.getKey(), item.getValue());
                        }
                    }
                } catch (InterruptedException e) {
                    log.info("insert  fail,{}", e.toString());
                    Thread.currentThread().interrupt();
                    flag = true;
                }
            }
        }
    }

}
