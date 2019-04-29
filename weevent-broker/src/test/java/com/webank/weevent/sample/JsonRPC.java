package com.webank.weevent.sample;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

public class JsonRPC {
    public static void main(String[] args) {
        System.out.println("This is WeEvent json rpc sample.");

        try {
            URL remote = new URL("http://localhost:8080/weevent/jsonrpc");
            // 创建客户端
            JsonRpcHttpClient client = new JsonRpcHttpClient(remote);
            // 实例化rpc对象
            IBrokerRpc rpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);

            // 确认主题存在
            rpc.open("com.weevent.test");

            // 发布事件，主题“com.weevent.test”，事件内容为"hello weevent"
            SendResult sendResult = rpc.publish("com.weevent.test", "hello weevent".getBytes(StandardCharsets.UTF_8));
            System.out.println(sendResult.getStatus());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (BrokerException e) {
            e.printStackTrace();
        }
    }
}
