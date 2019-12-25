package com.webank.weevent.governance.junit;

import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.properties.ConstantProperties;
import com.webank.weevent.governance.utils.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
public class ForwardControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private Cookie cookie;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        this.cookie = new Cookie(ConstantProperties.COOKIE_MGR_ACCOUNT_ID, "1");

    }

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }


    @Test
    public void testGroupList() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.get("/weevent/" + "admin" + "/" + "listGroup").contentType(MediaType.APPLICATION_JSON_UTF8).cookie(this.cookie))
                .andReturn().getResponse();
        String contentAsString = response.getContentAsString();
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertNotNull(contentAsString);
        Map jsonObject = JsonUtil.parseObject(response.getContentAsString(),Map.class);
        Object code = jsonObject.get("code");
        Object data = jsonObject.get("data");
        Assert.assertEquals(0, code);
        Assert.assertTrue(data instanceof List);
    }

}
