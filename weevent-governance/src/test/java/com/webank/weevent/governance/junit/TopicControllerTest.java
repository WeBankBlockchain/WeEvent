package com.webank.weevent.governance.junit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.TopicEntity;
import com.webank.weevent.governance.entity.TopicPage;
import com.webank.weevent.governance.utils.JwtUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
public class TopicControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private String token;


    private Map<String, Integer> brokerIdMap = new ConcurrentHashMap<>();

    @Before
    public void setUp() {
        token = createToken();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
        token = createToken();
        addBroker();
        testTopicOpen();
    }

    //add broker
    public void addBroker() throws Exception {
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"" + this.brokerUrl + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        brokerIdMap.put("brokerId", (Integer) governanceResult.getData());
        Assert.assertEquals(governanceResult.getStatus().toString(), "200");
    }


    public void testTopicOpen() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"topic\":\"com.weevent.rest\",\"userId\":\"1\",\"creater\":\"1\",\"groupId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/openTopic").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }

    @Test
    public void testTopicOpenException() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"topic\":\"com.weevent.rest\",\"userId\":\"1\",\"creater\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/openTopic").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals(governanceResult.getStatus().toString(), "100109");
    }

    @Test
    public void testTopicList() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"pageSize\":\"10\",\"pageIndex\":\"0\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/topic/list").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        TopicPage topicPage = JsonHelper.json2Object(result, TopicPage.class);
        List<TopicEntity> topicInfoList = topicPage.getTopicInfoList();
        Assert.assertTrue(CollectionUtils.isNotEmpty(topicInfoList));
    }

    @Test
    public void testTopicInfo() throws Exception {
        String url = "/topic/topicInfo?brokerId=" + brokerIdMap.get("brokerId") + "&topic=com.weevent.rest&groupId=1";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertNotNull(response.getContentAsString());
        TopicEntity topicEntity = JsonHelper.json2Object(response.getContentAsString(), TopicEntity.class);
        Assert.assertEquals(topicEntity.getTopicName(), "com.weevent.rest");
    }


    @Test
    public void testTopicClose() throws Exception {
        String url = "/topic/close?brokerId=" + brokerIdMap.get("brokerId") + "&topic=com.weevent.rest&groupId=1";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertEquals(Boolean.valueOf(contentAsString), true);
    }

    @Test
    public void testGetTopicInfo() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"topic\":\"com.weevent.rest\",\"groupId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/openTopic").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertNotNull(response.getContentAsString());
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertEquals(jsonObject.get("status").toString(), "100109");
    }

    @Test
    public void testDestinationList() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/destinationList").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertNotNull(response.getContentAsString());
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertEquals(jsonObject.get("code").toString(), "100107");    // PageSize{1-100}
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
        testTopicClose();
        deleteBroker();
    }
}
