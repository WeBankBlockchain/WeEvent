package com.webank.weevent.processor.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@Slf4j
public class Util {


    /**
     * check the database url
     *
     * @param databaseUrl
     * @return
     */

    public static Connection getConnection(String databaseUrl) {
        String driver = "com.mysql.jdbc.Driver";
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(databaseUrl);
            return conn;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getKeys(String objJson) {
        List<String> keys = new ArrayList<>();
        try {
            Iterator<String> sIterator = (new org.json.JSONObject(objJson)).keys();

            while (sIterator.hasNext()) {
                String key = (sIterator.next());
                keys.add(key);
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return keys;
    }

    public static boolean checkValidJson(String jsonStr) {
        Object object = null;
        try {
            object = new JSONTokener(jsonStr).nextValue();
            return true;
        } catch (JSONException e) {
            log.info(e.toString());
            return false;
        }
    }

    public static boolean compareNumber(PlainSelect plainSelect, List<String> contentKeys, String eventContent, String operation) throws JSONException {
        boolean flag = false;
        String leftKey;
        int RightKey;

        for (int i = 0; i < contentKeys.size(); i++) {
            switch (operation) {
                case "=":
                    leftKey = (((EqualsTo) plainSelect.getWhere()).getLeftExpression()).toString();
                    RightKey = Integer.valueOf((((EqualsTo) plainSelect.getWhere()).getRightExpression()).toString());
                    if (contentKeys.get(i).equals(leftKey)) {
                        // compare the value
                        JSONObject jObj = new JSONObject(eventContent);
                        String extract = Util.recurseKeys(jObj, contentKeys.get(i));
                        log.info("extract:{},operation:{},leftKey:{}", extract, operation, leftKey);
                        if (Integer.valueOf(extract) == RightKey) {
                            log.info("hit it....");
                            flag = true;
                            break;
                        }
                    }

                    break;
                case "!=":

                    leftKey = (((NotEqualsTo) plainSelect.getWhere()).getLeftExpression()).toString();
                    RightKey = Integer.valueOf((((NotEqualsTo) plainSelect.getWhere()).getRightExpression()).toString());
                    if (contentKeys.get(i).equals(leftKey)) {
                        // compare the value
                        JSONObject jObj = new JSONObject(eventContent);
                        String extract = Util.recurseKeys(jObj, contentKeys.get(i));
                        log.info("extract:{},operation:{},leftKey:{}", extract, operation, leftKey);
                        if (Integer.valueOf(extract) != RightKey) {
                            log.info("hit it....");
                            flag = true;
                            break;
                        }
                    }

                    break;
                case "<>":

                    leftKey = (((NotEqualsTo) plainSelect.getWhere()).getLeftExpression()).toString();
                    RightKey = Integer.valueOf((((NotEqualsTo) plainSelect.getWhere()).getRightExpression()).toString());
                    if (contentKeys.get(i).equals(leftKey)) {
                        // compare the value
                        JSONObject jObj = new JSONObject(eventContent);
                        String extract = Util.recurseKeys(jObj, contentKeys.get(i));
                        log.info("extract:{},operation:{},leftKey:{}", extract, operation, leftKey);
                        if (Integer.valueOf(extract) != RightKey) {
                            log.info("hit it....");
                            flag = true;
                            break;

                        }
                    }

                    break;
                case "<":
                    leftKey = (((MinorThan) plainSelect.getWhere()).getLeftExpression()).toString();
                    RightKey = Integer.valueOf((((MinorThan) plainSelect.getWhere()).getRightExpression()).toString());
                    if (contentKeys.get(i).equals(leftKey)) {
                        // compare the value
                        JSONObject jObj = new JSONObject(eventContent);
                        String extract = Util.recurseKeys(jObj, contentKeys.get(i));
                        log.info("extract:{},operation:{},leftKey:{}", extract, operation, leftKey);
                        if (Integer.valueOf(extract) < RightKey) {
                            log.info("hit it....");
                            flag = true;
                            break;
                        }
                    }
                    break;
                case "<=":
                    leftKey = (((MinorThanEquals) plainSelect.getWhere()).getLeftExpression()).toString();
                    RightKey = Integer.valueOf((((MinorThanEquals) plainSelect.getWhere()).getRightExpression()).toString());
                    if (contentKeys.get(i).equals(leftKey)) {
                        // compare the value
                        JSONObject jObj = new JSONObject(eventContent);
                        String extract = Util.recurseKeys(jObj, contentKeys.get(i));
                        log.info("extract:{},operation:{},leftKey:{}", extract, operation, leftKey);
                        if (Integer.valueOf(extract) <= RightKey) {
                            log.info("hit it....");
                            flag = true;
                            break;
                        }
                    }

                    break;
                case ">":
                    leftKey = (((GreaterThan) plainSelect.getWhere()).getLeftExpression()).toString();
                    RightKey = Integer.valueOf((((GreaterThan) plainSelect.getWhere()).getRightExpression()).toString());
                    if (contentKeys.get(i).equals(leftKey)) {
                        // compare the value
                        JSONObject jObj = new JSONObject(eventContent);
                        String extract = Util.recurseKeys(jObj, contentKeys.get(i));
                        log.info("extract:{},operation:{},leftKey:{}", extract, operation, leftKey);
                        if (Integer.valueOf(extract) > RightKey) {
                            log.info("hit it....");
                            flag = true;
                            break;
                        }
                    }
                    break;
                case ">=":
                    leftKey = (((GreaterThanEquals) plainSelect.getWhere()).getLeftExpression()).toString();
                    RightKey = Integer.valueOf((((GreaterThanEquals) plainSelect.getWhere()).getRightExpression()).toString());
                    if (contentKeys.get(i).equals(leftKey)) {
                        // compare the value
                        JSONObject jObj = new JSONObject(eventContent);
                        String extract = Util.recurseKeys(jObj, contentKeys.get(i));
                        log.info("extract:{},operation:{},leftKey:{}", extract, operation, leftKey);
                        if (Integer.valueOf(extract) >= RightKey) {
                            log.info("hit it....");
                            flag = true;
                            break;
                        }
                    }

                    break;
                case "BETWEEN":
                    if (operation.equals("BETWEEN")) {
                        log.info("check:start: {},end: {}", ((Between) plainSelect.getWhere()).getBetweenExpressionStart().toString(), ((Between) plainSelect.getWhere()).getBetweenExpressionEnd().toString());
                        int leftValue = Integer.valueOf(((Between) plainSelect.getWhere()).getBetweenExpressionStart().toString());
                        int rightValue = Integer.valueOf(((Between) plainSelect.getWhere()).getBetweenExpressionEnd().toString());
                        leftKey = ((Between) plainSelect.getWhere()).getLeftExpression().toString();
                        if (contentKeys.get(i).equals(leftKey)) {
                            // compare the value
                            JSONObject jObj = new JSONObject(eventContent);
                            String extract = Util.recurseKeys(jObj, contentKeys.get(i));
                            log.info("extract:{},operation:{},leftKey:{}", extract, operation, leftKey);
                            if (Integer.valueOf(extract) > leftValue && Integer.valueOf(extract) < rightValue) {
                                log.info("hit it....");
                                flag = true;
                                break;
                            }
                        }
                    }
                    break;
                default:
                    log.error("default ");

            }
        }
        return flag;
    }

    public static String recurseKeys(JSONObject jObj, String findKey) throws JSONException {
        String finalValue = "";
        if (jObj == null) {
            return "";
        }

        Iterator<String> keyItr = jObj.keys();
        Map<String, String> map = new HashMap<>();

        while (keyItr.hasNext()) {
            String key = keyItr.next();
            map.put(key, jObj.getString(key));
        }

        for (Map.Entry<String, String> e : (map).entrySet()) {
            String key = e.getKey();
            if (key.equalsIgnoreCase(findKey)) {
                return jObj.getString(key);
            }

            // read value
            Object value = jObj.get(key);

            if (value instanceof JSONObject) {
                finalValue = recurseKeys((JSONObject) value, findKey);
            }
        }

        // key is not found
        return finalValue;
    }
}



