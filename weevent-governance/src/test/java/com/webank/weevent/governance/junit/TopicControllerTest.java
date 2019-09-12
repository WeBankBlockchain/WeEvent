package com.webank.weevent.governance.junit;

import java.util.List;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.entity.TopicEntity;
import com.webank.weevent.governance.entity.TopicPage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
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

public class TopicControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;


    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testTopicClose() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/close?brokerId=1&topic=com.weevent.rest1121").contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }

    @Test
    public void testTopicList() throws Exception {
        String content = "{\"brokerId\":\"1\",\"pageSize\":\"10\",\"pageIndex\":\"0\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/topic/list").contentType(MediaType.APPLICATION_JSON_UTF8).content(content)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        TopicPage topicPage = JSON.parseObject(result, TopicPage.class);
        List<TopicEntity> topicInfoList = topicPage.getTopicInfoList();
        Assert.assertTrue(CollectionUtils.isNotEmpty(topicInfoList));
    }

    @Test
    public void testTopicOpen() throws Exception {
        String content = "{\"brokerId\":\"1\",\"topic\":\"com.weevent.rest1121\",\"userId\":\"1\",\"creater\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/openTopic").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }

    @Test
    public void testTopicInfo() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/topicInfo?brokerId=1&topic=com.weevent.rest").contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertNotNull(response.getContentAsString());
        TopicEntity topicEntity = JSONObject.parseObject(response.getContentAsString(), TopicEntity.class);
        Assert.assertEquals(topicEntity.getTopicName(), "com.weevent.rest");
    }
}
