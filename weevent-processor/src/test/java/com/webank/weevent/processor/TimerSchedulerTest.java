package com.webank.weevent.processor;


import com.webank.weevent.processor.model.TimerScheduler;
import com.webank.weevent.processor.utils.JsonUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TimerSchedulerTest {

    private MockMvc mockMvc;
    private TimerScheduler timerScheduler;
    private String url = "/timerScheduler/insert";
    private String jdbcUrl = "jdbc:h2:~/WeEvent_processor1?user=root&password=123456";
    private String parsingSql = "select count(1)　from QRTZ_JOB_DETAILS";
    private String periodParams = "*/5 * * * * ?";

    @Autowired
    protected WebApplicationContext wac;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        timerScheduler = new TimerScheduler("1","test", jdbcUrl, periodParams, parsingSql);
        url = "/timerScheduler/insert";
    }

    @Test
    public void testInsertNormal001() throws Exception {
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void testInsertNormal002() throws Exception {
        timerScheduler.setSchedulerName("test2");
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void testInsertException001() throws Exception {
        timerScheduler.setPeriodParams(null);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    public void testInsertException002() throws Exception {
        timerScheduler.setSchedulerName(null);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    public void testUpdateNormal001() throws Exception {
        timerScheduler.setSchedulerName("test111");
        testInsertNormal001();
        url = "/timerScheduler/update";
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void testUpdateNormal002() throws Exception {
        timerScheduler.setSchedulerName("test2");
        testInsertNormal001();
        url = "/timerScheduler/update";
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }


    @Test
    public void testUpdateException001() throws Exception {
        timerScheduler.setSchedulerName(null);
        url = "/timerScheduler/update";
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    @Test
    public void testUpdateException002() throws Exception {
        timerScheduler.setPeriodParams(null);
        url = "/timerScheduler/update";
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }

    public void testDeleteNormal() throws Exception {
        url = "/timerScheduler/delete";
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }
}
