package com.webank.weevent.sample;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

public class JsonRPC {
    private final static String groupId = "1";
    private final static Map<String, String> extensions = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("This is WeEvent json rpc sample.");
        try {
            URL remote = new URL("http://localhost:8080/weevent/jsonrpc");
            // init jsonrpc client
            JsonRpcHttpClient client = new JsonRpcHttpClient(remote);
            // init IBrokerRpc object
            IBrokerRpc rpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
            // open topic
            rpc.open("com.weevent.test", groupId);
            // publish event
            SendResult sendResult = rpc.publish("com.weevent.test", groupId, "hello weevent".getBytes(StandardCharsets.UTF_8), extensions);
            System.out.println(sendResult.getStatus());
        } catch (MalformedURLException | BrokerException e) {
            e.printStackTrace();
        }
    }
}
