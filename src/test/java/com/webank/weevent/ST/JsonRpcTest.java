package com.webank.weevent.ST;

import java.net.URL;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonRpcTest {
    private IBrokerRpc iBrokerRpc;

    @Before
    public void before() throws Exception {
        String url = "http://127.0.0.1:8080/weevent/jsonrpc";

        JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(url));
        this.iBrokerRpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
    }

    @Test
    public void testOpenTopic() throws BrokerException {
        String topic = "com.weevent.test.jsonrpc";
        boolean open = iBrokerRpc.open(topic);
        System.out.println("open topic : " + open);
        Assert.assertTrue(open);
    }

    @Test
    public void testCloseTopic() throws BrokerException {
        String topic = "com.weevent.test.jsonrpc";
        boolean open = iBrokerRpc.close(topic);
        System.out.println("close topic : " + open);
        Assert.assertTrue(open);
    }

    @Test
    public void testList() throws BrokerException {
        TopicPage list = iBrokerRpc.list(0, 10);
        System.out.println("list topic : " + list);
        Assert.assertTrue(list.getTotal() > 0);
    }

    @Test
    public void testState() throws BrokerException {
        String topic = "com.weevent.test.jsonrpc";
        TopicInfo state = iBrokerRpc.state(topic);
        System.out.println("state : " + state);
        Assert.assertNotNull(state.getTopicAddress());
    }

    @Test
    public void testPublish() throws BrokerException {
        String topic = "com.weevent.test.jsonrpc";
        SendResult publish = iBrokerRpc.publish(topic, "Hello World!".getBytes());
        System.out.println(publish);
        Assert.assertNotNull(publish.getEventId());
    }

    @Test
    public void testPublishContentequal10K() throws BrokerException {
        String str = get10KStr();
        String topic = "com.weevent.test.jsonrpc";
        SendResult publish = iBrokerRpc.publish(topic, str.getBytes());
        System.out.println(publish);
        Assert.assertNotNull(publish.getEventId());
    }

    @Test
    public void testPublishContentgt10K() throws BrokerException {
        String str = get10KStr() + "s";
        String topic = "com.weevent.test.jsonrpc";
        SendResult publish = iBrokerRpc.publish(topic, str.getBytes());
        System.out.println(publish);
    }

    @Test
    public void testSubscribe() throws BrokerException {
        String subscriptionId = iBrokerRpc.subscribe("com.weevent.test.jsonrpc",
                "",
                "http://127.0.0.1:8081/mock/rest/onEvent");
        System.out.println(subscriptionId);
    }

    @Test
    public void testReSubscribe() throws BrokerException {
        String subscriptionId = iBrokerRpc.subscribe("com.weevent.test.jsonrpc",
                "48d607c8-1ea9-4e7a-8b69-e1d01e801d1d",
                "http://127.0.0.1:8081/mock/rest/onEvent");
        System.out.println(subscriptionId);
    }

    @Test
    public void testUnSubscribe() throws BrokerException {
        String subscriptionId = "48d607c8-1ea9-4e7a-8b69-e1d01e801d1d";
        boolean unSubscribe = iBrokerRpc.unSubscribe(subscriptionId);
        System.out.println(unSubscribe);
    }

    //get 10K string
    private String get10KStr() {
        StringBuilder result = new StringBuilder("");
        for (int i = 0; i < 1024; i++) {
            result = result.append("abcdefghij");
        }
        return result.toString();
    }

}
