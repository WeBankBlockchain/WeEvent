package com.webank.weevent.processor.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.sdk.WeEvent;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class CommonUtil {


    /**
     * check the database url
     *
     * @param databaseUrl data bae url
     * @return connection
     */

    public static Connection getConnection(String databaseUrl) {
        String driver = ProcessorApplication.processorConfig.getDataBaseDriver();
        try {
            Class.forName(driver);
            return DriverManager.getConnection(databaseUrl);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
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
        if ("json".equals(eventMessage.getExtensions().get("weevent-format")) && CommonUtil.checkValidJson(content)) {
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

    private static Map<String, String> generateSqlOrder(String brokerId, String groupId, String eventId, String topicName, List<String> result, JSONObject eventContent, JSONObject table) {
        Map<String, String> sql = new HashMap<>();
        Map<String, String> sqlOrder = new HashMap<>();
        boolean eventIdFlag = false;
        boolean topicNameFlag = false;
        boolean brokerIdFlag = false;
        boolean groupIdFlag = false;

        // get all select field and value, and the select field must in eventContent.
        for (String key : result) {
            sql.put(key, null);
            if (eventContent.containsKey(key)) {
                sql.put(key, eventContent.get(key).toString());
            }
            // set the flag
            if (ConstantsHelper.EVENT_ID.equals(key)) {
                eventIdFlag = true;
            }
            if (ConstantsHelper.TOPIC_NAME.equals(key)) {
                topicNameFlag = true;
            }
            if (ConstantsHelper.BROKER_ID.equals(key)) {
                brokerIdFlag = true;
            }
            if (ConstantsHelper.GROUP_ID.equals(key)) {
                groupIdFlag = true;
            }
        }

        // keep the right order
        for (Map.Entry<String, Object> entry : table.entrySet()) {
            for (String key : result) {
                if (entry.getKey().equals(key)) {
                    sqlOrder.put(entry.getKey(), sql.get(key));
                }
            }
        }

        // if user need eventId, add the event id
        if (eventIdFlag) {
            sqlOrder.put(ConstantsHelper.EVENT_ID, eventId);
        }
        if (topicNameFlag) {
            sqlOrder.put(ConstantsHelper.TOPIC_NAME, topicName);
        }
        if (brokerIdFlag) {
            sqlOrder.put(ConstantsHelper.BROKER_ID, brokerId);
        }
        if (groupIdFlag) {
            sqlOrder.put(ConstantsHelper.GROUP_ID, groupId);
        }
        return sqlOrder;
    }

}



