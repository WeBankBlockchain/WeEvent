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

public class AccountControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testGetUserId() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/user/getUserId?username=zjy05").contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("200"));

    }

    @Test
    public void testForgetPassword() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/forget?username=zjy05").contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("200"));

    }

    @Test
    public void testUpdatePassword() throws Exception {
        String content = "{\"username\":\"zjy05\",\"oldPassword\":\"12345226\",\"password\":\"1232245226\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("200"));

    }

    @Test
    public void testRegister() throws Exception {
        String content = "{\"username\":\"zjy05\",\"email\":\"zjyxxx@sohu.com\",\"password\":\"123456\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("200"));

    }


    @Test
    public void testAccountList() throws Exception {
        String content = "{\"userId\":\"1\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/accountList").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("200"));

    }


}
