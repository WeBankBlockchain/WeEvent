package com.webank.weevent.governance.junit;

import java.security.Security;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.client.BrokerException;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
public class AccountControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private UserDetailsService userDetailsService;

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
    public void testCheckData() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/user/check/test/1").contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }

    @Test
    public void testLoadUserByUsername() throws Exception {
        UserDetails details = userDetailsService.loadUserByUsername("zjy05");
        Assert.assertNotNull(details);
    }

    @Test
    public void testRegisterException001() throws Exception {
        String content = "{\"username\":\"zjy05\",\"email\":\"admin@test.com\",\"password\":\"123456\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testRegisterException002() throws Exception {
        String content = "{\"username\":\"zjy05\",\"email\":\"admin@test.com\",\"password\":\"12346\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testRegisterException003() throws Exception {
        String content = "{\"username\":\"\",\"email\":\"admin@test.com\",\"password\":\"123456\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testRegisterException004() throws Exception {
        String content = "{\"username\":\"test\",\"email\":\"admin@test.com\",\"password\":\"\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }


    @Test
    public void testAccountList() throws Exception {
        String token = createToken();
        Security.setProperty(token, "1");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/accountList").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Assert.assertTrue(response.getContentAsString().contains("200"));
    }

    @Test
    public void testAuthRequire() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/user/require").contentType(MediaType.APPLICATION_JSON_UTF8)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(jsonObject.get("code").toString(), "302000");
    }


    @Test
    public void testUpdatePassword() throws Exception {
        String content = "{\"username\":\"zjy05\",\"oldPassword\":\"123456\",\"password\":\"1232245226\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }

    @Test
    public void testResetPassword() throws Exception {
        String content = "{\"username\":\"zjy05\",\"password\":\"123456\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/reset").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
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
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testUpdatePasswordException002() throws Exception {
        String content = "{\"username\":\"zjy05\",\"oldPassword\":\"123456qqq\",\"password\":\"\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }

    @Test
    public void testUpdatePasswordException003() throws Exception {
        String content = "{\"username\":\"zjy05\",\"oldPassword\":\"\",\"password\":\"\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/user/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        Assert.assertEquals("400", governanceResult.getStatus().toString());
    }


    public void testDelete() throws Exception {
        String content = "{\"username\":\"zjy05\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/user/delete").content(content).contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertNotNull(jsonObject);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }

    @After
    public void after() throws Exception {
        testDelete();
    }


}
