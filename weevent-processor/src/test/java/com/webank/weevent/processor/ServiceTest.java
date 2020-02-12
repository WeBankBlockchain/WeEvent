package com.webank.weevent.processor;

import java.util.Date;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.utils.JsonUtil;

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
    public void setUp() {
        String brokerUrl = wac.getEnvironment().getProperty("ci.broker.ip");
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        rule.setId("1111");
        rule.setRuleName("test");
        rule.setBrokerId("1");
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(a)<21 or floor(c)>10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setDatabaseUrl("jdbc:mysql://127.0.0.1:3306/fromIfttt?user=root&password=111111");
        rule.setBrokerUrl("http://"+brokerUrl+"/weevent");
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
    public void checkConditionRight() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "c<10");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void checkConditionRight6() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"2018-06-30 20:00:00\",\"c\":10}").param("condition", "c<10");
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
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }

    @Test
    public void deleteRule() throws Exception {
        String url = "/deleteCEPRuleById";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).param("id", "1111");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void absHitRule() throws Exception {
        String arr = "[[\"0\", \"6\", \"floor\", \"c\"]]";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"c\":10}");
        rule.setSelectField("a,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(c)<10");
        rule.setToDestination("to.com.webank.weevent");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getStatus());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void multiHitRule1() throws Exception {
        String arr = "[[\"12\",\"18\",\"abs\",\"c\"],[\"29\",\"35\",\"abs\",\"d\"]]";
        rule.setPayload("{\"a\":\"12345678901234567\",\"b\":20,\"c\":10,\"d\":10}");
        rule.setSelectField("a");
        rule.setConditionField("(b>=11 and (abs(c)!=22)) and abs(d)<=33");
        rule.setFunctionArray(arr);
        rule.setConditionType(2);
        rule.setId("20200102");
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());

        rule.setFromDestination("testFromDestination");
        RequestBuilder requestBuilder4 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result4 = mockMvc.perform(requestBuilder4).andDo(print()).andReturn();
        log.info("result4:{}", result4.getResponse().getContentAsString());
        assertEquals(200, result4.getResponse().getStatus());

        String url1 = "/statistic";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url1).contentType(MediaType.APPLICATION_JSON).param("idList", "1104154821111");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result);
        assertEquals(200, result.getResponse().getStatus());

        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.get(url1).contentType(MediaType.APPLICATION_JSON).param("idList", "1104154821");
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        log.info("result:{}", result3);
        assertEquals(200, result2.getResponse().getStatus());
    }

    @Test
    public void statistic1() throws Exception {
        String url1 = "/statistic";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url1).contentType(MediaType.APPLICATION_JSON).param("idList", "");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());

        String url2 = "/statistic";
        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.get(url2).contentType(MediaType.APPLICATION_JSON).param("idList", "1104154821111");
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        log.info("result:{}", result2.getResponse().getContentAsString());
        assertEquals(200, result2.getResponse().getStatus());
    }


    @Test
    public void multiHitRule() throws Exception {
        String arr ="[[\"0\",\"8\", \"abs\",\"age\"]]";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"age\":1,\"name\":\"rocky\"}");
        rule.setSelectField("age,name,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(age)>20 and name!=\"name\"");
        rule.setToDestination("to.com.webank.weevent");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        rule.setId("202002101213");
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getStatus());
        assertEquals(200, result3.getResponse().getStatus());
    }

    @Test
    public void multiHitRule2() throws Exception {
        String url1 = "/statistic";
        String arr ="[[\"0\",\"9\",\"ceil\",\"age\"]]";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"age\":1,\"name\":\"rocky\"}");
        rule.setSelectField("age,name,eventId,topicName,brokerId,groupId");
        rule.setConditionField("ceil(age)>=20 and (age<=40 and name!=\"mark\")");
        rule.setToDestination("to.com.webank.weevent");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        rule.setId("202002101213");
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getStatus());
        assertEquals(200, result3.getResponse().getStatus());

        RequestBuilder requestBuilder5 = MockMvcRequestBuilders.get(url1).contentType(MediaType.APPLICATION_JSON).param("idList", "202002101213");
        MvcResult result5 = mockMvc.perform(requestBuilder5).andDo(print()).andReturn();
        log.info("result:{}", result5);
        assertEquals(200, result5.getResponse().getStatus());

        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"age\":1,\"name\":\"rocky\"}");
        rule.setSelectField("age,name,eventId,topicName,brokerId,groupId");
        rule.setConditionField("ceil(age)>=20 and (age<=40 and name!=\"mark\")");
        rule.setToDestination("to.com.webank.weevent");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        rule.setId("202002101214");
        RequestBuilder requestBuilder6 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result6 = mockMvc.perform(requestBuilder6).andDo(print()).andReturn();
        log.info("result3:{}", result6.getResponse().getStatus());
        assertEquals(200, result6.getResponse().getStatus());


        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url1).contentType(MediaType.APPLICATION_JSON).param("idList", "202002101214");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result);
        assertEquals(200, result.getResponse().getStatus());

        // rule.setId("202002101215");
        rule.setFromDestination("testFromDestination");
        RequestBuilder requestBuilder4 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result4 = mockMvc.perform(requestBuilder4).andDo(print()).andReturn();
        log.info("result4:{}", result4.getResponse().getContentAsString());
        assertEquals(200, result4.getResponse().getStatus());

        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.get(url1).contentType(MediaType.APPLICATION_JSON).param("idList", "202002101213","202002101214","202002101215");
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        log.info("result:{}", result3);
        assertEquals(200, result2.getResponse().getStatus());
    }

    @Test
    public void multiHitRule3() throws Exception {
        String arr ="[[\"0\",\"6\", \"abs\",\"a\"]]";
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"rocky\"}");
        rule.setSelectField("a,b,eventId,topicName,brokerId,groupId");
        rule.setConditionField("abs(a)>20");
        rule.setToDestination("to.com.webank.weevent");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        rule.setId("202002101216");
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getStatus());
        assertEquals(200, result3.getResponse().getStatus());
    }


    @Test
    public void nowSystemToTopic() throws Exception {
        String arr = "[[\"10\",\"13\",\"now\",\"datatime\"]]";
        String url = "/startCEPRule";
        rule.setId("110000");
        rule.setFromDestination("from.com.webank.weevent");
        rule.setPayload("{\"a\":1,\"b\":\"test\",\"datatime\":\"2018-06-30 20:00:00\"}");
        rule.setSelectField("a,eventId,currentDate");
        rule.setConditionField("datatime<=now");
        rule.setToDestination("to.com.webank.weevent");
        rule.setFunctionArray(arr);
        rule.setConditionType(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        log.info("result3:{}", result);
    }
}