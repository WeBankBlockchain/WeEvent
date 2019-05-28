package com.webank.governance.junit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.webank.governance.JUnitTestBase;

public class RegisterControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
	mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testGetUserId() throws Exception {
	mockMvc.perform(get("/user/getUserId?username=zjy142214").contentType(MediaType.APPLICATION_JSON_UTF8))
		.andExpect(status().isOk()).andExpect(jsonPath("$.data").value(4));

    }

    @Test
    public void testForgetPassword() throws Exception {
	mockMvc.perform(get("/user/forget?username=zjy142214").contentType(MediaType.APPLICATION_JSON_UTF8))
		.andExpect(status().isOk()).andExpect(jsonPath("$.status").value(200));

    }

    @Test
    public void testUpdatePassword() throws Exception {
	String content = "{\"id\":\"5\",\"oldPassword\":\"111111\",\"password\":\"123456\"}";
	mockMvc.perform(put("/user/update").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
		.andExpect(status().isOk()).andExpect(jsonPath("$.status").value(400));

    }

    @Test
    public void testRegister() throws Exception {
	String content = "{\"username\":\"zjy02\",\"email\":\"zjy142214@sohu.com\",\"password\":\"123456\"}";
	mockMvc.perform(post("/user/register").contentType(MediaType.APPLICATION_JSON_UTF8).content(content))
		.andExpect(status().isOk());

    }

}
