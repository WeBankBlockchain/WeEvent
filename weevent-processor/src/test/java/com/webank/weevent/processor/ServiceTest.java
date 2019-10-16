package com.webank.weevent.processor;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.service.CEPRuleServiceImpl;

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

    @Autowired
    CEPRuleServiceImpl ruleService;
    private MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext wac;


    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void selectByPrimaryKey() {
        CEPRule rule = ruleService.selectByPrimaryKey("6");
        System.out.println(rule);
        assertNotNull(rule);
        log.info("rule:{},rule id:{},rule name:{}", rule.toString(), rule.getId(), rule.getRuleName());
    }


    @Test
    public void startCEPRule() throws Exception {
        String url = "/startCEPRule?id=201970367829835101";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON);
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void getCEPRuleListByPage() throws Exception {
        String url = "/getCEPRuleListByPage";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
                .param("currPage", "1")
                .param("pageSize", "10");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void getCEPRuleListByPageWrongParam() throws Exception {
        String url = "/getCEPRuleListByPage";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
                .param("currPage", "1")
                .param("pageSize", "0");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());

    }


    @Test
    public void getCEPRuleListByPageWrongParam2() throws Exception {
        String url = "/getCEPRuleListByPage";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
                .param("currPage", "1")
                .param("pageSize", "1");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void getCEPRuleListByPageWrongParam3() throws Exception {
        String url = "/getCEPRuleListByPage";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
                .param("currPage", "1")
                .param("pageSize", "51");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());

    }

    @Test
    public void selectByRuleName() throws Exception {
        String url = "/getCEPRuleByName";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("ruleName", "air3");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void selectByRuleNameWrongParam() throws Exception {
        String url = "/getCEPRuleByName";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("ruleName", "");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void selectByRuleNameWrongParam2() throws Exception {
        String url = "/getCEPRuleByName";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("ruleName", "你好中国");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void selectByRuleNameParam3() throws Exception {
        String url = "/getCEPRuleByName";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("ruleName", "wyruewyruyewuryewuryuewy" +
                "ewqewqeqweqeqwewqewqeqadadasdasdadadsadadhajsdajhdjahdjahdjahdjahdjahdjahdahdjahdjhadjahdjahdjahdjahdjajdadjadhajdhajhdjadadadadadadsd" +
                "ewqewqeqweqeqwewqewqeqadadasdasdadadsadadhajsdajhdjahdjahdjahdjahdjahdjahdahdjahdjhadjahdjahdjahdjahdjajdadjadhajdhajhdjadadadadadadsd" +
                "ewqewqeqweqeqwewqewqeqadadasdasdadadsadadhajsdajhdjahdjahdjahdjahdjahdjahdahdjahdjhadjahdjahdjahdjahdjajdadjadhajdhajhdjadadadadadadsd" +
                "ewqewqeqweqeqwewqewqeqadadasdasdadadsadadhajsdajhdjahdjahdjahdjahdjahdjahdahdjahdjhadjahdjahdjahdjahdjajdadjadhajdhajhdjadadadadadadsd" +
                "ewqewqeqweqeqwewqewqeqadadasdasdadadsadadhajsdajhdjahdjahdjahdjahdjahdjahdahdjahdjhadjahdjahdjahdjahdjajdadjadhajdhajhdjadadadadadadsd" +
                "ewqewqeqweqeqwewqewqeqadadasdasdadadsadadhajsdajhdjahdjahdjahdjahdjahdjahdahdjahdjhadjahdjahdjahdjahdjajdadjadhajdhajhdjadadadadadadsd" +
                "ewqewqeqweqeqwewqewqeqadadasdasdadadsadadhajsdajhdjahdjahdjahdjahdjahdjahdahdjahdjhadjahdjahdjahdjahdjajdadjadhajdhajhdjadadadadadadsd" +
                "adadhsahdashdjsahdjhsadjhasjdhajhdjahdjhajdhajhdradadasdddddddddddddddddddddddddddddddddddddddddddddddddddddddddadadadadadadadsas");
        MvcResult result = mockMvc.perform(requestBuilder).andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void selectByRuleId() throws Exception {
        String url = "/getCEPRuleById";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
                .param("id", "6");
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
                "        \"brokerUrl\": \"http://127.0.0.1:8090/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseurl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019\",\n" +
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
                "        \"brokerUrl\": \"http://127.0.0.1:8090/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseurl\": \"jdbc:mysql://127.0.0.1:3306/cep?user=root&password=WeEvent@2019\",\n" +
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
        ArgumentCaptor<CEPRule> argument = ArgumentCaptor.forClass(CEPRule.class);
        CEPRule rule = ruleService.selectByPrimaryKey(ruleId);
        assertEquals(ruleId, rule.getId());
        assertEquals("air3", rule.getRuleName());
    }

}