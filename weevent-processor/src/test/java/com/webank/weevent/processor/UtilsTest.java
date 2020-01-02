package com.webank.weevent.processor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.processor.utils.CommonUtil;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class UtilsTest {
    @Test
    public void checkReplaceCondition1() {
        String conditionField = "abs(a)<21 and c>10 or b.trim()==\"1111\" and floor(c)>10";
        String arr = "[[\"0\", \"6\", \"abs\", \"a\"], [\"22\", \"30\", \"trim\", \"b\"], [\"43\", \"51\", \"floor\", \"c\"]]";
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put("a", 10);
        payload.put("b", "1111 ");
        payload.put("c", 10);
        String[][] systemFunctionDetail = CommonUtil.stringConvertArray(arr);
        String condition = CommonUtil.replaceCondition(systemFunctionDetail, conditionField, payload);
        log.info("condition:{}", condition);
        Assert.assertEquals(condition, "10<21 and c>10 or \"1111\"==\"1111\" and 10.0>10");
    }

    @Test
    public void checkReplaceCondition2() {
        String conditionField = "abs(a)<21 or floor(c)>10";
        String arr = "[[\"0\", \"6\", \"abs\", \"a\"], [\"13\", \"21\", \"floor\", \"c\"]]";
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put("a", 10);
        payload.put("b", "1111 ");
        payload.put("c", 10);
        String[][] systemFunctionDetail = CommonUtil.stringConvertArray(arr);
        String condition = CommonUtil.replaceCondition(systemFunctionDetail, conditionField, payload);
        log.info("condition:{}", condition);
        Assert.assertEquals(condition, "10<21 or 10.0>10");
    }


    @Test
    public void checkReplaceCondition3() {
        String conditionField = "(abs(a)>=20 or (floor(b)!=222.2 and d<=111)) and ceil(c)<=111 or e!=33";
        String arr = "[[\"16\", \"24\", \"floor\", \"b\"], [\"1\", \"7\", \"abs\", \"a\"], [\"49\", \"56\", \"ceil\", \"c\"]]";
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put("a", 10);
        payload.put("b", 111);
        payload.put("c", 10);
        payload.put("d", 10);
        payload.put("e", 10);

        String[][] systemFunctionDetail = CommonUtil.stringConvertArray(arr);
        String condition = CommonUtil.replaceCondition(systemFunctionDetail, conditionField, payload);
        log.info("condition:{}", condition);
        Assert.assertNotNull(condition);
    }

    @Test
    public void checkReplaceCondition4() {
        String conditionField = "abs(a)>=20 or e!=33 and round(d)<=11";
        String arr = "[[\"0\", \"6\", \"abs\", \"a\"], [\"24\", \"32\", \"round\", \"d\"]]";
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put("a", 10);
        payload.put("b", 111);
        payload.put("c", 10);
        payload.put("d", 10);
        payload.put("e", 10);

        String[][] systemFunctionDetail = CommonUtil.stringConvertArray(arr);
        String condition = CommonUtil.replaceCondition(systemFunctionDetail, conditionField, payload);
        log.info("condition:{}", condition);
        Assert.assertEquals(condition, "10>=20 or e!=33 and 10<=11");

    }

    @Test
    public void checkReplaceCondition5() {
        String conditionField = "a.substring(11,12)==\"2\" and b.concat(a)==\"1234567890123456712345678901234567\"";
        String arr = "[[\"0\", \"18\", \"substring\", \"a,11,12\"], [\"28\", \"39\", \"concat\", \"b,a\"]]";
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put("a", "12345678901234567");
        payload.put("b", "12345678901234567");
        payload.put("c", 10);
        payload.put("d", 10);
        payload.put("e", 10);

        String[][] systemFunctionDetail = CommonUtil.stringConvertArray(arr);
        String condition = CommonUtil.replaceCondition(systemFunctionDetail, conditionField, payload);
        log.info("condition:{}", condition);
        Assert.assertEquals(condition, "\"2\"==\"2\" and \"1234567890123456712345678901234567\"==\"1234567890123456712345678901234567\"");

    }

    @Test
    public void checkReplaceCondition6() {
        String conditionField = "a.substring(11)==\"aa\" and b.concat(a)==\"aa\"";
        String arr = "[[\"0\", \"15\", \"substring\", \"a,11\"], [\"26\", \"37\", \"concat\", \"b,a\"]]";
        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put("a", "0123456789101112131415");
        payload.put("b", "0123456789101112131415");
        payload.put("c", 10);
        payload.put("d", 10);
        payload.put("e", 10);


        String[][] systemFunctionDetail = CommonUtil.stringConvertArray(arr);
        String condition = CommonUtil.replaceCondition(systemFunctionDetail, conditionField, payload);
        log.info("condition:{}", condition);
        Assert.assertEquals(condition, "\"01112131415\"==\"aa\" and \"01234567891011121314150123456789101112131415\"==\"aa\"");

    }

    @Test
    public void stringConvertArray() {
        String s = "[[\"0\", \"8\", \"floor\", \"c\"]]";
        String[][] ret = CommonUtil.stringConvertArray(s);
        Assert.assertEquals(ret.length, 1);

    }

    @Test
    public void checkReplaceCondition7() {
        String conditionField = "(b>=11 and (abs(c)!=22)) and abs(d)<=33";
        String arr = "[[\"12\",\"18\",\"abs\",\"c\"],[\"29\",\"35\",\"abs\",\"d\"]]";

        Map<String, Object> payload = new ConcurrentHashMap<>();
        payload.put("a", "12345678901234567");
        payload.put("b", 111);
        payload.put("c", 10);
        payload.put("d", 10);

        String[][] systemFunctionDetail = CommonUtil.stringConvertArray(arr);
        String condition = CommonUtil.replaceCondition(systemFunctionDetail, conditionField, payload);
        log.info("condition:{}", condition);
        Assert.assertEquals(condition, "(b>=11 and (10!=22)) and 10<=33");

    }
}
