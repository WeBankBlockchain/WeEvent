package com.webank.weevent.governance.junit;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.service.BrokerService;

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

    private Map<String, Integer> brokerIdMap = new HashMap<>();

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        this.cookie = new Cookie(ConstantProperties.COOKIE_MGR_ACCOUNT_ID, "1");
        brokerIdMap.put("brokerId", 1);
    }

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }

    //add broker
    @Test
    public void testBroker001() throws Exception {
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"http://127.0.0.1:7000/weevent\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JSONObject.parseObject(response.getContentAsString(), GovernanceResult.class);
        brokerIdMap.put("brokerId", (Integer) governanceResult.getData());
        Assert.assertEquals(governanceResult.getStatus().toString(), "200");
    }

    //update broker
    @Test
    public void testBroker002() throws Exception {
        String content = "{\"id\":" + this.brokerIdMap.get("brokerId") + ",\"name\":\"broker1\",\"brokerUrl\":\"http://127.0.0.1:7000/weevent\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/update").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content)).andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

    }


    // get broker by id
    @Test
    public void testBroker003() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/broker/" + this.brokerIdMap.get("brokerId")).contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertNotNull(contentAsString);
    }

    // get broker by userId
    @Test
    public void testBroker004() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/broker/list?userId=1").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertNotNull(contentAsString);
    }


    //delete broker by id
    @Test
    public void testBroker005() throws Exception {
        String content = "{\"id\":" + this.brokerIdMap.get("brokerId") + ",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        JSONObject jsonObject = JSONObject.parseObject(response.getContentAsString());
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }

/*    @Test
    public void testBroker006() throws Exception {
        String content = "{\"id\":" + this.brokerId + ",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        JSONObject jsonObject = JSONObject.parseObject(response.getContentAsString());
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }*/

}
