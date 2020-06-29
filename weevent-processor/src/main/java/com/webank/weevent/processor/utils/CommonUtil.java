package com.webank.weevent.processor.utils;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.processor.ProcessorApplication;
import com.webank.weevent.processor.enums.DatabaseTypeEnum;
import com.webank.weevent.processor.model.CEPRule;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.springframework.util.CollectionUtils;
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
    public static Connection getDbcpConnection(String databaseUrl, String databaseType) {
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
                Properties properties = new Properties();
                BasicDataSource ds = BasicDataSourceFactory.createDataSource(properties);
                dsMap.put(databaseUrl, ds);
                if (DatabaseTypeEnum.H2_DATABASE.getCode().equals(databaseType)) {
                    ds.setDriverClassName("org.h2.Driver");
                } else {
                    ds.setDriverClassName("org.mariadb.jdbc.Driver");
                }
                ds.setUrl(urlPage(databaseUrl));
                ds.setUsername(requestUrlMap.get("user"));
                ds.setPassword(requestUrlMap.get("password"));

                ds.setInitialSize(Integer.parseInt(Objects.requireNonNull(ProcessorApplication.environment.getProperty("spring.datasource.dbcp2.initial-size"))));
                ds.setMinIdle(Integer.parseInt(Objects.requireNonNull(ProcessorApplication.environment.getProperty("spring.datasource.dbcp2.min-idle"))));
                ds.setMaxWaitMillis(Integer.parseInt(Objects.requireNonNull(ProcessorApplication.environment.getProperty("spring.datasource.dbcp2.max-wait-millis"))));
                ds.setMaxTotal(Integer.parseInt(Objects.requireNonNull(ProcessorApplication.environment.getProperty("spring.datasource.dbcp2.max-total"))));

                return ds.getConnection();
            }
        } catch (Exception e) {
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
        try {
            Map<String, Object> map = JsonHelper.json2Object(objJson, new TypeReference<Map<String, Object>>() {
            });
            return Arrays.asList(map.keySet().toArray(new String[]{}));
        } catch (Exception e) {
            log.info("json get key error", e);
            return Collections.emptyList();
        }
    }

    public static boolean checkJson(String content, String objJson) {
        boolean tag = true;
        //parsing and match
        if (!StringUtils.isEmpty(content) && !StringUtils.isEmpty(objJson)) {
            List<String> contentKeys = getKeys(content);
            List<String> objJsonKeys = getKeys(objJson);
            if (CollectionUtils.isEmpty(objJsonKeys)) {
                return false;
            }
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

    private static List<String> getSelectFieldList(String selectFields, String payload) throws BrokerException {
        List<String> result = new ArrayList<>();
        // if select is equal * ,then select all fields.
        if ("*".equals(selectFields)) {
            String selectFieldsTemp = payload;
            Iterator it = JsonHelper.json2Object(selectFieldsTemp, Map.class).entrySet().iterator();

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

    public static Map<String, String> contactsql(CEPRule rule, WeEvent eventMessage) throws BrokerException {
        String content = new String(eventMessage.getContent());

        // get select field
        List<String> result = getSelectFieldList(rule.getSelectField(), rule.getPayload());
        Map<String, Object> table = JsonHelper.json2Object(rule.getPayload(), new TypeReference<Map<String, Object>>() {
        });

        Map eventContent;
        Map<String, String> sqlOrder;

        if (JsonHelper.isValid(content)) {
            eventContent = JsonHelper.json2Object(content, Map.class);
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
                log.info("other type:{}", key);
                break;
        }
        return map;
    }


    public static String setWeEventContent(String brokerId, String groupId, WeEvent eventMessage, String
            selectField, String payload) throws BrokerException {
        String content = new String(eventMessage.getContent());
        Map eventContent = JsonHelper.json2Object(content, Map.class);
        Map<String, Object> payloadContent = JsonHelper.json2Object(payload, new TypeReference<Map<String, Object>>() {
        });

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
                    //yyyy/MM/dd HH:mm:ss
                    sqlOrder.put(ConstantsHelper.NOW, String.valueOf(new Date().getTime()));

                    break;
                case ConstantsHelper.CURRENT_TIME:
                    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
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

    public static boolean compareMessage(Pair<CEPRule, CEPRule> rules) {
        if (StringUtils.isEmpty(rules)) {
            return false;
        }
        // check the from destination
        return rules.getKey().getFromDestination().equals(rules.getValue().getFromDestination());
    }

    public static boolean isDate(String strDate) {
        Pattern pattern = Pattern
                .compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
        Matcher m = pattern.matcher(strDate);
        return m.matches() && (strDate.contains("-") && strDate.contains(":"));
    }

    public static boolean isSimpleDate(String strDate) {
        Pattern pattern = Pattern
                .compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$");
        Matcher m = pattern.matcher(strDate);
        return m.matches() && strDate.contains("-");
    }

    public static boolean isTime(String strDate) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        boolean dateFlag = true;
        try {
            format.parse(strDate);
        } catch (ParseException e) {
            dateFlag = false;
        }
        return dateFlag;
    }
}
