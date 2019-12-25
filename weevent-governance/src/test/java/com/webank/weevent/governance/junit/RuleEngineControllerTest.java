package com.webank.weevent.governance.junit;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.entity.RuleEngineEntity;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${weevent.url:http://127.0.0.1:7000/weevent}")
    private String brokerUrl;

    private Map<String, Integer> ruleMap = new ConcurrentHashMap<>();


    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        this.cookie = new Cookie(ConstantProperties.COOKIE_MGR_ACCOUNT_ID, "1");
    }

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
        addBroker();
        testAddRuleEngine();
    }

    //add broker
    public void addBroker() throws Exception {
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"" + this.brokerUrl + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        ruleMap.put("brokerId", (Integer) governanceResult.getData());
    }

    public void testAddRuleEngine() throws Exception {
        String content = "{\"ruleName\":\"tempera_ture-alaa23\",\"payloadType\":\"1\",\"payloadMap\":{\"temperate\":30,\"humidity\":0.5}," +
                "\"userId\":\"1\",\"brokerId\":\"" + this.ruleMap.get("brokerId") + "\",\"groupId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Object data = governanceResult.getData();
        RuleEngineEntity engineEntity = JsonUtil.parseObject(JsonUtil.toJSONString(data), RuleEngineEntity.class);
        ruleMap.put("ruleId", engineEntity.getId());
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }

    @Test
    public void testAddRuleEngineException() throws Exception {
        String content = "{\"ruleName\":\"tempera_ture-alaa23\",\"payloadType\":\"1\",\"payloadMap\":{\"temperate\":30,\"humidity\":0.5}," +
                "\"userId\":\"1\",\"brokerId\":\"" + this.ruleMap.get("brokerId") + "\",\"groupId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonUtil.parseObject(response.getContentAsString(),Map.class);
        Assert.assertEquals(jsonObject.get("code").toString(), "-1");
    }

    @Test
    public void testGetRuleEngines() throws Exception {
        String content = "{\"userId\":\"1\",\"brokerId\":\"" + this.ruleMap.get("brokerId") + "\",\"pageNumber\":\"1\",\"pageSize\":\"10\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/list").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }

    @Test
    public void testGetRuleEngineDetail() throws Exception {
        String content = "{\"id\":\"" + ruleMap.get("ruleId") + "\",\"userId\":\"1\",\"brokerId\":\"" + this.ruleMap.get("brokerId") + "\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/detail").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }


    @Test
    public void testUpdateRuleEngine() throws Exception {
        String content = "{\"id\":\"" + ruleMap.get("ruleId") + "\",\"ruleName\":\"temperature-alarm6616\",\"payloadType\":\"1\"," +
                "\"payloadMap\":{\"temperate\":30,\"humidity\":0.5},\"brokerId\":\"" + this.ruleMap.get("brokerId") + "\"," +
                "\"fromDestination\":\"com.weevent.stomp\",\"toDestination\":\"com.weevent.test\",\"com.weevent.mqtt\":\"test\"," +
                "\"selectField\":\"temperate\",\"conditionField\":\"temperate>38\",\"conditionType\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/update").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }

    @Test
    public void testUpdateRuleEngineException001() throws Exception {
        String content = "{\"id\":\"" + ruleMap.get("ruleId") + "\",\"ruleName\":\"\",\"payloadType\":\"1\"," +
                "\"payloadMap\":{\"temperate\":30,\"humidity\":0.5},\"brokerId\":\"" + this.ruleMap.get("brokerId") + "\"," +
                "\"fromDestination\":\"com.weevent.stomp\",\"toDestination\":\"com.weevent.test\",\"com.weevent.mqtt\":\"test\"," +
                "\"selectField\":\"temperate\",\"conditionField\":\"temperate>38\",\"conditionType\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/update").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonUtil.parseObject(response.getContentAsString(),Map.class);
        Assert.assertEquals(jsonObject.get("code").toString(), "-1");
    }


    @Test
    public void testUpdateRuleEngineException002() throws Exception {
        String content = "{\"id\":\"" + ruleMap.get("ruleId") + "\",\"ruleName\":\"temperature-alarm6616\",\"payloadType\":\"1\"," +
                "\"brokerId\":\"" + this.ruleMap.get("brokerId") + "\"," +
                "\"fromDestination\":\"com.weevent.stomp\",\"toDestination\":\"com.weevent.test\",\"com.weevent.mqtt\":\"test\"," +
                "\"selectField\":\"temperate\",\"conditionField\":\"temperate>38\",\"conditionType\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/update").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonUtil.parseObject(response.getContentAsString(),Map.class);
        Assert.assertEquals(jsonObject.get("code").toString(), "-1");
    }


    @Test
    public void testStartEngine() throws Exception {
        testUpdateRuleEngine();
        String content = "{\"id\":\"" + ruleMap.get("ruleId") + "\",\"userId\":\"1\",\"brokerId\":\"" + this.ruleMap.get("brokerId") + "\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/start").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }

    public void testDeleteRuleEngine() throws Exception {
        String content = "{\"id\":\"" + ruleMap.get("ruleId") + "\",\"userId\":\"1\",\"brokerId\":\"" + this.ruleMap.get("brokerId") + "\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/ruleEngine/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }


    //delete broker by id
    public void deleteBroker() throws Exception {
        String content = "{\"id\":" + this.ruleMap.get("brokerId") + ",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonUtil.parseObject(response.getContentAsString(),Map.class);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }


    @After
    public void after() throws Exception {
        testDeleteRuleEngine();
        deleteBroker();

    }

}
