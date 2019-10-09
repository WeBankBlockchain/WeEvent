package com.webank.weevent.processor.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.statement.select.PlainSelect;

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
            return DriverManager.getConnection(databaseUrl);
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

    /**
     * right value is a number
     *
     * @param RightKey right value
     * @param eventContent event content
     * @param item
     * @param operation operation
     * @return
     * @throws JSONException
     */
    private static boolean CompareCondition(Integer RightKey, String eventContent, String item, String operation) throws JSONException {

        JSONObject jObj = JSONObject.parseObject(eventContent);
        String extract = Util.recurseKeys(jObj, item);
        if ((operation.equals(Constants.NOT_QUALS_TO) || operation.equals(Constants.NOT_QUALS_TO_TWO)) && !Integer.valueOf(extract).equals(RightKey)) {
            return true;
        } else if (operation.equals(Constants.QUALS_TO) && (Integer.valueOf(extract).equals(RightKey))) {
            return true;
        } else if (operation.equals(Constants.MINOR_THAN) && (Integer.valueOf(extract) < RightKey)) {
            return true;
        } else if (operation.equals(Constants.MINOR_THAN_EQUAL) && Integer.valueOf(extract) <= RightKey) {
            return true;
        } else if (operation.equals(Constants.GREATER_THAN) && (Integer.valueOf(extract)) > RightKey) {
            return true;
        } else if (operation.equals(Constants.MINOR_THAN_EQUAL) && (Integer.valueOf(extract)) >= RightKey) {
            return true;
        }
        return false;
    }

    /**
     * check the pattern of url
     *
     * @param urls
     * @return true false
     */
    private static Boolean isHttpUrl(String urls) {
        boolean isurl = false;
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式

        Pattern pat = Pattern.compile(regex.trim());
        Matcher mat = pat.matcher(urls.trim());
        isurl = mat.matches();
        if (isurl) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * compare condition value
     *
     * @param leftValue Between (left value，right value)
     * @param rightValue Between (left value，right value)
     * @param eventContent event message
     * @param item current item
     * @return true false
     * @throws JSONException
     */
    private static boolean CompareCondition(Integer leftValue, Integer rightValue, String eventContent, String item) throws JSONException {
        JSONObject jObj = JSONObject.parseObject(eventContent);
        String extract = Util.recurseKeys(jObj, item);
        if (Integer.valueOf(extract) > leftValue && Integer.valueOf(extract) < rightValue) {
            return true;
        }
        return false;
    }

    /**
     * handle LIKE and IN
     *
     * @param rightValueStr like vale ,such as LIKE(?,?)
     * @param eventContent event message
     * @param item current item
     * @param operation Like or in
     * @return flag true or false
     */
    private static boolean CompareCondition(String rightValueStr, String eventContent, String item, String operation) throws JSONException {

        JSONObject jObj = JSONObject.parseObject(eventContent);
        String extract = Util.recurseKeys(jObj, item);
        if (operation.equals("LIKE") || operation.equals("IN")) {
            if (rightValueStr.contains(extract)) {
                log.info("hit it....");
                return true;
            }
        }
        return false;
    }

    public static boolean compareNumber(Expression whereCondition, List<String> contentKeys, String eventContent, String operation) {
        boolean flag = false;
        String leftKey;
        int RightKey;
        leftKey = (((BinaryExpression) whereCondition).getLeftExpression()).toString().toUpperCase();
        RightKey = Integer.valueOf((((BinaryExpression) whereCondition).getRightExpression()).toString());
        for (int i = 0; i < contentKeys.size(); i++) {
            if (contentKeys.get(i).equals(leftKey)) {
                flag = CompareCondition(RightKey, eventContent, contentKeys.get(i), operation);
            }
        }
        return flag;
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

    public static boolean compareNumber(PlainSelect plainSelect, List<String> contentKeys, String eventContent, String operation) {
        boolean flag = false;
        String leftKey;

        for (int i = 0; i < contentKeys.size(); i++) {
            switch (operation) {
                case Constants.BETWEEN:
                    log.info("check:start: {},end: {}", ((Between) plainSelect.getWhere()).getBetweenExpressionStart().toString(), ((Between) plainSelect.getWhere()).getBetweenExpressionEnd().toString());
                    int leftValue = Integer.valueOf(((Between) plainSelect.getWhere()).getBetweenExpressionStart().toString());
                    int rightValue = Integer.valueOf(((Between) plainSelect.getWhere()).getBetweenExpressionEnd().toString());
                    leftKey = ((Between) plainSelect.getWhere()).getLeftExpression().toString().toUpperCase();
                    if (contentKeys.get(i).equals(leftKey)) {
                        flag = CompareCondition(leftValue, rightValue, eventContent, contentKeys.get(i));
                    }

                    break;
                case Constants.LIKE:
                    leftKey = (((StringValue) ((LikeExpression) plainSelect.getWhere()).getLeftExpression()).getValue()).toUpperCase();
                    String rightValueStr = (((StringValue) ((LikeExpression) plainSelect.getWhere()).getRightExpression()).getValue());
                    if (contentKeys.get(i).equals(leftKey)) {
                        flag = CompareCondition(rightValueStr, eventContent, contentKeys.get(i), operation);
                    }
                    break;

                case Constants.IN:
                    final List exprList = new ArrayList();
                    Expression where = plainSelect.getWhere();
                    where.accept(new ExpressionVisitorAdapter() {

                        @Override
                        public void visit(InExpression expr) {
                            super.visit(expr);
                            exprList.add(expr.getLeftExpression());
                            exprList.add(expr.getLeftItemsList());
                            exprList.add(expr.getRightItemsList());
                            ItemsList qq = expr.getRightItemsList();
                        }
                    });
                    leftKey = exprList.get(0).toString().toUpperCase();
                    rightValueStr = exprList.get(2).toString();
                    if (contentKeys.get(i).equals(leftKey)) {
                        flag = CompareCondition(rightValueStr, eventContent, contentKeys.get(i), operation);
                    }
                    break;
                default:
                    log.error("default ");
            }
        }
        return flag;
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



