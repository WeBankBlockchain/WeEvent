package com.webank.weevent.ST;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * CGI subscribe test.
 *
 * @author matthewliu
 * @version 1.0
 * @since 2019/02/14
 */
@Slf4j
public class SubscribeTest extends JUnitTestBase {
    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testRestfulSubscribe() {
        log.info("===================={}", this.testName.getMethodName());

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        RestTemplate rest = new RestTemplate(requestFactory);

        ResponseEntity<String> rsp = rest.getForEntity("http://localhost:8080/weevent/rest/subscribe?topic={topic}&subscriptionId={subscriptionId}&url={url}",
                String.class,
                this.topicName,
                "",
                "http://localhost:" + listenPort + "/weevent/mock/rest/onEvent");
        log.info("subscribe, status: {} body: {}", rsp.getStatusCode(), rsp.getBody());
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertFalse(rsp.getBody().contains("{"));

        rsp = rest.getForEntity("http://localhost:" + listenPort + "/weevent/rest/publish?topic={topic}&content={content}",
                String.class,
                this.topicName,
                "hello weevent");
        log.info("publish, status: {} body: {}", rsp.getStatusCode(), rsp.getBody());
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody().contains("SUCCESS"));

        // wait to notify
        try {
            Thread.sleep(10 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testJsonRPCSubscribe() {
        log.info("===================={}", this.testName.getMethodName());

        URL remote = null;
        try {
            remote = new URL("http://localhost:8080/weevent/jsonrpc");
        } catch (MalformedURLException e) {
            log.error("invalid url", e);
            fail();
        }

        JsonRpcHttpClient client = new JsonRpcHttpClient(remote);
        IBrokerRpc rpc = ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);

        try {
            String subscriptionId = rpc.subscribe(this.topicName,
                    "",
                    "http://localhost:8080/weevent/mock/jsonrpc");
            assertTrue(!subscriptionId.isEmpty());

            SendResult sendResult = rpc.publish(this.topicName, "hello weevent".getBytes(StandardCharsets.UTF_8));
            assertTrue(sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS);
        } catch (BrokerException e) {
            log.error("error", e);
            fail();
        }

        // wait to notify
        try {
            Thread.sleep(10 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
