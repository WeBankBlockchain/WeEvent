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
public class RuleDatabaseControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private Cookie cookie;

    private String userId = "1";

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        this.cookie = new Cookie(ConstantProperties.COOKIE_MGR_ACCOUNT_ID, this.userId);
    }

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }

    @Test
    public void testAddCirculationDatabase() throws Exception {
        String content = "{\"databaseUrl\":\"jdbc:mysql://127.0.0.1:3306/governance?user=root&password=123456&useUnicode=true&characterEncoding=utf-8&useSSL=false\"," +
                "\"userId\":"+this.userId+",\"brokerId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/circulationDatabase/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), ErrorCode.SUCCESS.getCode());
    }

    @Test
    public void testGetCirculationDatabases() throws Exception {
        String content = "{\"id\":\"1\",\"userId\":\"1\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/circulationDatabase/list").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), ErrorCode.SUCCESS.getCode());
    }

    @Test
    public void testUpdateCirculationDatabase() throws Exception {
        String content = "{\"id\":\"1\",\"userId\":\"1\"," +
                "\"databaseUrl\":\"jdbc:mysql://127.0.0.1:3306/governance?user=root&password=123456&useUnicode=true&characterEncoding=utf-8&useSSL=false\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/circulationDatabase/update").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), ErrorCode.SUCCESS.getCode());
    }

    @Test
    public void testDeleteCirculationDatabase() throws Exception {
        String content = "{\"id\":\"1\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/circulationDatabase/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), ErrorCode.SUCCESS.getCode());
    }


}
