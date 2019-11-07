package com.webank.weevent.processor;

import com.webank.weevent.processor.model.CEPRule;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
    public void startCEPRule() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":1031,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void insertRule200() throws Exception {
        String url = "/insert";
        String cEPrule = " {\n" +
                " \t\t\"id\":1,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void insertNullRule400() throws Exception {
        String url = "/insert";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(400, result.getResponse().getStatus());
    }


    @Test
    public void insertAndSelectByPrimaryKey01() throws Exception {
        String url = "/insert";
        String cEPrule = " {\n" +
                " \t\t\"id\":1,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        ArgumentCaptor.forClass(CEPRule.class);
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

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "c<10 and (c=\"10\")");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkConditionRight4() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "c=10");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void checkConditionRight5() throws Exception {
        String url = "/checkWhereCondition";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("payload", "{\"a\":1,\"b\":\"test\",\"c\":10}").param("condition", "b=\"10\"");
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
    public void deleteRule() throws Exception {
        String url = "/deleteCEPRuleById";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).param("id", "1031");
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
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
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
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }


    @Test
    public void checkHitToDB1() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent111\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b,c\",\n" +
                "        \"conditionField\": \"c<20\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.com.webank.weevent111\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void conditionIsBlankForDB() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011651,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.conditionIsBlankForDB\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"b33\\\":1,\\\"c33\\\":\\\"test\\\",\\\"d33\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"b33,c33,d33\",\n" +
                "        \"conditionField\": \"\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.conditionIsBlankForDB\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=ifttt3\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void conditionIsBlankForTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.conditionIsBlankForTopic\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a22\\\":1,\\\"b22\\\":\\\"test\\\",\\\"c22\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a22,b22,c22\",\n" +
                "        \"conditionField\": \"\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.conditionIsBlankForTopic\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=ifttt2\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void selectIsStarToDB() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011632,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.selectIsStarToDB\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a1\\\":1,\\\"b1\\\":\\\"test\\\",\\\"c1\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"*\",\n" +
                "        \"conditionField\": \"c1<9\",\n" +
                "        \"conditionType\": 2,\n" +
                "        \"toDestination\": \"to.selectIsStarToDB\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=ifttt1\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void selectIsStarToTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011253,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.selectIsStarToTopic\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"*\",\n" +
                "        \"conditionField\": \"c<20\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.selectIsStarToTopic\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }


    @Test
    public void selectAddEventIDToTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11041548,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.selectAddEventIDToTopic\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId\",\n" +
                "        \"conditionField\": \"c<20\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.selectAddEventIDToTopic\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void selectCheckEqualToTopic() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11041548,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.selectAddEventIDToTopic\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent?groupId=1\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c=10\",\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.selectAddEventIDToTopic\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void selectAddEventIDToDB() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011604,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.selectAddEventIDToDB\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent?groupId=1\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId,topicName,brokerId,groupId\",\n" +
                "        \"conditionField\": \"c<20\",\n" +
                "        \"conditionType\":2,\n" +
                "        \"toDestination\": \"to.selectAddEventIDToDB\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void selectAddEventIDToDBMoreParam() throws Exception {
        StringBuffer insertExpression = new StringBuffer("\"(a=10) and (b= ");
        insertExpression.append("\\\"");
        insertExpression.append("test");
        insertExpression.append("\\\"");
        insertExpression.append(") and c>1\"");
        // "(a=10) and (b= \"test\") and c>1"
        log.info("{}", insertExpression.toString());
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011604,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.selectAddEventIDToDB\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,b\",\n" +
                "        \"conditionField\":" + insertExpression.toString() + ",\n" +
                "        \"conditionType\":2,\n" +
                "        \"toDestination\": \"to.selectAddEventIDToDB\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";

        log.info("{}", cEPrule);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void hitselectEventIDParamToDB() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011604,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.hitselectEventIDParamToDB\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"b\\\":1,\\\"c\\\":\\\"test\\\",\\\"d\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId\",\n" +
                "        \"conditionField\": \"d<10\",\n" +
                "        \"conditionType\":2,\n" +
                "        \"toDestination\": \"to.hitselectEventIDParamToDB\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void checkEqualConditionPattern11() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011604,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.hitselectEventIDParamToDB\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"b\\\":1,\\\"c\\\":\\\"test\\\",\\\"d\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId\",\n" +
                "        \"conditionField\": \"d=10\",\n" +
                "        \"conditionType\":2,\n" +
                "        \"toDestination\": \"to.hitselectEventIDParamToDB\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }


    @Test
    public void checkEqualConditionPattern() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011604,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.hitselectEventIDParamToDB\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"b\\\":1,\\\"c\\\":\\\"test\\\",\\\"d\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId\",\n" +
                "        \"conditionField\": \"d=10\",\n" +
                "        \"conditionType\":2,\n" +
                "        \"toDestination\": \"to.hitselectEventIDParamToDB\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }

    @Test
    public void notHitselectEventIDParamToDB() throws Exception {

        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":11011604,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.notHitselectEventIDParamToDB\",\n" +
                "        \"brokerUrl\": \"http://127.0.0.1:7000/weevent\",\n" +
                "        \"payload\":\"{\\\"b\\\":1,\\\"c\\\":\\\"test\\\",\\\"d\\\":10}\",\n" +
                "        \"payloadType\":1,\n" +
                "        \"selectField\": \"a,eventId\",\n" +
                "        \"conditionField\": \"d<0\",\n" +
                "        \"conditionType\":2,\n" +
                "        \"toDestination\": \"to.notHitselectEventIDParamToDB\",\n" +
                "        \"databaseUrl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019&tableName=fromIfttt\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"brokerId\": \"1\",\n" +
                "        \"userId\": \"1\",\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
        Thread.sleep(200000);
    }
}
