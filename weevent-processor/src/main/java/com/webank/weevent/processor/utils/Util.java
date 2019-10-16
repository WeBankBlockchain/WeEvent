package com.webank.weevent.processor.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class Util {


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

        url = url.trim().toLowerCase();

        arrSplit = url.split("[?]");
        if (url.length() > 0) {
            if (arrSplit.length > 1) {
                if (arrSplit[0] != null) {
                    page = arrSplit[0];
                }
            }
        }

        return page;
    }


    private static String truncateUrlPage(String strURL) {
        String strAllParam = null;
        String url = strURL.trim().toLowerCase();

        String[] arrSplit = url.split("[?]");
        if ((url.length() > 1) && (arrSplit.length) > 1 && (arrSplit[1] != null)) {
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

    public static Map<String, Integer> contactsql(String content, String objJson) {
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


    public static String byte2hex(byte[] buffer) {
        String h = "0x";

        for (byte aBuffer : buffer) {
            String temp = Integer.toHexString(aBuffer & 0xFF);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            h = h + " " + temp;
        }

        return h;

    }


    public static String recurseKeys(JSONObject jObj, String findKey) {
        String finalValue = "";
        if (jObj == null) {
            return "";
        }
        finalValue = jObj.get(findKey).toString();
        // key is not found
        return finalValue;
    }
}



