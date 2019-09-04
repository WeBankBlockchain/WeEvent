package com.webank.weevent.governance.junit;

import com.webank.weevent.governance.JUnitTestBase;

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

public class BrokerControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testAddBroker() throws Exception {
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"http://127.0.0.1:7090/weevent\",\"webaseUrl\":\"http://127.0.0.1:7090/weevent\",\"userId\":\"4\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }

    @Test
    public void testUpdateBroker() throws Exception {
        String content = "{\"id\":\"2\",\"name\":\"broker1\",\"brokerUrl\":\"http://127.0.0.1:8080\",\"webaseUrl\":\"http://127.0.0.1:8080/webase-node-mgr\",\"userId\":\"4\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.put("/broker").contentType(MediaType.APPLICATION_JSON_UTF8).content(content)).andReturn().getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));

    }

    @Test
    public void testDeleteBroker() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.delete("/broker/3").contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("true"));
    }

    @Test
    public void testGetBroker() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/broker/2").contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertNotNull(mvcResult.getModelAndView());
        Assert.assertNotNull(mvcResult.getModelAndView().getModel());
        Assert.assertEquals(mvcResult.getModelAndView().getModel().get("id").toString(), "2");
    }

    @Test
    public void testGetBrokers() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/broker/list?userId=1").contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        /*String content = response.getContentAsString();
        List<Object> list = Arrays.asList(content);
        List<BrokerEntity> brokerEntities = (List<BrokerEntity> )list;
        Assert.assertNotNull(mvcResult.getModelAndView());
        Assert.assertNotNull(mvcResult.getModelAndView().getModel());
        Assert.assertEquals(mvcResult.getModelAndView().getModel().get("userId").toString(),"1");*/
    }
}
