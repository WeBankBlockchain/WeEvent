package com.webank.weevent.governance.junit;


import javax.servlet.http.Cookie;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.code.ErrorCode;
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
        String content = "{\"ruleName\":\"tempera_ture-alarm\",\"payloadType\":\"1\",\"payloadMap\":{\"temperate\":30,\"humidity\":0.5}," +
                "\"userId\":\"1\",\"brokerId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }

    @Test
    public void testGetRuleEngines() throws Exception {
        String content = "{\"ruleName\":\"tempera_ture-alarm\",\"userId\":\"1\",\"brokerId\":\"1\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/list").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        GovernanceResult governanceResult = JSONObject.parseObject(contentAsString, GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), ErrorCode.SUCCESS.getCode());
    }

    @Test
    public void testUpdateRuleEngine() throws Exception {
        String content = "{\"id\":\"2\",\"ruleName\":\"temperature-alarm\",\"payloadType\":\"1\"," +
                "\"payloadMap\":{\"temperate\":30,\"humidity\":0.5},\"brokerId\":\"1\"," +
                "\"fromDestination\":\"airCondition\",\"toDestination\":\"test\"," +
                "\"selectField\":\"temperate\",\"conditionField\":\"temperate>38\",\"conditionType\":\"1\"," +
                "\"brokerUrl\":\"http://127.0.0.1:7000/weevent?groupId=1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/update").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), ErrorCode.SUCCESS.getCode());
    }

    @Test
    public void testUpdateRuleEngineStatus() throws Exception {
        String content = "{\"id\":\"2\",\"status\":\"1\",\"userId\":\"1\",\"brokerId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/updateStatus").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), ErrorCode.SUCCESS.getCode());
    }

    @Test
    public void testDeleteRuleEngine() throws Exception {
        String content = "{\"id\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.delete("/ruleEngine/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), ErrorCode.SUCCESS.getCode());
    }


}
