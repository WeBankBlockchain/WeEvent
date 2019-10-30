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
            context.set("a", "rere");
            context.set("b", 1);
            context.set("c", 1);
            // Create an expression
            String jexlExp = "a>10";
            Boolean e = (Boolean) jexl.createExpression(jexlExp).evaluate(context);
            log.info(e.toString());
            Assert.assertEquals(true, e);
        } catch (Exception e) {
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

}
