package com.webank.weevent.governance.junit;

import com.webank.weevent.governance.JUnitTestBase;

import lombok.extern.slf4j.Slf4j;
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
public class HistoricalDataTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;


    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }


    @Test
    public void testHistoricalDataList() throws Exception {
        String content = "{\"groupId\":\"1\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/historicalData/list").contentType(MediaType.APPLICATION_JSON_UTF8).cookie().content(content)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
    }

}
