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
        rule.setBrokerUrl("http://122.51.93.181:7000/weevent");
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
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
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
    public void multiHitRule1() throws Exception {
        String arr = "[[\"12\",\"18\",\"abs\",\"c\"],[\"29\",\"35\",\"abs\",\"d\"]]";
        rule.setPayload("{\"a\":\"12345678901234567\",\"b\":20,\"c\":10,\"d\":10}");
        rule.setSelectField("a");
        rule.setConditionField("(b>=11 and (abs(c)!=22)) and abs(d)<=33");
        rule.setSystemFunctionMessage(arr);
        rule.setConditionType(2);
        rule.setId("20191230");

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(rule));
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        log.info("result3:{}", result3.getResponse().getContentAsString());
        assertEquals(200, result3.getResponse().getStatus());

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
}