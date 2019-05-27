package com.webank.governance.junit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.webank.weevent.governance.GovernanceApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GovernanceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RegisterControllerTest {
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}
	
	@Test
	public void testForgetPassword() throws Exception {
		mockMvc.perform(get("/user/forget?username=zjy142214")
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200));
				
	}
	
	@Test
	public void testUpdatePassword() throws Exception {
		String content = "{\"id\":\"5\",\"oldPassword\":\"111111\",\"password\":\"123456\"}";
		mockMvc.perform(put("/user/update")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(content))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(400));
				
	}
	
	@Test
	public void testRegister() throws Exception {
		String content = "{\"username\":\"zjy02\",\"email\":\"zjy142214@sohu.com\",\"password\":\"123456\"}";
		mockMvc.perform(post("/user/register")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(content))
				.andExpect(status().isOk());
				
	}
	
}
