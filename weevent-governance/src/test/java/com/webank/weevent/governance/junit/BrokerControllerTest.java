package com.webank.weevent.governance.junit;


import java.util.List;

import javax.servlet.http.Cookie;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.entity.BrokerEntity;
import com.webank.weevent.governance.properties.ConstantProperties;

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
public class BrokerControllerTest extends JUnitTestBase {

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
    public void testAddBroker() throws Exception {
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"http://10.107.105.228:8111/weevent\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }

    @Test
    public void testUpdateBroker() throws Exception {
        String content = "{\"id\":\"1\",\"name\":\"broker1\",\"brokerUrl\":\"http://10.107.105.228:8111/weevent\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/update").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));

    }


    @Test
    public void testGetBroker() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/broker/1").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertNotNull(contentAsString);
        JSONObject jsonObject = JSONObject.parseObject(contentAsString);
        Assert.assertEquals(jsonObject.get("id").toString(), "1");
    }

    @Test
    public void testGetBrokers() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/broker/list?userId=1").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertNotNull(contentAsString);
        List<BrokerEntity> brokerEntities = JSONObject.parseArray(contentAsString, BrokerEntity.class);
        Assert.assertEquals(brokerEntities.get(0).getUserId().toString(), "1");
    }

    @Test
    public void testDeleteBroker() throws Exception {
        String content = "{\"id\":\"1\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }
}
