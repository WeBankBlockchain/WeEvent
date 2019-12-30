package com.webank.weevent.processor;

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

    @Autowired
    protected WebApplicationContext wac;


    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
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

        String url = "/insert";
        String cEPrule = " {\n" +
                " \t\t\"id\":10321253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c<21\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=password\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void checkHitToTopicDestination() throws Exception {

        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":10321253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c<20\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=password\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void updateCEPRuleById() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":10321253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"cc\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,cc\",\n" +
                "        \"conditionField\": \"cc<20\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=password\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void checkMultiConditionHitToDB1() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":10321253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c<20 and a>10\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkMultiConditionHitToDB2() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":10321253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c<20 and a==10\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkMultiConditionHitToDB3() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":10321253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c<20 or a==10\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkHitToDB1() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c>20\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void conditionIsBlankForDB() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkHitToDB() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":10321253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c<20\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void checkHitToTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c>20\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void conditionIsBlankToTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void conditionIsBlankForTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a\",\n" +
                "        \"conditionField\": \"\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void selectIsStarToDB() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011632,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"1\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"*\",\n" +
                "        \"conditionField\": \"c<19\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void selectIsStarToTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011632,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"1\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"*\",\n" +
                "        \"conditionField\": \"c<19\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void startAndUpdate() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":110112511,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c<20\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());


        String cEPrule2 = " {\n" +
                " \t\t\"id\":110112511,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":11}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b\",\n" +
                "        \"conditionField\": \"c<10\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule2);
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        log.info("result:{}", result2.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void selectEventIDToTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":110112511,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c<20\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void selectSystemFieldToTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11041548,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent?groupId=1\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c==10\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder2 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result2 = mockMvc.perform(requestBuilder2).andDo(print()).andReturn();
        log.info("result:{}", result2.getResponse().getContentAsString());
        assertEquals(200, result2.getResponse().getStatus());
    }


    @Test
    public void selectEventIDToDB() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11041548,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId\",\n" +
                "        \"conditionField\": \"c<20\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void selectSystemFieldToTopic2() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11041548,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent?groupId=1\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c==10\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void startCEPRule() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":110415481,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent?groupId=1\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c==10\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void conditionConplexToTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":110415481,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent?groupId=1\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c==10 and a>10 or a<1\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void conditionConplexToTopic2() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":110415481,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent?groupId=1\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c==10 and a>10\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void conditionConplexToDB() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":110415481,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent?groupId=1\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c==10 or a>10 or a<1\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void conditionConplexToDB2() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":110415481,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent?groupId=1\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c==10 and a>10\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder1 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder1).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
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
    public void getWeEventCollection() throws Exception {
        String url = "/startCEPRule";
        String cEPrule3 = " {\n" +
                " \t\t\"id\":1104154821,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c<10\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"tableName\": \"fromIfttt\",\n" +
                "        \"systemFunctionMessage\": null,\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule3);
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result3.getResponse().getStatus());

        String url1 = "/statistic";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url1).contentType(MediaType.APPLICATION_JSON).param("brokerId", "1");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result);
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void multiHitRule1() throws Exception {
        String url = "/startCEPRule";
        String cEPrule3 = " {\n" +
                " \t\t\"id\":1104154821111,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c<10\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/fromIfttt?user=test&password=password&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"groupId\": \"1\",\n" +
                "        \"systemTag\": \"0\",\n" +
                "        \"tableName\": \"fromIfttt\",\n" +
                "        \"systemFunctionMessage\": null,\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";

        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule3);
        MvcResult result3 = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
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