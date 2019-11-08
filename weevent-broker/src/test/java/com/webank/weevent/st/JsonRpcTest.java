package com.webank.weevent.st;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

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
    private String eventId;
    private Map<String, String> extension = new HashMap<>();
    private String content = "Hello json rpc";

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        String url = "http://localhost:" + listenPort + "/weevent/jsonrpc";

        JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(url));
        this.iBrokerRpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
        this.eventId = iBrokerRpc.publish(this.jsonTopic, this.content.getBytes()).getEventId();

        client.setExceptionResolver(response -> {
            log.error("Exception in json rpc invoke, {}", response.toString());
            JsonNode error = response.get("error");
            return new BrokerException(error.get("code").intValue(), error.get("message").textValue());
        });
    }

    @Test
    public void testOpenNoGroupId() throws BrokerException {
        boolean open = iBrokerRpc.open(this.jsonTopic);
        log.info("open topic : " + open);
        Assert.assertTrue(open);
    }

    @Test
    public void testOpenWithGroupId() throws BrokerException {
        boolean open = iBrokerRpc.open(this.jsonTopic, this.groupId);
        log.info("open topic : " + open);
        Assert.assertTrue(open);
    }

    @Test
    public void testCloseNoGroupId() throws BrokerException {
        boolean open = iBrokerRpc.close(this.jsonTopic);
        log.info("close topic : " + open);
        Assert.assertTrue(open);
    }

    @Test
    public void testCloseWithGroupId() throws BrokerException {
        boolean open = iBrokerRpc.close(this.jsonTopic, this.groupId);
        log.info("close topic : " + open);
        Assert.assertTrue(open);
    }

    @Test
    public void testExistNoGroupId() throws BrokerException {
        boolean exist = iBrokerRpc.exist(this.jsonTopic);
        log.info("topic exist : " + exist);
        Assert.assertTrue(exist);
    }

    @Test
    public void testExistWithGroupId() throws BrokerException {
        boolean exist = iBrokerRpc.exist(this.jsonTopic, this.groupId);
        log.info("topic exist : " + exist);
        Assert.assertTrue(exist);
    }

    @Test
    public void testStateNoGroupId() throws BrokerException {
        TopicInfo state = iBrokerRpc.state(this.jsonTopic);
        log.info("state : " + state);
        Assert.assertNotNull(state.getCreatedTimestamp());
    }

    @Test
    public void testStateWithGroupId() throws BrokerException {
        TopicInfo state = iBrokerRpc.state(this.jsonTopic, this.groupId);
        log.info("state : " + state);
        Assert.assertNotNull(state.getCreatedTimestamp());
    }

    @Test
    public void testListNoGroupId() throws BrokerException {
        TopicPage list = iBrokerRpc.list(0, 10);
        log.info("list topic : " + list);
        Assert.assertTrue(list.getTotal() > 0);
    }

    @Test
    public void testListWithGroupId() throws BrokerException {
        TopicPage list = iBrokerRpc.list(0, 10, this.groupId);
        log.info("list topic : " + list);
        Assert.assertTrue(list.getTotal() > 0);
    }

    @Test
    public void testPublishNoGroupIdExt() throws BrokerException {
        SendResult publish = iBrokerRpc.publish(this.jsonTopic, "Hello World!".getBytes());
        log.info("publish: " + publish);
        Assert.assertNotNull(publish.getEventId());
    }

    @Test
    public void testPublishWithGroupIdNoExt() throws BrokerException {
        SendResult publish = iBrokerRpc.publish(this.jsonTopic, this.groupId, this.content.getBytes());
        log.info("publish: " + publish);
        Assert.assertNotNull(publish.getEventId());
    }

    @Test
    public void testPublishNoGroupId() throws BrokerException {
        Map<String, String> ext = new HashMap<>();
        ext.put("weevent-jsonrpctest1", "json rpc ext value1");
        ext.put("weevent-jsonrpctest2", "json rpc ext value2");
        SendResult publish = iBrokerRpc.publish(this.jsonTopic, this.content.getBytes(), ext);
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
        WeEvent event = iBrokerRpc.getEvent(this.eventId);
        log.info("getEvent : " + event);
        Assert.assertEquals(this.content, new String(event.getContent()));
    }

    @Test
    public void testGetEventWithGroupId() throws BrokerException {
        WeEvent event = iBrokerRpc.getEvent(this.eventId, this.groupId);
        log.info("getEvent : " + event);
        Assert.assertEquals(this.content, new String(event.getContent()));
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
