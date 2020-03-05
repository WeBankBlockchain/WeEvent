package com.webank.weevent.processor;

import java.util.Date;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.client.JsonHelper;

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
public class ConditionTest {

    private MockMvc mockMvc;
    private CEPRule rule = new CEPRule();
    private String url = "/startCEPRule";

    @Autowired
    protected WebApplicationContext wac;


    @Before
    public void setUp() {
        String brokerUrl = wac.getEnvironment().getProperty("ci.broker.ip");
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        rule.setId("1111");
        rule.setRuleName("test");
        rule.setBrokerId("1");
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(a)<21 or floor(c)>10");
        rule.setToDestination("to.com.weevent.test");
        rule.setDatabaseUrl("jdbc:mysql://127.0.0.1:3306/fromIfttt?user=root&password=111111");
        rule.setBrokerUrl("http://" + brokerUrl + "/weevent");
        rule.setCreatedTime(new Date());
        rule.setStatus(1);
        rule.setUserId("1");
        rule.setGroupId("1");
        rule.setSystemTag("0");
        rule.setTableName("fromIfttt");
        rule.setFunctionArray(null);
        rule.setConditionType(1);
    }

    @Test
    public void checkMultiConditionHitToDB1() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 or a==10");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void checkMultiUpdateConditionHitToDB1() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 or a==10");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);

        String url1 = "/statistic";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url1).contentType(MediaType.APPLICATION_JSON).param("idList", "");
        MvcResult result4 = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("statistic:{}", result4.getResponse().getContentAsString());
        assertEquals(200, result4.getResponse().getStatus());

        rule.setFromDestination("test.fromDestination");
        rule.setConditionType(1);
        RequestBuilder requestBuilder1 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result1 = mockMvc.perform(requestBuilder1).andDo(print()).andReturn();
        assertEquals(200, result1.getResponse().getStatus());
        log.info("result:{}", result1);

        String url2 = "/statistic";
        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.get(url2).contentType(MediaType.APPLICATION_JSON).param("idList", "1104154821111");
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        log.info("result:{}", result2.getResponse().getContentAsString());
        assertEquals(200, result2.getResponse().getStatus());

    }

    @Test
    public void checkMultiConditionHitToDB2() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 and a>10");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void checkMultiConditionHitToTopic3() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 or a==10");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void checkMultiConditionHitToDB3() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20 or a==10");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void conditionIsBlankForDB() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void checkHitToDB() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c>20");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }


    @Test
    public void checkHitToTopic() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c>20");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result:{}", result);
    }

    @Test
    public void checkHitToTopic2() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("c>20");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result:{}", result);

        RequestBuilder requestBuilder1 = MockMvcRequestBuilders.get("/getJobDetail").contentType(MediaType.APPLICATION_JSON).param("id", "1111");
        MvcResult result2 = mockMvc.perform(requestBuilder1).andDo(print()).andReturn();
        assertEquals(200, result2.getResponse().getStatus());
        log.info("result:{}", result2);
    }

    @Test
    public void conditionIsBlankToTopic() throws Exception {
        String arr = "";
        rule.setSelectField("a,b,c");
        rule.setConditionField("");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result:{}", result);
    }

    @Test
    public void conditionIsBlankForTopic() throws Exception {
        String arr = "";
        rule.setSelectField("a");
        rule.setConditionField("");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }


    @Test
    public void selectIsStarToDB() throws Exception {
        String arr = "";
        rule.setSelectField("*");
        rule.setConditionField("c<20");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void selectIsStarToTopic() throws Exception {
        String arr = "";
        rule.setSelectField("*");
        rule.setConditionField("c<20");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void startAndUpdate() throws Exception {
        String arr = "";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,b");
        rule.setConditionField("c<10");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);

        rule.setSelectField("a,b,c");
        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        assertEquals(200, result2.getResponse().getStatus());
    }

    @Test
    public void selectEventIDToTopic() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,b,c");
        rule.setConditionField("c<20");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void selectSystemFieldToTopic() throws Exception {
        String arr = "";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c<20");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
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
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void conditionEqualToTopic() throws Exception {
        String arr = "";
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c==10");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
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
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
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
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void conditionConplexToDB() throws Exception {
        String arr = "";
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c==10 or a>10 or a<1");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void conditionConplexToDB2() throws Exception {
        String arr = "";
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void nowSystemToDB() throws Exception {
        String arr = "";
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,now");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void currentDateSystemToTopic() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,currentDate");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void currentTimeSystemToDB() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,currentTime");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }


    @Test
    public void currentTimeSystemToTopic2() throws Exception {
        String arr = "[[\"10\",\"21\",\"currentDate\",\"datatime\"]]";
        String url = "/startCEPRule";
        rule.setId("110000");
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"age\":1,\"name\":\"test\",\"datatime\":20200210}");
        rule.setSelectField("a,eventId,currentDate");
        rule.setConditionField("datatime>=currentDate");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void currentTimeSystemToTopic3() throws Exception {
        String arr = "[[\"11\",\"22\",\"currentTime\",\"datatime1\"]]";
        String url = "/startCEPRule";
        rule.setId("110000");
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"age\":1,\"name\":\"test\",\"datatime1\":20200210}");
        rule.setSelectField("a,eventId,currentDate");
        rule.setConditionField("datatime1>=currentTime");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }


    @Test
    public void systemParameterToDB() throws Exception {
        String arr = "";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId,now,currentDate,currentTime");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result.getResponse().getContentAsString());
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
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(c)<10");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getStatus());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void ceilHitRule() throws Exception {
        String arr = "[[\"0\", \"7\", \"floor\", \"c\"]]";
        String url = "/startCEPRule";
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("ceil(c)<10");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void floorHitRule1() throws Exception {
        String arr = "[[\"0\", \"8\", \"floor\", \"c\"]]";
        rule.setId("1111");
        rule.setRuleName("test");
        rule.setBrokerId("1");
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("floor(c)<10");
        rule.setToDestination("to.com.weevent.test");
        rule.setCreatedTime(new Date());
        rule.setStatus(1);
        rule.setUserId("1");
        rule.setGroupId("1");
        rule.setSystemTag("0");
        rule.setTableName("fromIfttt");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void ceilAbsFloorHitRule() throws Exception {
        String arr = "[[\"16\", \"24\", \"floor\", \"b\"], [\"1\", \"7\", \"abs\", \"a\"], [\"49\", \"56\", \"ceil\", \"c\"]]";
        rule.setPayload("{\"a\":1,\"b\":10,\"c\":10,\"d\":10,\"e\":10}");
        rule.setId("201912231453");
        rule.setConditionField("(abs(a)>=20 or (floor(b)!=222.2 and d<=111)) and ceil(c)<=111 or e!=33");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void ceilAbsFloorHitRule2() throws Exception {
        String arr = "[[\"16\", \"24\", \"floor\", \"b\"], [\"1\", \"7\", \"abs\", \"a\"], [\"49\", \"56\", \"ceil\", \"c\"]]";
        rule.setPayload("{\"a\":1,\"b\":10,\"c\":10,\"d\":10,\"e\":10}");
        rule.setId("201912231453");
        rule.setConditionField("(abs(a)>=20 or (floor(b)!=222.2 and d<=111)) and ceil(c)<=111 and e!=33");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void absFloorHitRule() throws Exception {
        String arr = "[[\"0\", \"6\", \"abs\", \"a\"], [\"13\", \"21\", \"floor\", \"c\"]]";
        String url = "/startCEPRule";
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(a)<21 or floor(c)>10");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void deleteTheRule() throws Exception {
        String arr = "";
        rule.setFromDestination("from.com.weevent.test");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId,now,currentDate,currentTime");
        rule.setConditionField("c==10 and a>10");
        rule.setToDestination("to.com.weevent.test");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());

        String url2 = "/deleteCEPRuleById";
        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.post(url2).contentType(MediaType.APPLICATION_JSON).param("id", rule.getId());
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        log.info("result:{}", result2.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void stringContactHitRule() throws Exception {
        String arr = "[[\"0\", \"18\", \"substring\", \"a,11,12\"], [\"28\", \"39\", \"concat\", \"b,a\"]]";
        rule.setPayload("{\"a\":\"12345678901234567\",\"b\":\"12345678901234567\",\"c\":10,\"d\":10,\"e\":10}");

        rule.setSelectField("a");
        rule.setConditionField("a.substring(11,12)==\"2\" and b.concat(a)!=\"1234567890123456712345678901234567\"");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        rule.setId("20191230");

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void stringContactHitRule2() throws Exception {
        String arr = "[[\"0\", \"18\", \"substring\", \"a,11,12\"], [\"28\", \"39\", \"concat\", \"b,a\"]]";
        rule.setPayload("{\"a\":\"12345678901234567\",\"b\":\"12345678901234567\",\"c\":10,\"d\":10,\"e\":10}");

        rule.setSelectField("a");
        rule.setConditionField("a.substring(11,12)==\"2\" and b.concat(a)==\"1234567890123456712345678901234567\"");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        rule.setId("20191230");

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void absMultiHitRule() throws Exception {
        String arr = "[[\"12\",\"18\",\"abs\",\"c\"],[\"29\",\"35\",\"abs\",\"d\"]]";
        rule.setPayload("{\"a\":\"12345678901234567\",\"b\":20,\"c\":10,\"d\":10}");
        rule.setSelectField("a");
        rule.setConditionField("(b>=11 and (abs(c)!=22)) and abs(d)<=33");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        rule.setId("20191230");

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonHelper.object2Json(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());
    }
}
