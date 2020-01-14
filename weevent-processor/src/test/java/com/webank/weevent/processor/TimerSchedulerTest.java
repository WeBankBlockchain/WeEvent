package com.webank.weevent.processor;


import java.util.Date;

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
    private String jdbcUrl = "jdbc:h2:~/WeEvent_governance?user=root&password=123456";
    private Long timePeriod = 1L;
    private long delay = 1000;
    private String parsingSql = "select *　from timer_scheduler_job";
    private String periodParams="{\"hour\":\"17\",\"minute\":\"10\"}";

    @Autowired
    protected WebApplicationContext wac;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        timerScheduler = new TimerScheduler("test", jdbcUrl, timePeriod, periodParams,delay,parsingSql);
    }

    @Test
    public void testInsertNormal() throws Exception {
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void testInsertException() throws Exception {
        timerScheduler = new TimerScheduler("test", null, timePeriod, periodParams,delay,parsingSql);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timePeriod));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }


    @Test
    public void testFindAllNormal() throws Exception {
        url = "/timerScheduler/list";
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());

    }

    @Test
    public void testUpdateNormal() throws Exception {
        testInsertNormal();
        timePeriod = 10L;
        url = "/timerScheduler/update";
        timerScheduler.setTimePeriod(timePeriod);
        timerScheduler.setId(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());

    }

    @Test
    public void testUpdateException() throws Exception {
        testInsertNormal();
        timePeriod = 10L;
        url = "/timerScheduler/update";
        timerScheduler.setTimePeriod(null);
        timerScheduler.setId(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timePeriod));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(400, result.getResponse().getStatus());
    }


    @Test
    public void testDeleteNormal() throws Exception {
        url = "/timerScheduler/delete";
        timerScheduler = new TimerScheduler();
        timerScheduler.setId(1);
        RequestBuilder requestBuilder3 = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(JsonUtil.toJSONString(timerScheduler));
        MvcResult result = mockMvc.perform(requestBuilder3).andDo(print()).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }
}
