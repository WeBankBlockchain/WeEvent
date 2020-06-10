package com.webank.weevent.governance.junit;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.governance.JUnitTestBase;
import com.webank.weevent.governance.common.ErrorCode;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.FileTransportEntity;
import com.webank.weevent.governance.utils.JwtUtils;
import com.webank.weevent.governance.utils.Utils;

import com.fasterxml.jackson.core.type.TypeReference;
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
public class FileControllerTest extends JUnitTestBase {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private String token;

    private Map<String, Integer> brokerIdMap = new ConcurrentHashMap<>();

    private String defaultGroupId = "1";
    private String senderTransport = "com.weevent.sender";
    private String receiverTransport = "com.weevent.receiver";

    @Before
    public void setUp() {
        token = createToken();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
        token = createToken();
        addBroker();
        openTopic();
        openReceiver();
        openSender();
    }

    @After
    public void after() throws Exception {
        closeTopic();
        closeTransport();
        deleteBroker();
    }

    /**
     * test open Receiver transport
     *
     * @throws Exception Exception
     */
    @Test
    public void testReceiverExist() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId")
                + "\",\"groupId\":\"" + this.defaultGroupId
                + "\",\"topicName\":\"" + this.receiverTransport
                + "\",\"role\":\"1\",\"overWrite\":\"0\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/file/openTransport").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Map<String, String> map = JsonHelper.json2Object(result, new TypeReference<Map<String, String>>() {
        });

        Assert.assertEquals(String.valueOf(ErrorCode.TRANSPORT_ALREADY_EXISTS.getCode()), map.get("code"));
    }

    /**
     * test open Sender transport
     *
     * @throws Exception Exception
     */
    @Test
    public void testSenderExist() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId")
                + "\",\"groupId\":\"" + this.defaultGroupId
                + "\",\"topicName\":\"" + this.senderTransport
                + "\",\"role\":\"0\",\"overWrite\":\"1\"}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/file/openTransport").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Map<String, String> map = JsonHelper.json2Object(result, new TypeReference<Map<String, String>>() {
        });

        Assert.assertEquals(String.valueOf(ErrorCode.TRANSPORT_ALREADY_EXISTS.getCode()), map.get("code"));
    }

    /**
     * test list transport
     *
     * @throws Exception Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testListTransport() throws Exception {
        String url = "/file/listTransport?brokerId=" + brokerIdMap.get("brokerId") + "&groupId=" + this.defaultGroupId;

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JsonHelper.json2Object(result, GovernanceResult.class);
        Assert.assertEquals(200, (int) governanceResult.getStatus());
        List<FileTransportEntity> transportList = (List<FileTransportEntity>) governanceResult.getData();
        Assert.assertNotNull(transportList);
        Assert.assertTrue(transportList.size() > 0);
    }

    /**
     * test status
     *
     * @throws Exception Exception
     */
    @Test
    public void testStatus() throws Exception {
        String url = "/file/status?brokerId=" + brokerIdMap.get("brokerId") + "&groupId=" + this.defaultGroupId + "&topicName=" + this.senderTransport + "&role=1";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JsonHelper.json2Object(result, GovernanceResult.class);
        Assert.assertEquals(200, (int) governanceResult.getStatus());
    }

    /**
     * test listFile
     *
     * @throws Exception Exception
     */
    @Test
    public void testListFile() throws Exception {
        String url = "/file/listFile?brokerId=" + brokerIdMap.get("brokerId") + "&groupId=" + this.defaultGroupId + "&topicName=" + this.senderTransport;

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JsonHelper.json2Object(result, GovernanceResult.class);
        Assert.assertEquals(200, (int) governanceResult.getStatus());
    }

    /**
     * test prepareUploadFile
     *
     * @throws Exception Exception
     */
    @Test
    public void testPrepareUploadFile() throws Exception {
        String fileId = UUID.randomUUID().toString().replace("-", "");
        String url = "/file/upload?groupId=" + this.defaultGroupId
                + "&identifier=" + fileId
                + "&topicName=" + this.senderTransport
                + "&totalChunks=" + 10
                + "&totalSize=" + 10485760
                + "&chunkSize=" + 1048576
                + "&filename=test.txt";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JsonHelper.json2Object(result, GovernanceResult.class);
        Assert.assertEquals(200, (int) governanceResult.getStatus());

        String rootPath = System.getProperty("user.dir") + File.separator;
        String filePath = rootPath + "logs" + File.separator + "upload" + File.separator + fileId;
        Assert.assertTrue(Utils.removeLocalFile(filePath));
    }

    private void openTopic() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"topic\":\"" + this.senderTransport + "\",\"userId\":\"1\",\"creater\":\"1\",\"groupId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/openTopic").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);

        content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId") + "\",\"topic\":\"" + this.receiverTransport + "\",\"userId\":\"1\",\"creater\":\"1\",\"groupId\":\"1\"}";
        response = mockMvc.perform(MockMvcRequestBuilders.post("/topic/openTopic").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
    }

    //add broker
    public void addBroker() throws Exception {
        String content = "{\"name\":\"broker2\",\"brokerUrl\":\"" + this.brokerUrl + "\",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/add").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        System.out.println("response:"+ response.getContentAsString());
        GovernanceResult governanceResult = JsonHelper.json2Object(response.getContentAsString(), GovernanceResult.class);
        System.out.println("governanceResult.getData:"+ governanceResult.getData());
        brokerIdMap.put("brokerId", (Integer) governanceResult.getData());
        Assert.assertEquals(governanceResult.getStatus().toString(), "200");
    }

    //delete broker by id
    private void deleteBroker() throws Exception {
        String content = "{\"id\":" + this.brokerIdMap.get("brokerId") + ",\"userId\":\"1\"}";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/broker/delete").contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token).content(content))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        Map jsonObject = JsonHelper.json2Object(response.getContentAsString(), Map.class);
        Assert.assertEquals(jsonObject.get("status").toString(), "200");
    }

    private void closeTransport() throws Exception {
        String contentSender = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId")
                + "\",\"groupId\":\"" + this.defaultGroupId
                + "\",\"topicName\":\"" + this.senderTransport + "\"}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/file/closeTransport").contentType(MediaType.APPLICATION_JSON_UTF8).content(contentSender).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JsonHelper.json2Object(result, GovernanceResult.class);
        Boolean isSuccess = (Boolean) governanceResult.getData();
        Assert.assertTrue(isSuccess);

        String contentReceiver = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId")
                + "\",\"groupId\":\"" + this.defaultGroupId
                + "\",\"topicName\":\"" + this.receiverTransport + "\"}";

        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/file/closeTransport").contentType(MediaType.APPLICATION_JSON_UTF8).content(contentReceiver).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        response = mvcResult.getResponse();
        result = response.getContentAsString();
        Assert.assertNotNull(result);
        governanceResult = JsonHelper.json2Object(result, GovernanceResult.class);
        isSuccess = (Boolean) governanceResult.getData();
        Assert.assertTrue(isSuccess);

    }

    private void closeTopic() throws Exception {
        String url = "/topic/close?brokerId=" + brokerIdMap.get("brokerId") + "&topic=com.weevent.rest&groupId=1";
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_UTF8).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token))
                .andReturn().getResponse();
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_OK);
        String contentAsString = response.getContentAsString();
        Assert.assertEquals(Boolean.valueOf(contentAsString), true);
    }

    private void openSender() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId")
                + "\",\"groupId\":\"" + this.defaultGroupId
                + "\",\"topicName\":\"" + this.senderTransport
                + "\",\"role\":\"0\",\"overWrite\":\"1\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/file/openTransport").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JsonHelper.json2Object(result, GovernanceResult.class);
        Boolean isSuccess = (Boolean) governanceResult.getData();
        Assert.assertTrue(isSuccess);
    }

    private void openReceiver() throws Exception {
        String content = "{\"brokerId\":\"" + this.brokerIdMap.get("brokerId")
                + "\",\"groupId\":\"" + this.defaultGroupId
                + "\",\"topicName\":\"" + this.receiverTransport
                + "\",\"role\":\"1\",\"overWrite\":\"0\"}";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/file/openTransport").contentType(MediaType.APPLICATION_JSON_UTF8).content(content).header(JwtUtils.AUTHORIZATION_HEADER_PREFIX, token)).andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        String result = response.getContentAsString();
        Assert.assertNotNull(result);
        GovernanceResult governanceResult = JsonHelper.json2Object(result, GovernanceResult.class);
        Boolean isSuccess = (Boolean) governanceResult.getData();
        Assert.assertTrue(isSuccess);
    }
}
