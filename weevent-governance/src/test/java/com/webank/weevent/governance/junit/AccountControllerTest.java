package com.webank.weevent.governance.junit;

import java.util.Map;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.result.GovernanceResult;
import com.webank.weevent.governance.utils.JsonUtil;

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
public class AccountControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;


    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
        testRegister();
    }


    public void testRegister() throws Exception {
        String content = "{\"username\":\"zjy05\",\"email\":\"admin@test.com\",\"password\":\"123456\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("200"));

    }

    @Test
    public void testRegisterException001() throws Exception {

        String content = "{\"username\":\"zjy05\",\"email\":\"admin@test.com\",\"password\":\"123456\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testRegisterException002() throws Exception {
        String content = "{\"username\":\"zjy05\",\"email\":\"admin@test.com\",\"password\":\"12346\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testRegisterException003() throws Exception {
        String content = "{\"username\":\"\",\"email\":\"admin@test.com\",\"password\":\"123456\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testRegisterException004() throws Exception {
        String content = "{\"username\":\"test\",\"email\":\"admin@test.com\",\"password\":\"\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }


    @Test
    public void testAccountList() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/accountList").contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("200"));

    }

    @Test
    public void testGetUserId() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/user/getUserId?username=zjy05").contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonUtil.parseObject(response.getContentAsString(), Map.class);
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }


    @Test
    public void testUpdatePassword() throws Exception {
        String content = "{\"username\":\"zjy05\",\"oldPassword\":\"123456\",\"password\":\"1232245226\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonUtil.parseObject(response.getContentAsString(), Map.class);
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");

    }

    @Test
    public void testUpdatePasswordException001() throws Exception {
        String content = "{\"username\":\"zjy05\",\"oldPassword\":\"123456qqq\",\"password\":\"1232245222226\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testUpdatePasswordException002() throws Exception {
        String content = "{\"username\":\"zjy05\",\"oldPassword\":\"123456qqq\",\"password\":\"\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testUpdatePasswordException003() throws Exception {
        String content = "{\"username\":\"zjy05\",\"oldPassword\":\"\",\"password\":\"\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonUtil.parseObject(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }


    public void testDelete() throws Exception {
        String content = "{\"username\":\"zjy05\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/user/delete").content(content).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonUtil.parseObject(response.getContentAsString(), Map.class);
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }

    @After
    public void after() throws Exception {
        testDelete();
    }


}
