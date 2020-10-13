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
public class RuleDatabaseControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private String token;
    private String userId = "1";


    private Map<String, Integer> brokerIdMap = new ConcurrentHashMap<>();

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
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
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"" + brokerUrl + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult<?> governanceResponse = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        brokerIdMap.put("brokerId", (Integer) governanceResponse.getData());
    }

    @Test
    public void testAddRuleDatabase() throws Exception {
        String content = "{\"databaseType\":1,\"datasourceName\":\"test123\",\"databaseUrl\":\"jdbc:h2:~/WeEvent_governance\"," +
                "\"username\":\"root\",\"password\":\"123456\",\"tableName\":\"t_rule_database\"," +
                "\"userId\":" + this.userId + ",\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"systemTag\":\"false\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/circulationDatabase/add").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult<?> governanceResponse = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResponse.getCode().intValue(), 200);
    }

    @Test
    public void testGetRuleDatabases() throws Exception {
        String content = "{\"brokerId\":\"1\",\"userId\":\"1\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/circulationDatabase/list").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult<?> governanceResponse = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResponse.getCode().intValue(), 200);
    }

    @Test
    public void testUpdateRuleDatabase() throws Exception {
        String content = "{\"id\":\"1\",\"databaseType\":1,\"datasourceName\":\"test123111\",\"databaseUrl\":\"jdbc:h2:~/WeEvent_governance\"," +
                "\"username\":\"root\",\"password\":\"123456\",\"tableName\":\"t_rule_database\"," +
                "\"userId\":" + this.userId + ",\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"systemTag\":\"false\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/circulationDatabase/update").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult<?> governanceResponse = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResponse.getCode().intValue(), 200);
    }

    @Test
    public void testDeleteRuleDatabase() throws Exception {
        String content = "{\"id\":\"1\",\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/circulationDatabase/delete").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult<?> governanceResponse = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResponse.getCode().intValue(), 200);
    }

    @Test
    public void testCheckDataBaseUrlException() throws Exception {
        String content = "{\"id\":1111,\"datasourceName\":\"test123\",\"databaseType\":1,\"databaseUrl\":\"jdbc:h2:~/WeEvent_governance\"," +
                "\"username\":\"root\",\"password\":\"123456\",\"tableName\":\"t_rule_database\"," +
                "\"userId\":" + this.userId + ",\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/circulationDatabase/checkDataBaseUrl").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map map = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertEquals(Integer.parseInt(map.get("code").toString()), -1);
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
