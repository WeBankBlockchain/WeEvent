package com.webank.weevent.processor.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        String driver = "com.mysql.jdbc.Driver";
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
      //  String url = strURL.trim().toLowerCase();

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

    public static List<String> getAllKey(Map<String, String> map) {
        List<String> keys = new ArrayList<>();
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            keys.add((String) entry.getKey());
        }
        return keys;
    }

    public static Map<String, String> contactsql(String content, String objJson, String payload) {
        Map<String, String> sql = new HashMap<>();
        Map<String, String> sqlOrder = new HashMap<>();

        if (!StringUtils.isEmpty(content)
                && !StringUtils.isEmpty(objJson)) {
            String[] result = objJson.split(",");
            JSONObject eventContent = JSONObject.parseObject(content);
            JSONObject table = JSONObject.parseObject(payload);

            // get all select field and value
            for (String key : result) {
                sql.put(key, null);
                if (eventContent.containsKey(key)) {
                    sql.put(key, eventContent.get(key).toString());
                }
            }

            // keep the  right order
            for (Map.Entry<String, Object> entry : table.entrySet()) {
                for (String key : result) {
                    if (entry.getKey().equals(key)) {
                        sqlOrder.put(entry.getKey(), sql.get(key));
                    }
                }
            }

        }
        return sqlOrder;
    }

}



