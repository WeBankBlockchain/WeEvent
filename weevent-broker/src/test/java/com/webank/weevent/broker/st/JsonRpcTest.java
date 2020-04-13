package com.webank.weevent.broker.st;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.broker.JUnitTestBase;
import com.webank.weevent.client.BaseResponse;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.TopicInfo;
import com.webank.weevent.client.TopicPage;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.client.jsonrpc.IBrokerRpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class JsonRpcTest extends JUnitTestBase {
    private IBrokerRpc iBrokerRpc;
    private String jsonTopic = "com.weevent.test";
    private String testTopic = "com.weevent.testTopic";
    private String eventId;
    private Map<String, String> extension = new HashMap<>();
    private String content = "Hello json rpc";

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        String url = "http://localhost:7000/weevent-broker/jsonrpc";

        JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(url));
        this.iBrokerRpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
        iBrokerRpc.open(this.jsonTopic, "");
        iBrokerRpc.open(this.jsonTopic, this.groupId);
        this.eventId = iBrokerRpc.publish(this.jsonTopic, this.groupId, this.content.getBytes(), new HashMap<>()).getEventId();

        client.setExceptionResolver(response -> {
            log.error("Exception in json rpc invoke, {}", response.toString());
            JsonNode error = response.get("error");
            return new BrokerException(error.get("code").intValue(), error.get("message").textValue());
        });
    }

    @Test
    public void testOpenNoGroupId() throws BrokerException {
        BaseResponse<Boolean> baseResponse = iBrokerRpc.open(this.jsonTopic, "");
        log.info("open topic : " + baseResponse);
        Assert.assertTrue(baseResponse.getData());
    }

    @Test
    public void testOpenWithGroupId() throws BrokerException {
        BaseResponse<Boolean> baseResponse = iBrokerRpc.open(this.jsonTopic, this.groupId);
        log.info("open topic : " + baseResponse);
        Assert.assertTrue(baseResponse.getData());
    }

    @Test
    public void testCloseNoGroupId() throws BrokerException {
        BaseResponse<Boolean> baseResponse = iBrokerRpc.close(this.jsonTopic, "");
        log.info("close topic : " + baseResponse);
        Assert.assertTrue(baseResponse.getData());
    }

    @Test
    public void testCloseWithGroupId() throws BrokerException {
        BaseResponse<Boolean> baseResponse = iBrokerRpc.close(this.jsonTopic, this.groupId);
        log.info("close topic : " + baseResponse);
        Assert.assertTrue(baseResponse.getData());
    }

    @Test
    public void testExistNoGroupId() throws BrokerException {
        BaseResponse<Boolean> baseResponse = iBrokerRpc.exist(this.jsonTopic, "");
        log.info("topic exist : " + baseResponse);
        Assert.assertTrue(baseResponse.getData());
    }

    @Test
    public void testExistWithGroupId() throws BrokerException {
        BaseResponse<Boolean> baseResponse = iBrokerRpc.exist(this.jsonTopic, this.groupId);
        log.info("topic exist : " + baseResponse);
        Assert.assertTrue(baseResponse.getData());
    }

    @Test
    public void testStateNoGroupId() throws BrokerException {
        BaseResponse<TopicInfo> baseResponse = iBrokerRpc.state(this.jsonTopic, "");
        log.info("state : " + baseResponse);
        Assert.assertNotNull(baseResponse.getData().getCreatedTimestamp());
    }

    @Test
    public void testStateWithGroupId() throws BrokerException {
        BaseResponse<TopicInfo> baseResponse = iBrokerRpc.state(this.jsonTopic, this.groupId);
        log.info("state : " + baseResponse);
        Assert.assertNotNull(baseResponse.getData().getCreatedTimestamp());
    }

    @Test
    public void testListNoGroupId() throws BrokerException {
        BaseResponse<TopicPage> baseResponse = iBrokerRpc.list(0, 10, "");
        log.info("list topic : " + baseResponse);
        Assert.assertTrue(baseResponse.getData().getTotal() > 0);
    }

    @Test
    public void testListWithGroupId() throws BrokerException {
        BaseResponse<TopicPage> baseResponse = iBrokerRpc.list(0, 10, this.groupId);
        log.info("list topic : " + baseResponse);
        Assert.assertTrue(baseResponse.getData().getTotal() > 0);
    }

    @Test
    public void testPublishNoGroupIdExt() throws BrokerException {
        SendResult publish = iBrokerRpc.publish(this.jsonTopic, "", "Hello World!".getBytes(), new HashMap<>());
        log.info("publish: " + publish);
        Assert.assertNotNull(publish.getEventId());
    }

    @Test
    public void testPublishWithGroupIdNoExt() throws BrokerException {
        Map<String, String> ext = new HashMap<>();
        ext.put("weevent-jsonrpctest1", "json rpc ext value1");
        SendResult publish = iBrokerRpc.publish(this.jsonTopic, this.groupId, this.content.getBytes(), ext);
        log.info("publish: " + publish);
        Assert.assertNotNull(publish.getEventId());
    }

    @Test
    public void testPublishNoGroupId() throws BrokerException {
        Map<String, String> ext = new HashMap<>();
        ext.put("weevent-jsonrpctest1", "json rpc ext value1");
        ext.put("weevent-jsonrpctest2", "json rpc ext value2");
        SendResult publish = iBrokerRpc.publish(this.jsonTopic, "", this.content.getBytes(), new HashMap<>());
        log.info("publish: " + publish);
        Assert.assertNotNull(publish.getEventId());
    }

    @Test
    public void testPublishWithGroupId() throws BrokerException {
        Map<String, String> ext = new HashMap<>();
        ext.put("weevent-jsonrpctest1", "json rpc ext value1");
        ext.put("weevent-jsonrpctest2", "json rpc ext value2");
        SendResult publish = iBrokerRpc.publish(this.jsonTopic, this.groupId, this.content.getBytes(), ext);
        log.info("publish: " + publish);
        Assert.assertNotNull(publish.getEventId());
    }

    @Test
    public void testPublishContentEq10K() throws BrokerException {
        String str = get10KStr();
        SendResult publish = iBrokerRpc.publish(this.jsonTopic, this.groupId, str.getBytes(), extension);
        log.info("publish: " + publish);
        Assert.assertNotNull(publish.getEventId());
    }

    @Test
    public void testPublishContentGt10K() {
        String str = get10KStr() + "s";
        try {
            SendResult publish = iBrokerRpc.publish(this.jsonTopic, this.groupId, str.getBytes(), extension);
            Assert.assertNull(publish);
        } catch (BrokerException e) {
            log.info(e.getMessage());
            Assert.assertEquals(ErrorCode.EVENT_CONTENT_EXCEEDS_MAX_LENGTH.getCodeDesc(), e.getMessage());
        }
    }

    @Test
    public void testPublishExtEq1K() {
        Map<String, String> ext = get1KMap();
        try {
            SendResult publish = iBrokerRpc.publish(this.jsonTopic, this.groupId, this.content.getBytes(), ext);
            log.info("publish: " + publish);
            Assert.assertNotNull(publish.getEventId());
        } catch (BrokerException e) {
            log.info(e.getMessage());
            Assert.assertEquals(ErrorCode.EVENT_EXTENSIONS_EXCEEDS_MAX_LENGTH.getCodeDesc(), e.getMessage());
        }
    }

    @Test
    public void testPublishExtGt1K() {
        Map<String, String> ext = get1KMap();
        ext.put("weevent-key2", "value2");
        try {
            SendResult publish = iBrokerRpc.publish(this.jsonTopic, this.groupId, this.content.getBytes(), ext);
            Assert.assertNull(publish);
        } catch (BrokerException e) {
            log.info(e.getMessage());
            Assert.assertEquals(ErrorCode.EVENT_EXTENSIONS_EXCEEDS_MAX_LENGTH.getCodeDesc(), e.getMessage());
        }
    }

    @Test
    public void testGetEventNoGroupId() throws BrokerException {
        BaseResponse<WeEvent> baseResponse = iBrokerRpc.getEvent(this.eventId, "");
        log.info("getEvent : " + baseResponse);
        Assert.assertEquals(this.content, new String(baseResponse.getData().getContent()));
    }

    @Test
    public void testGetEventWithGroupId() throws BrokerException {
        BaseResponse<WeEvent> baseResponse = iBrokerRpc.getEvent(this.eventId, this.groupId);
        log.info("getEvent : " + baseResponse);
        Assert.assertEquals(this.content, new String(baseResponse.getData().getContent()));
    }

    // get 10K string
    private String get10KStr() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            result.append("abcdefghij");
        }
        return result.toString();
    }

    // get 1K Map
    private Map<String, String> get1KMap() {
        Map<String, String> map = new HashMap<>();

        StringBuilder valueStr = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            valueStr.append("abcdefghij");
        }
        valueStr.append("abcdefd");
        map.put("weevent-key1", valueStr.toString());
        return map;
    }

}
