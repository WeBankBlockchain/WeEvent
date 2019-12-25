package com.webank.weevent.processor;

import java.util.Date;

import com.webank.weevent.processor.model.CEPRule;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceTest {

    private MockMvc mockMvc;
    private CEPRule rule = new CEPRule();
    private String url = "/startCEPRule";

    @Autowired
    protected WebApplicationContext wac;


    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        rule.setId("1111");
        rule.setRuleName("test");
        rule.setBrokerId("1");
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(a)<21 or floor(c)>10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setDatabaseUrl("jdbc:mysql://129.204.225.235:3306/fromIfttt?user=test&password=007412");
        rule.setBrokerUrl("http://127.0.0.1:7000/weevent");
        rule.setCreatedTime(new Date());
        rule.setStatus(1);
        rule.setUserId("1");
        rule.setGroupId("1");
        rule.setSystemTag("0");
        rule.setTableName("fromIfttt");
        rule.setConditionType(1);

    }


    @Test
    public void checkConditionRight() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "c<10");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void checkConditionRight2() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "c<10 and (c=\"test\")");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkConditionRight3() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "c<10 and (c==\"10\")");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkConditionRight4() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "c==10");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkConditionRight5() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "b==\"10\"");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkConditionWrong2() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "a<10 and b>10");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkConditionWrong() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "c<test");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());

    }

    @Test
    public void insert() throws Exception {
        url = "/insert";

        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 or a==10");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }


    @Test
    public void checkMultiConditionHitToDB1() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 or a==10");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void checkMultiConditionHitToDB2() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 and a>10");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void checkMultiConditionHitToTopic3() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 or a==10");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void checkMultiConditionHitToDB3() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 or a==10");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void conditionIsBlankForDB() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void checkHitToDB() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c>20");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }


    @Test
    public void checkHitToTopic() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c>20");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void conditionIsBlankToTopic() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void conditionIsBlankForTopic() throws Exception {
        String arr = "";
        rule.setSelectField("a");
        rule.setConditionField("");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }


    @Test
    public void selectIsStarToDB() throws Exception {
        String arr = "";
        rule.setSelectField("*");
        rule.setConditionField("c<20");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void selectIsStarToTopic() throws Exception {
        String arr = "";
        rule.setSelectField("*");
        rule.setConditionField("c<20");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void startAndUpdate() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,b");
        rule.setConditionField("c<10");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);

        Thread.sleep(1000);
        rule.setSelectField("a,b,c");
        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        assertEquals(200, result2.getResponse().getStatus());
        Thread.sleep(1000000);
    }

    @Test
    public void selectEventIDToTopic() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }


    @Test
    public void selectSystemFieldToTopic() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c<20");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void selectEventIDToDB() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId");
        rule.setConditionField("c<20");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }


    @Test
    public void conditionEqualToTopic() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c==10");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }


    @Test
    public void conditionConplexToTopic() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c==10 and a>10 or a<1");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void conditionConplexToTopic2() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c==10 and a>10");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void conditionConplexToDB() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c==10 or a>10 or a<1");
        rule.setToDestination("to.com.webank.weevent");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void conditionConplexToDB2() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void nowSystemToDB() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,now");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }


    @Test
    public void currentDateSystemToDB() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,currentDate");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
        Thread.sleep(1000000);
    }

    @Test
    public void currentTimeSystemToDB() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,currentTime");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void systemParameterToDB() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId,now,currentDate,currentTime");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void getRuleDetails() throws Exception {
        String url = "/getCEPRuleById";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("id", "11041548");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void getNoNRuleDetails() throws Exception {
        String url = "/getCEPRuleById";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("id", "111");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void deleteRule() throws Exception {
        String url = "/deleteCEPRuleById";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).param("id", "11041548");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void absHitRule() throws Exception {
        String arr = "[[\"0\", \"6\", \"floor\", \"c\"]]";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(c)<10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3);
        Thread.sleep(1000000);
    }

    @Test
    public void ceilHitRule() throws Exception {
        String arr = "[[\"0\", \"7\", \"floor\", \"c\"]]";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("ceil(c)<10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3);
    }

    @Test
    public void floorHitRule1() throws Exception {
        String arr = "[[\"0\", \"8\", \"floor\", \"c\"]]";
        String url = "/startCEPRule";
        rule.setId("1111");
        rule.setRuleName("test");
        rule.setBrokerId("1");
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("floor(c)<10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setDatabaseUrl("jdbc:mysql://129.204.225.235:3306/fromIfttt?user=test&password=007412");
        rule.setBrokerUrl("http://122.51.93.181:7000/weevent");
        rule.setCreatedTime(new Date());
        rule.setStatus(1);
        rule.setUserId("1");
        rule.setGroupId("1");
        rule.setSystemTag("0");
        rule.setTableName("fromIfttt");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3);
    }

    @Test
    public void ceilAbsFloorHitRule() throws Exception {
        String url = "/startCEPRule";
        String arr = "[[\"16\", \"24\", \"floor\", \"b\"], [\"1\", \"7\", \"abs\", \"a\"], [\"49\", \"56\", \"ceil\", \"c\"]]";
        rule.setPayload("{\"a\":1,\"b\":10,\"c\":10,\"d\":10,\"e\":10}");
        rule.setId("201912231453");
        rule.setConditionField("(abs(a)>=20 or (floor(b)!=222.2 and d<=111)) and ceil(c)<=111 or e!=33");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3);
    }


    @Test
    public void absFloorHitRule() throws Exception {
        String arr = "[[\"0\", \"6\", \"abs\", \"a\"], [\"13\", \"21\", \"floor\", \"c\"]]";
        String url = "/startCEPRule";
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(a)<21 or floor(c)>10");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(1);

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3);
    }

    //:TODO add delete test
    @Test
    public void deleteTheRule() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId,now,currentDate,currentTime");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JSONObject.toJSON(rule).toString());
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);

        String url2 = "/deleteCEPRuleById";
        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.post(url2).contentType(MediaType.APPLICATION_JSON).param("id", rule.getId());
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        log.info("result:{}", result2.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

}