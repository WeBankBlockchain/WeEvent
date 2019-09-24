package com.webank.weevent.governance.junit;


import java.util.List;

import javax.servlet.http.Cookie;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.entity.RuleEngineEntity;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
public class RuleEngineControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }

    @Test
    public void testAddRuleEngine() throws Exception {
        Cookie cookie = new Cookie("admin", "123456");
        String content = "{\"ruleName\":\"temperature‐alarm\",\"payloadType\":\"1\"" +
                ",\"payload\":\"\"temperate\",\"userId\":\"1\"," +
                "\"brokerId\":\"1\",\"fromDestination\":\"airCondition\",\"toDestination\":\"test\"," +
                "\"selectField\":\"temperate\",\"conditionField\":\"temperate\",\"conditionType\":\"1\"," +
                "\"brokerUrl\":\"http://127.0.0.1:8090/weevent?groupId=1\",\"status\":\"0\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/add").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }

    @Test
    public void testGetRuleEngines() throws Exception {
        String content = "{\"ruleName\":\"test\",\"brokerUrl\":\"http://127.0.0.1:7000/weevent\",\"userId\":\"1\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/ruleEngine/list").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertNotNull(contentAsString);
        List<RuleEngineEntity> RuleEngineEntities = JSONObject.parseArray(contentAsString, RuleEngineEntity.class);
        Assert.assertEquals(RuleEngineEntities.get(0).getUserId().toString(), "1");
    }

    @Test
    public void testUpdateRuleEngine() throws Exception {
        String content = "{\"ruleName\":\"temperature-alarm\",\"payloadType\":\"1\"" +
                ",\"payload\":\"\"temperate\":30,\"humidity\":0.5\"\",\"userId\":\"1\"," +
                "\"brokerId\":\"1\",\"fromDestination\":\"airCondition\",\"toDestination\":\"test\"," +
                "\"selectField\":\"temperate\",\"conditionField\":\"temperate\",\"conditionType\":\"1\"," +
                "\"brokerUrl\":\"http://127.0.0.1:8090/weevent?groupId=1\",\"status\":\"0\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.put("/ruleEngine/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));

    }

    @Test
    public void testDeleteRuleEngine() throws Exception {
        String content = "{\"ruleName\":\"test\",\"brokerUrl\":\"http://127.0.0.1:7000/weevent\",\"userId\":\"1\"}";

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.delete("/ruleEngine/delete").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }


}
