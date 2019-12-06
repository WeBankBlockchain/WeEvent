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
public class TopicHistoricalTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private Cookie cookie = new Cookie(ConstantProperties.COOKIE_MGR_ACCOUNT_ID, "1");


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
    public void testBroker001() throws Exception {
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"http://127.0.0.1:8111/weevent\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }


    @Test
    public void testHistoricalDataList() throws Exception {
        String content = "{\"groupId\":\"1\",\"userId\":\"1\",\"brokerId\":\"1\",\"beginDate\":\"2019-10-08\",\"endDate\":\"2019-10-15\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/historicalData/list")
                .contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JSONObject.parseObject(result, GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().toString(), "200");
    }

    @Test
    public void testEventList() throws Exception {
        String content = "{\"groupId\":\"1\",\"userId\":\"1\",\"brokerId\":\"1\",\"beginDate\":\"2019-10-08\",\"endDate\":\"2019-11-15\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/historicalData/eventList")
                .contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JSONObject.parseObject(result, GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().toString(), "200");
    }

    @Test
    public void testBroker002() throws Exception {
        String content = "{\"id\":\"1\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }

}
