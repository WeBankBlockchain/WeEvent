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

public class BrokerControllerTest extends JUnitTestBase{
	
	@Autowired
	private WebApplicationContext wac;
	
	private MockMvc mockMvc;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
	}
	
	@Test
	public void testAddBroker() throws Exception {
		String content = "{\"name\":\"broker2\",\"brokerUrl\":\"http://192.168.58.138:8080\",\"userId\":\"4\"}";
		mockMvc.perform(post("/broker")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(content))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));
				
	}
	
	@Test
	public void testUpdateBroker() throws Exception {
		String content = "{\"id\":\"2\",\"name\":\"broker1\",\"brokerUrl\":\"http://192.168.58.139:8080\",\"userId\":\"4\"}";
		mockMvc.perform(put("/broker")
				.contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(content))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));
			
	}
	
	@Test
	public void testDeleteBroker() throws Exception {
		mockMvc.perform(delete("/broker/3")
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));
			
	}
	
	@Test
	public void testGetBroker() throws Exception {
		mockMvc.perform(get("/broker/2")
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2));
			
	}
	
	@Test
	public void testGetBrokers() throws Exception {
		mockMvc.perform(get("/broker/list?username=zjy142214")
				.contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
			
	}
}
