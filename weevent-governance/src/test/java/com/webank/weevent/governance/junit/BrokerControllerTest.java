package com.webank.weevent.governance.junit;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.utils.JwtUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.After;
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

    private String token;


    private Map<String, Integer> brokerIdMap = new ConcurrentHashMap<>();

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        brokerIdMap.put("brokerId", 1);
    }

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
        token = createToken();
        addBroker();
    }

    //add broker
    public void addBroker() throws Exception {
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"" + this.brokerUrl + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        brokerIdMap.put("brokerId", (Integer) governanceResult.getData());
    }

    @Test
    public void testAddBrokerException001() throws Exception {
        String content = "{\"name\":\"broker\",\"brokerUrl\":\"" + this.brokerUrl + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("100108", governanceResult.getStatus().toString());
    }

    @Test
    public void testAddBrokerException002() throws Exception {
        String content = "{\"name\":\"broker\",\"brokerUrl\":\"" + "" + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertEquals("100105", jsonObject.get("code").toString());
    }

    //update broker
    @Test
    public void updateBroker() throws Exception {
        String content = "{\"id\":" + this.brokerIdMap.get("brokerId") + ",\"name\":\"broker1\",\"brokerUrl\":\"" + this.brokerUrl + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/update").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content)).andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

    }


    // get broker by id
    @Test
    public void getBrokerByBrokerId() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/broker/" + this.brokerIdMap.get("brokerId")).contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertNotNull(contentAsString);
    }

    // get broker by userId
    @Test
    public void getBrokerByUserId() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/broker/list?userId=1").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertNotNull(contentAsString);
    }

    @Test
    public void testCheckServerByUrl() throws Exception {
        String content = "{\"id\":" + this.brokerIdMap.get("brokerId") + ",\"brokerUrl\":\"" + this.brokerUrl + "\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/checkServer").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertNotNull(contentAsString);
    }

    //delete broker by id
    public void deleteBroker() throws Exception {
        String content = "{\"id\":" + this.brokerIdMap.get("brokerId") + ",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/delete").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }


    @After
    public void after() throws Exception {
        deleteBroker();
    }

}
