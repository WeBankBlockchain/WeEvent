package com.webank.weevent.governance.junit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;

import com.webank.weevent.governance.JUnitTestBase;
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
public class TopicHistoricalTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private Cookie cookie;


    @Value("${weevent.url:http://127.0.0.1:7000/weevent}")
    private String brokerUrl;

    private Map<String, Integer> brokerIdMap = new ConcurrentHashMap<>();


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
    }

    //add broker
    public void addBroker() throws Exception {
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"" + this.brokerUrl + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        brokerIdMap.put("brokerId", (Integer) governanceResult.getData());
    }

    @Test
    public void testHistoricalDataList() throws Exception {
        String content = "{\"groupId\":\"1\",\"userId\":\"1\",\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"beginDate\":\"2019-12-08\",\"endDate\":\"2099-12-15\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/historicalData/list")
                .contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JsonUtil.parseObject(result, GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().toString(), "200");
    }

    @Test
    public void testEventList() throws Exception {
        String content = "{\"groupId\":\"1\",\"userId\":\"1\",\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"beginDate\":\"2019-12-08\",\"endDate\":\"2099-12-15\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/historicalData/eventList")
                .contentType(MediaType.APPLICATION_JSON_UTF8).cookie(cookie).content(content)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JsonUtil.parseObject(result, GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().toString(), "200");
    }

    //delete broker by id
    public void deleteBroker() throws Exception {
        String content = "{\"id\":" + this.brokerIdMap.get("brokerId") + ",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/delete").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonUtil.parseObject(response.getContentAsString(), Map.class);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }


    @After
    public void after() throws Exception {
        deleteBroker();
    }


}
