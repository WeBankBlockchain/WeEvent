package com.webank.weevent.sample;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

public class JsonRPC {
    public static void main(String[] args) {
        System.out.println("This is WeEvent json rpc sample.");
        try {
            URL remote = new URL("http://localhost:7000/weevent/jsonrpc");
            // init json rpc client
            JsonRpcHttpClient client = new JsonRpcHttpClient(remote);
            // init IBrokerRpc object
            IBrokerRpc rpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);

            // ensure topic
            rpc.open("com.weevent.test", WeEvent.DEFAULT_GROUP_ID);

            // publish event
            SendResult sendResult = rpc.publish("com.weevent.test", WeEvent.DEFAULT_GROUP_ID, "hello WeEvent".getBytes(StandardCharsets.UTF_8), new HashMap<>());
            System.out.println(sendResult);
        } catch (MalformedURLException | BrokerException e) {
            e.printStackTrace();
        }
    }
}
