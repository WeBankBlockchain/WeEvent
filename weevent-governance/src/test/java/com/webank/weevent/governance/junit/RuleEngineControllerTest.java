package com.webank.weevent.governance.junit;


import javax.servlet.http.Cookie;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.result.GovernanceResult;

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
    private Cookie cookie;


    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        this.cookie = new Cookie(ConstantProperties.COOKIE_MGR_ACCOUNT_ID, "1");
    }

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }

    @Test
    public void testAddRuleEngine() throws Exception {
        String content = "{\"ruleName\":\"tempera_ture-alaa23\",\"payloadType\":\"1\",\"payloadMap\":{\"temperate\":30,\"humidity\":0.5}," +
                "\"userId\":\"1\",\"brokerId\":\"1\",\"groupId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }

    @Test
    public void testGetRuleEngines() throws Exception {
        String content = "{\"userId\":\"1\",\"brokerId\":\"1\",\"pageNumber\":\"1\",\"pageSize\":\"10\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/list").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }

    @Test
    public void testGetRuleEngineDetail() throws Exception {
        String content = "{\"userId\":\"1\",\"brokerId\":\"1\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/detail").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }


    @Test
    public void testUpdateRuleEngine() throws Exception {
        String content = "{\"id\":\"1\",\"ruleName\":\"temperature-alarm6616\",\"payloadType\":\"1\"," +
                "\"payloadMap\":{\"temperate\":30,\"humidity\":0.5},\"brokerId\":\"1\"," +
                "\"fromDestination\":\"com.weevent.stomp\",\"com.weevent.mqtt\":\"test\"," +
                "\"selectField\":\"temperate\",\"conditionField\":\"temperate>38\",\"conditionType\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/update").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }


    @Test
    public void testStartEngine() throws Exception {
        String content = "{\"id\":\"23\",\"userId\":\"1\",\"brokerId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/start").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }


    @Test
    public void testDeleteRuleEngine() throws Exception {
        String content = "{\"id\":\"19\",\"userId\":\"1\",\"brokerId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }


}
