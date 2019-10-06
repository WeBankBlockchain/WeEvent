package com.webank.weevent.processor;


import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.processor.model.CEPRule;
import com.webank.weevent.processor.service.CEPRuleServiceImpl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ServiceTest {

    @Autowired
    CEPRuleServiceImpl ruleService;
    private MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext wac;

    @Before()
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();  //初始化MockMvc对象
    }

    @Test
    public void selectByPrimaryKey() {
        CEPRule rule = ruleService.selectByPrimaryKey("6");
        System.out.println(rule);
        assertNotNull(rule);
        log.info("rule:{},rule id:{},rule name:{}", rule.toString(), rule.getId(), rule.getRuleName());
    }

    @Test
    public void getId() throws Exception {
        String url = "/getCEPRuleById";
        String responseString = mockMvc.perform(get(url)
                .param("id", "6")
                .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        log.info("return json:{}", responseString);

    }

    @Test
    public void selectByRuleName() throws Exception {
        String url = "/getCEPRuleByName";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON).param("ruleName", "air3");
        MvcResult result = mockMvc.perform(requestBuilder) .andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        Assert.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void selectByRuleId() throws Exception {
        String url = "/getCEPRuleById";
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
                .param("id", "6");
        MvcResult result = mockMvc.perform(requestBuilder) .andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        Assert.assertEquals(200, result.getResponse().getStatus());

    }


    @Test
    public void insertRule200() throws Exception {
        String url = "/insert";
        String cEPrule = " {\n" +
                " \t\t\"id\":1,\n" +
                "        \"ruleName\": \"air3\",\n" +
                "        \"fromDestination\": \"from.com.webank.weevent\",\n" +
                "        \"brokerUrl\": \"http://182.254.159.91:8090/weevent\",\n" +
                "        \"payload\":\"{\\\"studentName\\\":\\\"lily\\\",\\\"studentAge\\\":12}\",\n" +
                "        \"payloadType\": 0,\n" +
                "        \"selectField\": null,\n" +
                "        \"conditionField\": null,\n" +
                "        \"conditionType\": 1,\n" +
                "        \"toDestination\": \"to.com.webank.weevent\",\n" +
                "        \"databaseurl\": \"jdbc:mysql://182.254.159.91:3306/cep?user=root&password=WeEvent@2019\",\n" +
                "        \"createdTime\": \"2019-08-23T18:09:16.000+0000\",\n" +
                "        \"status\": 1,\n" +
                "        \"errorDestination\": null,\n" +
                "        \"errorCode\": null,\n" +
                "        \"errorMessage\": null,\n" +
                "        \"updatedTime\": \"2019-08-23T18:09:16.000+0000\"\n" +
                "    }";

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(cEPrule);
        MvcResult result = mockMvc.perform(requestBuilder) .andDo(print()).andReturn();
        log.info("result:{}", result.getResponse().getContentAsString());
        Assert.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void insertRule400() throws Exception {

    }

}