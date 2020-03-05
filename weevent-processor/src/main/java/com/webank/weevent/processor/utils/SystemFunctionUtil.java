package com.webank.weevent.processor.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SystemFunctionUtil {
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
            conditionField) throws BrokerException {

        Map maps = JsonHelper.object2Map(payload);
        Map<String, Object> payloadMap = new ConcurrentHashMap<>();
        for (Object map : maps.entrySet()) {
            payloadMap.put((String) ((Map.Entry) map).getKey(), ((Map.Entry) map).getValue());
        }
        return replaceCondition(systemFunctionMessage, conditionField, payloadMap);
    }

    public static boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches() && str.contains(".");
    }

    public static Pair<StringBuilder, Integer> numberOperator(StringBuilder sb, String conditionField, String[] arr, Map
            payload, int changePosition) {
        Integer start = Integer.valueOf(arr[0]);
        Integer end = Integer.valueOf(arr[1]);
        String type = arr[2];
        int changePositionAfter = 0;
        switch (type) {
            case "abs":
                if (isDouble((payload.get(arr[3])).toString())) {
                    sb.replace(start - changePosition, end - changePosition, String.valueOf(Math.abs((Double) payload.get(arr[3]))));
                } else {
                    sb.replace(start - changePosition, end - changePosition, String.valueOf(Math.abs((Integer) payload.get(arr[3]))));
                }
                changePositionAfter = changePosition(conditionField, sb.toString());
                break;

            case "ceil":
                if (isDouble((payload.get(arr[3])).toString())) {
                    sb.replace(start - changePosition, end - changePosition, String.valueOf(Math.ceil((Double) payload.get(arr[3]))));
                } else {
                    sb.replace(start - changePosition, end - changePosition, String.valueOf(Math.ceil((Integer) payload.get(arr[3]))));
                }
                changePositionAfter = changePosition(conditionField, sb.toString());
                break;

            case "floor":
                if (isDouble((payload.get(arr[3])).toString())) {
                    sb.replace(start - changePosition, end - changePosition, String.valueOf(Math.floor((Double) payload.get(arr[3]))));
                } else {
                    sb.replace(start - changePosition, end - changePosition, String.valueOf(Math.floor((Integer) payload.get(arr[3]))));
                }
                changePositionAfter = changePosition(conditionField, sb.toString());
                break;

            case "round":
                if (isDouble((payload.get(arr[3])).toString())) {
                    sb.replace(start - changePosition, end - changePosition, String.valueOf(Math.round((Double) payload.get(arr[3]))));
                } else {
                    sb.replace(start - changePosition, end - changePosition, String.valueOf(Math.round(Math.round((Integer) payload.get(arr[3])))));
                }
                changePositionAfter = changePosition(conditionField, sb.toString());
                break;
            default:
                log.info("conditionField:{}", conditionField);
                break;
        }
        return new Pair<>(sb, changePositionAfter);
    }

    public static Pair<StringBuilder, Integer> stringOperator(StringBuilder sb, String conditionField, String[] arr, Map
            payload, int changePosition) {
        String type = arr[2];
        String left = arr[3]; // end position
        String middle = "";
        String right = "";

        if (arr.length == 5) {
            left = arr[3];
            right = arr[4];
        } else if (arr.length == 6) { // multi parameter
            left = arr[3];
            middle = arr[4];
            right = arr[5];
        }

        String replaceContent = "";
        Integer start = Integer.valueOf(arr[0]);
        Integer end = Integer.valueOf(arr[1]);
        int changePositionAfter = 0;
        switch (type) {
            case "substring":
                if (!"".equals(middle)) {
                    replaceContent = "\"" + payload.get(left).toString().substring(Integer.valueOf(middle), Integer.valueOf(right)) + "\"";
                    sb.replace(start - changePosition, end - changePosition, replaceContent);
                } else {
                    replaceContent = "\"" + payload.get(left).toString().substring(Integer.valueOf(right)) + "\"";
                    sb.replace(start - changePosition, end - changePosition, replaceContent);
                }
                changePositionAfter = changePosition(conditionField, sb.toString());
                break;

            case "concat":
                replaceContent = "\"" + payload.get(left).toString().concat(payload.get(right).toString()) + "\"";
                sb.replace(start - changePosition, end - changePosition, replaceContent);
                changePositionAfter = changePosition(conditionField, sb.toString());
                break;

            case "trim":
                replaceContent = "\"" + payload.get(left).toString().trim() + "\"";
                sb.replace(start - changePosition, end - changePosition, replaceContent);
                changePositionAfter = changePosition(conditionField, sb.toString());
                break;

            case "lcase":
                replaceContent = "\"" + payload.get(left).toString().toLowerCase() + "\"";
                sb.replace(start - changePosition, end - changePosition, replaceContent);
                changePositionAfter = changePosition(conditionField, sb.toString());
                break;
            default:
                log.info("conditionField:{}", conditionField);
                break;
        }
        return new Pair<>(sb, changePositionAfter);
    }

    public static Pair<StringBuilder, Integer> timeOperator(StringBuilder sb, String conditionField, String[] arr, int changePositionString) {
        Integer start = Integer.valueOf(arr[0]);
        Integer end = Integer.valueOf(arr[1]);
        String type = arr[2];
        int changePositionAfter = 0;
        switch (type) {
            case "now":
                sb.replace(start - changePositionString, end - changePositionString, String.valueOf(new Date().getTime()));
                changePositionAfter = changePosition(conditionField, sb.toString());

                break;
            case "currentDate":
                String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
                sb.replace(start - changePositionString, end - changePositionString, date);
                changePositionAfter = changePosition(conditionField, sb.toString());

                break;
            case "currentTime":
                String time = new SimpleDateFormat("HHmmss").format(new Date());
                sb.replace(start - changePositionString, end - changePositionString, time);
                changePositionAfter = changePosition(conditionField, sb.toString());

                break;
            default:
                log.info("conditionField:{}", conditionField);
                break;
        }
        return new Pair<>(sb, changePositionAfter);
    }

    public static Pair<StringBuilder, Integer> replaceCase(StringBuilder sb, String conditionField, Map
            payload, String[] arr, int changePosition) {
        String type = arr[2];

        Pair<StringBuilder, Integer> ret;
        int changePositionAfter = 0;
        StringBuilder sbTransform = null;
        switch (type) {
            case "now":
            case "currentDate":
            case "currentTime":
                ret = timeOperator(sb, conditionField, arr, changePosition);
                sbTransform = ret.getKey();
                changePositionAfter = ret.getValue();
                break;

            case "abs":
            case "ceil":
            case "floor":
            case "round":
                ret = numberOperator(sb, conditionField, arr, payload, changePosition);
                sbTransform = ret.getKey();
                changePositionAfter = ret.getValue();
                break;

            case "substring":
            case "concat":
            case "trim":
            case "lcase":
                ret = stringOperator(sb, conditionField, arr, payload, changePosition);
                sbTransform = ret.getKey();
                changePositionAfter = ret.getValue();
                break;

            default:
                log.info("conditionField:{}", conditionField);
                break;
        }
        return new Pair<>(sbTransform, changePositionAfter);
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
            Pair<StringBuilder, Integer> ret = replaceCase(sb, conditionField, payload, arr[i], changePosition);
            sb = ret.getKey();
            changePosition = ret.getValue();
        }
        log.info("sb:{}", sb);
        return sb.toString();
    }
}