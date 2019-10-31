package com.webank.weevent.processor;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.sdk.IWeEventClient;

import com.alibaba.fastjson.JSONObject;
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
import static org.junit.Assert.assertNotNull;
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
                "        \"brokerUrl\": \"http://122.51.93.181:8090/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseurl\": \"jdbc:mysql://122.51.93.181:3306/cep?user=root&password=WeEvent@2019\",\n" +
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
                "        \"brokerUrl\": \"http://122.51.93.181:8090/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseurl\": \"jdbc:mysql://122.51.93.181:3306/cep?user=root&password=WeEvent@2019\",\n" +
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
                "        \"brokerUrl\": \"http://122.51.93.181:8090/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseurl\": \"jdbc:mysql://122.51.93.181:3306/cep?user=root&password=WeEvent@2019\",\n" +
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

        String ruleId = JSONObject.parseObject(result.getResponse().getContentAsString()).get("data").toString();
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
    public void checkTheHit() throws Exception {
        String url = "/startCEPRule";
        String cEPrule = " {\n" +
                " \t\t\"id\":1032,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://122.51.93.181:8090/weevent\",\n" +
                "        \"payload\":\"{\\\"a\\\":1,\\\"b\\\":\\\"test\\\",\\\"c\\\":10}\",\n" +
                "        \"payloadType\":\"c<20\",\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseurl\": \"jdbc:mysql://122.51.93.181:3306/cep?user=root&password=WeEvent@2019\",\n" +
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
        // publish message
        IWeEventClient client = IWeEventClient.build("http://122.51.93.181:8090/weevent");
        client.publish("from.com.webank.weevent", "{\"a\":1,\"b\":\"test\",\"c\":10}".getBytes());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void urlPage() {
        String url = "http://122.51.93.181:8090/weevent";
        String page = null;
        String[] arrSplit;

        arrSplit = url.split("[?]");
        if ((url.length()) > 0 && (arrSplit.length >= 1) && (arrSplit[0] != null)) {
            page = arrSplit[0];
        }
        log.info("test:{}", page);
        assertEquals(url, page);
    }

}