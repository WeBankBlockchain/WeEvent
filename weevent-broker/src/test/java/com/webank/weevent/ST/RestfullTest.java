package com.webank.weevent.ST;

import java.util.Map;

import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
public class RestfullTest {

    private static String URL = "http://localhost:8080/weevent/rest/";
    private RestTemplate rest = null;
    private static String subId = "";
    private static String eventId = "";

    @Before
    public void before() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        this.rest = new RestTemplate(requestFactory);
    }

    @Test
    public void testOpenTopic() {
        String topic = "com.weevent.test.rest";
        ResponseEntity<Boolean> rsp =
                rest.getForEntity(URL + "open?topic={topic}", Boolean.class, topic);
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody());
    }

    @Test
    public void testCloseTopic() {
        String topic = "com.weevent.test.rest";
        ResponseEntity<Boolean> rsp =
                rest.getForEntity(URL + "close?topic={topic}", Boolean.class, topic);
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody());
    }

    @Test
    public void testExistTopic() {
        ResponseEntity<Boolean> rsp =
                rest.getForEntity(URL + "exist?topic={topic}", Boolean.class, "com.weevent.test.rest");
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody());
    }

    @Test
    public void testState() {
        ResponseEntity<String> rsp = rest.getForEntity(URL + "state?topic={topic}",
                String.class,
                "com.weevent.test.rest");
        System.out.println("state, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody().contains("}"));
    }

    @Test
    public void testList() {
        ResponseEntity<String> rsp = rest.getForEntity(URL
                + "list?pageIndex={pageIndex}&pageSize={pageSize}", String.class, "0", "10");
        System.out.println("getEvent, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody().contains("]"));
    }

    @Test
    public void testSubscribe() {
        ResponseEntity<String> rsp = rest.getForEntity(
                URL + "subscribe?topic={topic}&subscriptionId={subscriptionId}&url={url}",
                String.class,
                "com.weevent.test.rest",
                "",
                "http://localhost:8081/mock/rest/onEvent");
        this.subId = rsp.getBody();
        log.info("subId: " + subId);
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody().contains("-"));
    }

    @Test
    public void testReSubscribe() {
        ResponseEntity<String> rsp = rest.getForEntity(
                URL + "subscribe?topic={topic}&subscriptionId={subscriptionId}&url={url}",
                String.class,
                "com.weevent.test.rest",
                subId,
                "http://localhost:8081/mock/rest/onEvent");
        log.info("subId: " + rsp.getBody());
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody().contains("-"));
    }

    @Test
    public void testPublish() {
        ResponseEntity<SendResult> rsp =
                rest.getForEntity(URL + "publish?topic={topic}&content={content}",
                        SendResult.class,
                        "com.weevent.test.rest",
                        "hellow eevent");
        log.info("subscribe, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        eventId = rsp.getBody().getEventId();
        System.out.println(rsp.getBody().getEventId());
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testGetEvent() {
        ResponseEntity<WeEvent> rsp =
                rest.getForEntity(URL + "getEvent?eventId={eventId}", WeEvent.class, "e39837d63efaffaa-4-74573");
        WeEvent event = rsp.getBody();
        String content = new String(event.getContent());
        System.out.println("getEvent, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        System.out.println("content: " + content);
        assertTrue(content.equals("hello weevent"));
    }


    @Test
    public void testGetBlockinfo() {
        ResponseEntity<String> rsp = rest
                .getForEntity("http://localhost:8081/weevent/admin/blockchaininfo", String.class);
        System.out
                .println("getBlockInfo, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertTrue(rsp.getBody().contains("]"));
    }

    @Test
    public void testDeployTopicControl() {
        ResponseEntity<String> rsp =
                rest.getForEntity("http://localhost:8081/weevent/admin/deploy_topic_control",
                        String.class);
        System.out.println(
                "deploy_topic_control, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        assertTrue(rsp.getStatusCodeValue() == 200);
    }

    @Test
    public void testListSubId() {
        ResponseEntity<Map> rsp = rest
                .getForEntity("http://localhost:8081/weevent/admin/listSubscription", Map.class);

        Map subMap = rsp.getBody();
        System.out
                .println("testListSubId, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        assertTrue(rsp.getStatusCodeValue() == 200);
    }

    @Test
    public void testUnsubscribe() {
        ResponseEntity<String> rsp =
                rest.getForEntity(URL + "unSubscribe?subscriptionId={subscriptionId}",
                        String.class,
                        "96da5b3b-8cfa-442e-969e-5af9c65791aa");
        System.out
                .println("unsubscribe, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        assertTrue(rsp.getStatusCodeValue() == 200);
        assertFalse(rsp.getBody().contains("{"));
    }
}
