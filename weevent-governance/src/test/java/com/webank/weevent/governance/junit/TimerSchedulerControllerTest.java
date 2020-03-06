package com.webank.weevent.governance.junit;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.utils.JwtUtils;
import com.webank.weevent.client.JsonHelper;

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
public class TimerSchedulerControllerTest extends JUnitTestBase {

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
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"" + this.brokerUrl + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        brokerIdMap.put("brokerId", (Integer) governanceResult.getData());
    }

    @Test
    public void testAddTimerScheduler() throws Exception {
        String content = "{\"schedulerName\":\"test123\",\"ruleDatabaseId\":\"1\",\"periodParams\":\"0 0 * * * ？\"," +
                "\"parsingSql\":\"select 1 from t_account\"," +
                "\"userId\":" + this.userId + ",\"brokerId\":" + this.brokerIdMap.get("brokerId")+"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/timerScheduler/add").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }

    @Test
    public void testGetTimerSchedulers() throws Exception {
        String content = "{\"brokerId\":\"1\",\"userId\":\"1\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/timerScheduler/list").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }

    @Test
    public void testUpdateTimerScheduler() throws Exception {
        String content = "{\"id\":\"1\",\"schedulerName\":\"test456\",\"ruleDatabaseId\":\"1\",\"periodParams\":\"0 0 * * * ？\"," +
                "\"parsingSql\":\"select 1 from t_account\"," +
                "\"userId\":" + this.userId + ",\"brokerId\":" + this.brokerIdMap.get("brokerId")+"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/timerScheduler/update").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
    }

    @Test
    public void testDeleteTimerScheduler() throws Exception {
        String content = "{\"id\":\"1\",\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/timerScheduler/delete").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().intValue(), 200);
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
