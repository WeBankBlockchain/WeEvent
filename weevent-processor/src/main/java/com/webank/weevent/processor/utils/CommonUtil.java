package com.webank.weevent.processor.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
            if (checkValidJson(objJson)) {
                for (Map.Entry<String, Object> entry : JSONObject.parseObject(objJson).entrySet()) {
                    keys.add(entry.getKey());
                }
            } else {
                keys = null;
            }

        } catch (JSONException e) {
            keys = null;
            log.info("json get key error");
        }
        return keys;
    }

    /**
     * check valid json string
     *
     * @param test json string
     * @return true or false
     */
    public final static boolean checkValidJson(String test) {
        try {
            JSONObject.parseObject(test);
        } catch (JSONException ex) {
            try {
                JSONObject.parseArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
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

    private static List<String> getSelectFieldList(String selectFields, String payload) {
        List<String> result = new ArrayList<>();
        // if select is equal * ,then select all fields.
        if ("*".equals(selectFields)) {
            String selectFieldsTemp = payload;
            Iterator it = JSONObject.parseObject(selectFieldsTemp).entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                result.add((String) entry.getKey());
            }

        } else {
            String[] array = selectFields.split(",");
            for (String s : array) {
                result.add(s);
            }
        }
        return result;
    }

    public static Map<String, String> contactsql(String brokerId, String groupId, WeEvent eventMessage, String selectFields, String payload) {
        String content = new String(eventMessage.getContent());
        String eventId = eventMessage.getEventId();
        String topicName = eventMessage.getTopic();

        // get select field
        List<String> result = getSelectFieldList(selectFields, payload);
        JSONObject table = JSONObject.parseObject(payload);
        JSONObject eventContent;
        Map<String, String> sqlOrder;
        if (CommonUtil.checkValidJson(content)) {
            eventContent = JSONObject.parseObject(content);
            sqlOrder = generateSqlOrder(brokerId, groupId, eventId, topicName, result, eventContent, table);
        } else {
            sqlOrder = generateSqlOrder(brokerId, groupId, eventId, topicName, result);
        }

        return sqlOrder;
    }

    // for the system tag
    private static Map<String, String> generateSqlOrder(String brokerId, String groupId, String eventId, String topicName, List<String> result) {

        Map<String, String> sqlOrder = new HashMap<>();
        // get all select field and value, and the select field must in eventContent.
        for (String key : result) {
            // set the flag
            if (ConstantsHelper.EVENT_ID.equals(key)) {
                sqlOrder.put(ConstantsHelper.EVENT_ID, eventId);
            }
            if (ConstantsHelper.TOPIC_NAME.equals(key)) {
                sqlOrder.put(ConstantsHelper.TOPIC_NAME, topicName);
            }
            if (ConstantsHelper.BROKER_ID.equals(key)) {
                sqlOrder.put(ConstantsHelper.BROKER_ID, brokerId);
            }
            if (ConstantsHelper.GROUP_ID.equals(key)) {
                sqlOrder.put(ConstantsHelper.GROUP_ID, groupId);
            }
        }

        return sqlOrder;
    }

    public static Map<String, Boolean> setFlag(Map<String, Boolean> map, String key) {
        // set the flag
        if (ConstantsHelper.EVENT_ID.equals(key)) {
            map.put(ConstantsHelper.EVENT_ID, true);
        }
        if (ConstantsHelper.TOPIC_NAME.equals(key)) {
            map.put(ConstantsHelper.TOPIC_NAME, true);
        }
        if (ConstantsHelper.BROKER_ID.equals(key)) {
            map.put(ConstantsHelper.BROKER_ID, true);
        }
        if (ConstantsHelper.GROUP_ID.equals(key)) {
            map.put(ConstantsHelper.GROUP_ID, true);
        }
        if (ConstantsHelper.NOW.equals(key)) {
            map.put(ConstantsHelper.NOW, true);
        }
        if (ConstantsHelper.CURRENT_DATE.equals(key)) {
            map.put(ConstantsHelper.CURRENT_DATE, true);
        }
        if (ConstantsHelper.CURRENT_TIME.equals(key)) {
            map.put(ConstantsHelper.CURRENT_TIME, true);
        }
        return map;
    }

    public static Map<String, String> contactSqlAccordingOrder(Map<String, Boolean> map, Map<String, String> sqlOrder, String brokerId, String groupId, String eventId, String topicName) {
        // set the flag
        for (Map.Entry<String, Boolean> entry : map.entrySet()) {
            // if true,then add it
            if (entry.getValue()) {
                if (ConstantsHelper.EVENT_ID.equals(entry.getKey())) {
                    sqlOrder.put(ConstantsHelper.EVENT_ID, eventId);
                }
                if (ConstantsHelper.TOPIC_NAME.equals(entry.getKey())) {
                    sqlOrder.put(ConstantsHelper.TOPIC_NAME, topicName);
                }
                if (ConstantsHelper.BROKER_ID.equals(entry.getKey())) {
                    sqlOrder.put(ConstantsHelper.BROKER_ID, brokerId);
                }
                if (ConstantsHelper.GROUP_ID.equals(entry.getKey())) {
                    sqlOrder.put(ConstantsHelper.GROUP_ID, groupId);
                }
                if (ConstantsHelper.NOW.equals(entry.getKey())) {
                    sqlOrder.put(ConstantsHelper.NOW, String.valueOf(new Date().getTime()));
                }
                if (ConstantsHelper.CURRENT_TIME.equals(entry.getKey())) {
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    sqlOrder.put(ConstantsHelper.CURRENT_TIME, sdf.format(date));
                }
                if (ConstantsHelper.CURRENT_DATE.equals(entry.getKey())) {
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                    sqlOrder.put(ConstantsHelper.CURRENT_DATE, sdf.format(date));
                }
            }
        }
        return sqlOrder;
    }


    private static Map<String, String> generateSqlOrder(String brokerId, String groupId, String eventId, String topicName, List<String> result, JSONObject eventContent, JSONObject table) {
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
            setFlag(tags, key);
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
     * count the position, for version 1.2 abs��ceil��floor��round
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

    public static String analysisSystemFunction(String[][] systemFunctionMessage, String payload, String conditionField) {
        Map maps = (Map) JSON.parse(payload);
        Map<String, Object> payloadMap = new ConcurrentHashMap<>();
        for (Object map : maps.entrySet()) {
            System.out.println(((Map.Entry) map).getKey() + "     " + ((Map.Entry) map).getValue());
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
            // int endPosition = Math.addExact(Math.addExact(Integer.valueOf(arr[i][0]), arr[i][1].length()), arr[i][3].length()) + 2;
            String[] strArray;
            String left = arr[i][3], middle = "", right = "";

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
                    sb.replace(Integer.valueOf(arr[i][0]), Integer.valueOf(arr[i][1]), String.valueOf(Math.abs((Integer) payload.get(arr[i][3]))));
                    changePosition = changePosition(conditionField, sb.toString());

                    break;

                case "ceil":

                    sb.replace(Integer.valueOf(arr[i][0]) - changePosition, Integer.valueOf(arr[i][1]) - changePosition, String.valueOf(Math.ceil((Integer) payload.get(arr[i][3]))));
                    changePosition = changePosition(conditionField, sb.toString());

                    break;

                case "floor":
                    log.info("sb:{}", sb);
                    //  String test = "10<21 and c>10 or 1111==\"1111\" and floor(b)>10";

                    //new StringBuilder(test).replace(34, 44, "10");
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
            }
        }
        log.info("sb:{}", sb);
        return sb.toString();

    }
}


