package com.webank.weevent.processor.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.util.StringUtils;

@Slf4j
public class CommonUtil {
    private static Map<String, BasicDataSource> dsMap = new ConcurrentHashMap<>();

    /**
     * check the database url
     *
     * @param databaseUrl data bae url
     * @return connection
     */
    public static Connection getDbcpConnection(String databaseUrl) {
        try {
            Map<String, String> requestUrlMap = uRLRequest(databaseUrl);
            // check all parameter
            if (!requestUrlMap.containsKey("user") || !requestUrlMap.containsKey("password") || StringUtils.isEmpty(urlPage(databaseUrl))) {
                return null;
            }
            // use the cache
            if (dsMap.containsKey(databaseUrl)) {
                // use the old connection
                return dsMap.get(databaseUrl).getConnection();
            } else {
                BasicDataSource ds = new BasicDataSource();
                dsMap.put(databaseUrl, ds);
                ds.setDriverClassName(ProcessorApplication.environment.getProperty("spring.datasource.driverClassName"));
                ds.setUrl(urlPage(databaseUrl));
                ds.setUsername(requestUrlMap.get("user"));
                ds.setPassword(requestUrlMap.get("password"));

                ds.setInitialSize(Integer.valueOf(ProcessorApplication.environment.getProperty("spring.datasource.dbcp2.initial-size")));
                ds.setMinIdle(Integer.valueOf(ProcessorApplication.environment.getProperty("spring.datasource.dbcp2.min-idle")));
                ds.setMaxWaitMillis(Integer.valueOf(ProcessorApplication.environment.getProperty("spring.datasource.dbcp2.max-wait-millis")));

                return ds.getConnection();
            }
        } catch (SQLException e) {
            log.error("e:{}", e.toString());
            return null;
        }
    }

    public static String urlPage(String url) {
        String page = null;
        String[] arrSplit;

        arrSplit = url.split("[?]");
        if ((url.length()) > 0 && (arrSplit.length >= 1) && (arrSplit[0] != null)) {
            page = arrSplit[0];
        }

        return page;
    }


    private static String truncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = strURL.split("[?]");
        if ((strURL.length() > 1) && (arrSplit.length) > 1 && (arrSplit[1] != null)) {
            strAllParam = arrSplit[1];
        }

        return strAllParam;
    }


    public static Map<String, String> uRLRequest(String URL) {
        Map<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = truncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            if (arrSplitEqual.length > 1) {
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (!arrSplitEqual[0].equals("")) {
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }


    public static List<String> getKeys(String objJson) {
        List<String> keys = new ArrayList<>();
        try {
            Map<String, Object> map = JsonUtil.parseObjectToMap(objJson);
            if (JsonUtil.isValid(objJson)) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    keys.add(entry.getKey());
                }
            } else {
                keys = null;
            }

        } catch (IOException e) {
            keys = null;
            log.info("json get key error");
        }
        return keys;
    }

    public static boolean checkJson(String content, String objJson) {
        boolean tag = true;
        //parsing and match
        if (!StringUtils.isEmpty(content)
                && !StringUtils.isEmpty(objJson)) {
            List<String> contentKeys = getKeys(content);
            List<String> objJsonKeys = getKeys(objJson);

            for (String contentKey : contentKeys) {
                if (!((objJsonKeys.contains(contentKey)) || "eventId".equals(contentKey))) {
                    tag = false;
                    break;
                }
            }
        }
        log.info("checkJson tag:{},content:{},objJson:{}", tag, content, objJson);
        return tag;
    }

    /**
     * get map all key
     *
     * @param map map
     * @return key list
     */
    public static List<String> getAllKey(Map<String, String> map) {
        List<String> keys = new ArrayList<>();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            keys.add((String) entry.getKey());
        }
        return keys;
    }

    private static List<String> getSelectFieldList(String selectFields, String payload) throws IOException {
        List<String> result = new ArrayList<>();
        // if select is equal * ,then select all fields.
        if ("*".equals(selectFields)) {
            String selectFieldsTemp = payload;
            Iterator it = JsonUtil.parseObject(selectFieldsTemp, Map.class).entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                result.add((String) entry.getKey());
            }

        } else {
            String[] array = selectFields.split(",");
            result.addAll(Arrays.asList(array));
        }
        return result;
    }

    public static Map<String, String> contactsql(CEPRule rule, WeEvent eventMessage) throws IOException {
        String content = new String(eventMessage.getContent());

        // get select field
        List<String> result = getSelectFieldList(rule.getSelectField(), rule.getPayload());
        Map<String, Object> table = JsonUtil.parseObjectToMap(rule.getPayload());
        Map eventContent;
        Map<String, String> sqlOrder;

        if (JsonUtil.isValid(content)) {
            eventContent = JsonUtil.parseObject(content, Map.class);
            sqlOrder = generateSqlOrder(rule.getBrokerId(), rule.getGroupId(), eventMessage.getEventId(), eventMessage.getTopic(), result, eventContent, table);
        } else {
            sqlOrder = generateSystemSqlOrder(rule.getBrokerId(), rule.getGroupId(), eventMessage.getEventId(), eventMessage.getTopic(), result);
        }

        return sqlOrder;
    }

    // for the system tag
    private static Map<String, String> generateSystemSqlOrder(String brokerId, String groupId, String eventId, String
            topicName, List<String> result) {

        Map<String, String> sqlOrder = new HashMap<>();
        // get all select field and value, and the select field must in eventContent.
        for (String key : result) {
            // set the flag
            switch (key) {
                case ConstantsHelper.EVENT_ID:
                    sqlOrder.put(ConstantsHelper.EVENT_ID, eventId);
                    break;
                case ConstantsHelper.TOPIC_NAME:
                    sqlOrder.put(ConstantsHelper.TOPIC_NAME, topicName);
                    break;
                case ConstantsHelper.BROKER_ID:
                    sqlOrder.put(ConstantsHelper.BROKER_ID, brokerId);
                    break;
                case ConstantsHelper.GROUP_ID:
                    sqlOrder.put(ConstantsHelper.GROUP_ID, groupId);
                    break;
                default:
                    break;
            }
        }

        return sqlOrder;
    }

    public static LinkedHashMap<String, Boolean> setFlag(LinkedHashMap<String, Boolean> map, String key) {
        // set the flag
        switch (key) {
            case ConstantsHelper.EVENT_ID:
                map.put(ConstantsHelper.EVENT_ID, true);

                break;
            case ConstantsHelper.TOPIC_NAME:
                map.put(ConstantsHelper.TOPIC_NAME, true);

                break;
            case ConstantsHelper.BROKER_ID:
                map.put(ConstantsHelper.BROKER_ID, true);

                break;
            case ConstantsHelper.GROUP_ID:
                map.put(ConstantsHelper.GROUP_ID, true);

                break;
            case ConstantsHelper.NOW:
                map.put(ConstantsHelper.NOW, true);

                break;
            case ConstantsHelper.CURRENT_TIME:
                map.put(ConstantsHelper.CURRENT_TIME, true);

                break;
            case ConstantsHelper.CURRENT_DATE:
                map.put(ConstantsHelper.CURRENT_DATE, true);

                break;
            default:
                log.info("error:{}", key);
                break;
        }
        return map;
    }


    public static String setWeEventContent(String brokerId, String groupId, WeEvent eventMessage, String
            selectField, String payload) throws IOException {
        String content = new String(eventMessage.getContent());
        Map eventContent = JsonUtil.parseObject(content, Map.class);
        Map<String, Object> payloadContent = JsonUtil.parseObjectToMap(payload);

        // match the table
        Map<String, Object> iftttContent = new HashMap<>();
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
        for (String item : result) {
            if (eventContent.containsKey(item)) {
                iftttContent.put(item, eventContent.get(item));
            }
            switch (item) {
                case ConstantsHelper.EVENT_ID:
                    iftttContent.put(item, eventMessage.getEventId());
                    break;

                case ConstantsHelper.TOPIC_NAME:
                    iftttContent.put(item, eventMessage.getTopic());
                    break;

                case ConstantsHelper.BROKER_ID:
                    iftttContent.put(item, brokerId);
                    break;

                case ConstantsHelper.GROUP_ID:
                    iftttContent.put(item, groupId);
                    break;

                default:
                    log.info("item:{}", item);
                    break;
            }
        }

        return iftttContent.toString();
    }


    public static Map<String, String> contactSqlAccordingOrder
            (Map<String, Boolean> map, Map<String, String> sqlOrder, String brokerId, String groupId, String
                    eventId, String topicName) {
        // set the flag
        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            // if true,then add it
            switch (entry.getKey()) {
                case ConstantsHelper.EVENT_ID:
                    sqlOrder.put(ConstantsHelper.EVENT_ID, eventId);

                    break;
                case ConstantsHelper.TOPIC_NAME:
                    sqlOrder.put(ConstantsHelper.TOPIC_NAME, topicName);

                    break;
                case ConstantsHelper.BROKER_ID:
                    sqlOrder.put(ConstantsHelper.BROKER_ID, brokerId);

                    break;
                case ConstantsHelper.GROUP_ID:
                    sqlOrder.put(ConstantsHelper.GROUP_ID, groupId);

                    break;
                case ConstantsHelper.NOW:
                    sqlOrder.put(ConstantsHelper.NOW, String.valueOf(new Date().getTime()));

                    break;
                case ConstantsHelper.CURRENT_TIME:
                    SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    sqlOrder.put(ConstantsHelper.CURRENT_TIME, sdfTime.format(new Date()));

                    break;
                case ConstantsHelper.CURRENT_DATE:
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd");
                    sqlOrder.put(ConstantsHelper.CURRENT_DATE, sdfDate.format(new Date()));

                    break;
                default:
                    log.info("error:{}", entry.getKey());
                    break;
            }
        }
        return sqlOrder;
    }

    private static Map<String, String> generateSqlOrder(String brokerId, String groupId, String eventId, String
            topicName, List<String> result, Map eventContent, Map<String, Object> table) {
        Map<String, String> sql = new HashMap<>();
        Map<String, String> sqlOrder = new HashMap<>();
        // <key-->flag>
        LinkedHashMap<String, Boolean> tags = new LinkedHashMap<>();

        // get all select field and value, and the select field must in eventContent, except the system parameter.
        for (String key : result) {
            sql.put(key, null);
            if (eventContent.containsKey(key)) {
                sql.put(key, eventContent.get(key).toString());
            }
            tags = setFlag(tags, key);
        }

        // keep the right order
        for (Map.Entry<String, Object> entry : table.entrySet()) {
            for (String key : result) {
                if (entry.getKey().equals(key)) {
                    sqlOrder.put(entry.getKey(), sql.get(key));
                }
            }
        }

        // add the system parameter
        sqlOrder = contactSqlAccordingOrder(tags, sqlOrder, brokerId, groupId, eventId, topicName);

        return sqlOrder;
    }

    public static boolean compareMessage(CEPRule rule, List<CEPRule> ruleList) {
        for (int i = 0; i < ruleList.size(); i++) {
            if (ruleList.get(i).getId().equals(rule.getId())) {
                return ruleList.get(i).getFromDestination().equals(rule.getFromDestination());
            }
        }
        return false;
    }


    /**
     * count the position, for version 1.2 abs and ceil and floor and round
     *
     * @param conditionField original condition message
     * @param sb condition buffer
     * @return amount
     */
    private static int changePosition(String conditionField, String sb) {
        int changePosition = 0;
        if (conditionField.length() > sb.length()) {
            changePosition = conditionField.length() - sb.length();
        }
        return changePosition;
    }

    public static String[][] stringConvertArray(String s) {
        String[] s1 = s.replaceAll("],", "]#").split("#");
        String[][] arr = new String[s1.length][];
        for (int i = 0; i < arr.length; i++) {
            String[] s2 = s1[i].split(",");
            arr[i] = new String[s2.length];
            for (int j = 0; j < s2.length; j++) {
                arr[i][j] = s2[j].replaceAll("\\[|\\]", "").replace("\"", "").trim();
            }
        }
        return arr;
    }

    public static String analysisSystemFunction(String[][] systemFunctionMessage, String payload, String
            conditionField) throws IOException {

        Map maps = JsonUtil.parseObjectToMap(payload);
        Map<String, Object> payloadMap = new ConcurrentHashMap<>();
        for (Object map : maps.entrySet()) {
            payloadMap.put((String) ((Map.Entry) map).getKey(), ((Map.Entry) map).getValue());
        }
        return replaceCondition(systemFunctionMessage, conditionField, payloadMap);
    }


    /**
     * @param arr enhance function message
     * @param conditionField original condition details
     * @param payload payload
     * @return condition
     */
    public static String replaceCondition(String[][] arr, String conditionField, Map payload) {
        StringBuilder sb = new StringBuilder(conditionField);
        int changePosition = 0;

        for (int i = 0; i < arr.length; i++) {
            String type = arr[i][2];
            // end position
            String left = arr[i][3];
            String middle = "";
            String right = "";

            if (arr[i].length == 5) {
                left = arr[i][3];
                right = arr[i][4];
            } else if (arr[i].length == 6) { // multi parameter
                left = arr[i][3];
                middle = arr[i][4];
                right = arr[i][5];
            }

            String replaceContent = "";
            switch (type) {
                case "abs":
                    sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, String.valueOf(Math.abs((Integer) payload.get(arr[i][3]))));
                    changePosition = changePosition(conditionField, sb.toString());

                    break;

                case "ceil":

                    sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, String.valueOf(Math.ceil((Integer) payload.get(arr[i][3]))));
                    changePosition = changePosition(conditionField, sb.toString());

                    break;

                case "floor":
                    log.info("sb:{}", sb);

                    sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, String.valueOf(Math.floor((Integer) payload.get(arr[i][3]))));
                    changePosition = changePosition(conditionField, sb.toString());

                    break;

                case "round":

                    sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, String.valueOf(Math.round(Math.round((Integer) payload.get(arr[i][3])))));
                    changePosition = changePosition(conditionField, sb.toString());

                    break;
                case "substring":
                    if (!"".equals(middle)) {
                        replaceContent = "\"" + payload.get(left).toString().substring(Integer.valueOf(middle), Integer.valueOf(right)) + "\"";
                        sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, replaceContent);
                    } else {
                        replaceContent = "\"" + payload.get(left).toString().substring(Integer.valueOf(right)) + "\"";
                        sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, replaceContent);
                    }

                    changePosition = changePosition(conditionField, sb.toString());

                    break;
                case "concat":
                    replaceContent = "\"" + payload.get(left).toString().concat(payload.get(right).toString()) + "\"";
                    sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, replaceContent);
                    changePosition = changePosition(conditionField, sb.toString());

                    break;
                case "trim":
                    replaceContent = "\"" + payload.get(left).toString().trim() + "\"";
                    sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, replaceContent);
                    changePosition = changePosition(conditionField, sb.toString());

                    break;
                case "lcase":
                    replaceContent = "\"" + payload.get(left).toString().toLowerCase() + "\"";
                    sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, replaceContent);
                    changePosition = changePosition(conditionField, sb.toString());

                    break;
                default:
                    log.info("conditionField:{}", conditionField);
                    break;
            }
        }
        log.info("sb:{}", sb);
        return sb.toString();

    }
}


