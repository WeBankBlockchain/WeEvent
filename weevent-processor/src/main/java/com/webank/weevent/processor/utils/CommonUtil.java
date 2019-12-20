package com.webank.weevent.processor.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.ProcessorApplication;
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

    public static Map<String, String> contactsql(String brokerId, String groupId, WeEvent eventMessage, String selectFields, String payload) throws IOException {
        String content = new String(eventMessage.getContent());
        String eventId = eventMessage.getEventId();
        String topicName = eventMessage.getTopic();

        // get select field
        List<String> result = getSelectFieldList(selectFields, payload);
        Map<String, Object> table = JsonUtil.parseObjectToMap(payload);
        Map eventContent;
        Map<String, String> sqlOrder;
        if (JsonUtil.isValid(content)) {
            eventContent = JsonUtil.parseObject(content, Map.class);
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

    private static Map<String, String> generateSqlOrder(String brokerId, String groupId, String eventId, String topicName, List<String> result, Map eventContent, Map<String, Object> table) {
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
            switch (key) {
                case ConstantsHelper.EVENT_ID:
                    eventIdFlag = true;
                    break;
                case ConstantsHelper.TOPIC_NAME:
                    topicNameFlag = true;
                    break;
                case ConstantsHelper.BROKER_ID:
                    brokerIdFlag = true;
                    break;
                case ConstantsHelper.GROUP_ID:
                    groupIdFlag = true;
                    break;
                default:
                    break;

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



