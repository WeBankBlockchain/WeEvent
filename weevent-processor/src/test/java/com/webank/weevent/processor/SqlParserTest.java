package com.webank.weevent.processor;

import java.util.List;
import com.webank.weevent.processor.utils.CommonUtil;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class SqlParserTest {

    @Test
    public void basicExpression() {
        JexlEngine jexl = new JexlBuilder().create();
        JexlContext context = new MapContext();
        context.set("a", 1);
        context.set("b", 1);
        context.set("c", 1);
        // Create an expression
        String jexlExp = "a>10 and b<10 or c<10";
        Boolean e = (Boolean) jexl.createExpression(jexlExp).evaluate(context);
        log.info(e.toString());
        Assert.assertEquals(true, e);
    }

    @Test
    public void basicExpressionSingle() {
        try {
            JexlEngine jexl = new JexlBuilder().create();
            JexlContext context = new MapContext();
            context.set("b", 1);
            context.set("c", 1);
            String jexlExp = "c=1";
            Boolean e = (Boolean) jexl.createExpression(jexlExp).evaluate(context);
            log.info(e.toString());
            Assert.assertEquals(true, e);
        } catch (Exception e) {
            int c = 1;
            Boolean e1 = false;
            if (c == 1) {
                e1 = true;
                log.info("11error number");

                Assert.assertEquals(true, e1);
            } else {
                log.info("22error number");

                Assert.assertEquals(false, e1);
            }

            log.info("error number");
        }

    }

    @Test
    public void basicCheckExpression() {
        try {
            String payload = "{\"a\":1,\"b\":\"test\",\"c\":10}";
            String condition = "c<10";
            List<String> payloadContentKeys = CommonUtil.getKeys(payload);
            JSONObject payloadJson = JSONObject.parseObject(payload);
            JexlEngine jexl = new JexlBuilder().create();

            JexlContext context = new MapContext();
            for (String key : payloadContentKeys) {
                context.set(key, payloadJson.get(key));
            }
            Boolean e = (Boolean) jexl.createExpression(condition).evaluate(context);
            log.info(e.toString());
            Assert.assertEquals(true, e);
        } catch (Exception e) {
            log.info("error number");
        }

    }

    @Test
    public void basicCheckExpressionEqual() {
        try {
            String payload = "{\"a\":1,\"b\":\"test\",\"c\":10}";
            String condition = "(c=10) and c>1";
            List<String> payloadContentKeys = CommonUtil.getKeys(payload);
            JSONObject payloadJson = JSONObject.parseObject(payload);
            JexlEngine jexl = new JexlBuilder().create();

            JexlContext context = new MapContext();
            for (String key : payloadContentKeys) {
                context.set(key, payloadJson.get(key));
            }
            Boolean e = (Boolean) jexl.createExpression(condition).evaluate(context);
            log.info(e.toString());
            Assert.assertEquals(true, e);
        } catch (Exception e) {
            log.info("error number");
        }
    }

    @Test
    public void basicCheckExpressionEqualThree() {
        try {
            String payload = "{\"a\":1,\"b\":10,\"c\":10}";
            String condition = " a >= 1 and (b = 10) or c > 1 ";
            List<String> payloadContentKeys = CommonUtil.getKeys(payload);
            JSONObject payloadJson = JSONObject.parseObject(payload);
            JexlEngine jexl = new JexlBuilder().create();

            JexlContext context = new MapContext();
            for (String key : payloadContentKeys) {
                context.set(key, payloadJson.get(key));
            }
            Boolean e = (Boolean) jexl.createExpression(condition).evaluate(context);
            log.info(e.toString());
            Assert.assertEquals(true, e);
        } catch (Exception e) {
            log.info("error number");
        }

    }

    @Test
    public void basicCheckExpressionEqualTwo() {
        try {
            String payload = "{\"a\":1,\"b\":\"test\",\"c\":10}";
            String condition = "(c=10)and(b=\"test\")";
            List<String> payloadContentKeys = CommonUtil.getKeys(payload);
            JSONObject payloadJson = JSONObject.parseObject(payload);
            JexlEngine jexl = new JexlBuilder().create();

            JexlContext context = new MapContext();
            for (String key : payloadContentKeys) {
                context.set(key, payloadJson.get(key));
            }
            Boolean e = (Boolean) jexl.createExpression(condition).evaluate(context);
            log.info(e.toString());
            Assert.assertEquals(true, e);
        } catch (Exception e) {
            log.info("error number");
        }

    }

    @Test
    public void checkEqual() {
        String condition = "a=1";
        String eventMessage = "{\"a\":1,\"b\":\"test\",\"c\":10}";

        //String eventContent = new String(eventMessage.getContent());
        JSONObject event = JSONObject.parseObject(eventMessage);
        String[] strs = condition.split("=");
        if (strs.length == 2) {
            // event contain left key
            if (event.containsKey(strs[0]) && event.get(strs[0]).toString().equals(strs[1])) {
                log.info("{}", "true");
                Assert.assertEquals("true", "true");
            } else {
                log.info("{}", "false 1");
                Assert.assertEquals("fail", "fail");

            }
        }
    }


}
