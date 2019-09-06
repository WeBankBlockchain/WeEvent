package com.webank.weevent.governance.junit;

import com.webank.weevent.governance.JUnitTestBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testTopicClose() throws Exception {
        String content = "{\"brokerId\":\"1\",\"topic\":\"com.weevent.rest\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/close").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
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
        JSONObject jsonObject = JSON.parseObject(result);
        Assert.assertNotNull(jsonObject.get("status"));
        //Assert.assertTrue(Integer.valueOf(jsonObject.get("status").toString()).intValue(),HttpStatus.SC_OK);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }

    @Test
    public void testTopicOpen() throws Exception {
        String content = "{\"brokerId\":\"1\",\"topic\":\"com.weevent.rest1121\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/openTopic").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }
}
