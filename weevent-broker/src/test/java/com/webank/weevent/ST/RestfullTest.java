package com.webank.weevent.ST;

import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class RestfullTest extends JUnitTestBase {

    private String url;
    private RestTemplate rest = null;
    private String subId = "";
    private String eventId = "";
    private String restTopic = "com.weevent.test.rest";
    private String content = "hellow eevent";

    @Before
    public void before() {
	url = "http://localhost:" + listenPort + "/weevent/rest/";
	SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
	this.rest = new RestTemplate(requestFactory);
	rest.getForEntity(url + "open?topic={topic}", Boolean.class, this.restTopic);

	eventId = rest.getForEntity(url + "publish?topic={topic}&content={content}", SendResult.class, this.restTopic,
		this.content).getBody().getEventId();
    }

    @Test
    public void testOpen_noGroupId() {
	ResponseEntity<Boolean> rsp = rest.getForEntity(url + "open?topic={topic}", Boolean.class, this.restTopic);
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody());
    }

    @Test
    public void testOpen_cotainGroupId() {
	ResponseEntity<Boolean> rsp = rest.getForEntity(url + "open?topic={topic}&groupId={groupId}", Boolean.class,
		this.restTopic, "1");
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody());
    }

    @Test
    public void testClose_noGroupId() {
	ResponseEntity<Boolean> rsp = rest.getForEntity(url + "close?topic={topic}", Boolean.class, this.restTopic);
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody());
    }

    @Test
    public void testClose_cotainGroupId() {
	ResponseEntity<Boolean> rsp = rest.getForEntity(url + "close?topic={topic}&groupId={groupId}", Boolean.class,
		this.restTopic, "1");
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody());
    }

    @Test
    public void testExist_noGroupId() {
	ResponseEntity<Boolean> rsp = rest.getForEntity(url + "exist?topic={topic}", Boolean.class, this.restTopic);
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody());
    }

    @Test
    public void testExist_cotainGroupId() {
	ResponseEntity<Boolean> rsp = rest.getForEntity(url + "exist?topic={topic}&groupId={groupId}", Boolean.class,
		this.restTopic, "1");
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody());
    }

    @Test
    public void testState_noGroupId() {
	ResponseEntity<TopicInfo> rsp = rest.getForEntity(url + "state?topic={topic}", TopicInfo.class, this.restTopic);
	System.out.println("state, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertEquals(this.restTopic, rsp.getBody().getTopicName());
    }

    @Test
    public void testState_cotainGroupId() {
	ResponseEntity<TopicInfo> rsp = rest.getForEntity(url + "state?topic={topic}&groupId={groupId}",
		TopicInfo.class, this.restTopic, "1");
	System.out.println("state, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertEquals(this.restTopic, rsp.getBody().getTopicName());
    }

    @Test
    public void testList_noGroupId() {
	ResponseEntity<TopicPage> rsp = rest.getForEntity(url + "list?pageIndex={pageIndex}&pageSize={pageSize}",
		TopicPage.class, "0", "10");
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().getTotal() > 0);
    }

    @Test
    public void testList_cotainGroupId() {
	ResponseEntity<TopicPage> rsp = rest.getForEntity(
		url + "list?pageIndex={pageIndex}&pageSize={pageSize}&groupId={groupId}", TopicPage.class, "0", "10",
		"1");
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().getTotal() > 0);
    }

    @Test
    public void testGetEvent_noGroupId() {
	ResponseEntity<WeEvent> rsp = rest.getForEntity(url + "getEvent?eventId={eventId}", WeEvent.class,
		this.eventId);
	assertTrue(rsp.getStatusCodeValue() == 200);
	WeEvent event = rsp.getBody();
	String returnContent = new String(event.getContent());
	assertEquals(this.content, returnContent);
    }

    @Test
    public void testGetEvent_containGroupId() {
	ResponseEntity<WeEvent> rsp = rest.getForEntity(url + "getEvent?eventId={eventId}&groupId={groupId}",
		WeEvent.class, this.eventId, "1");
	assertTrue(rsp.getStatusCodeValue() == 200);
	WeEvent event = rsp.getBody();
	String returnContent = new String(event.getContent());
	assertEquals(this.content, returnContent);
    }

    @Test
    public void testPublish_noGroupId() {
	ResponseEntity<SendResult> rsp = rest.getForEntity(url + "publish?topic={topic}&content={content}",
		SendResult.class, this.restTopic, this.content);
	log.info("subscribe, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testPublish_containGroupId() {
	ResponseEntity<SendResult> rsp = rest.getForEntity(
		url + "publish?topic={topic}&content={content}&groupId={groupId}", SendResult.class, this.restTopic,
		this.content, "1");
	log.info("subscribe, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testPublish_contentIs10K() {
	MultiValueMap<String, String> eventData = new LinkedMultiValueMap<String, String>();
	eventData.add("topic", this.restTopic);
	eventData.add("content", get10KStr());

	HttpHeaders headers = new HttpHeaders();
	MediaType type = MediaType.APPLICATION_FORM_URLENCODED;
	headers.setContentType(type);
	headers.add("Accept", MediaType.APPLICATION_JSON.toString());

	HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(eventData,
		headers);
	ResponseEntity<SendResult> rsp = rest.postForEntity(url + "publish", request, SendResult.class);
	log.info("subscribe, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testPublish_contentGt10K() {
	MultiValueMap<String, String> eventData = new LinkedMultiValueMap<String, String>();
	eventData.add("topic", this.restTopic);
	eventData.add("content", get10KStr() + "s");

	HttpHeaders headers = new HttpHeaders();
	MediaType type = MediaType.APPLICATION_FORM_URLENCODED;
	headers.setContentType(type);
	headers.add("Accept", MediaType.APPLICATION_JSON.toString());

	HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(eventData,
		headers);
	ResponseEntity<String> rsp = rest.postForEntity(url + "publish", request, String.class);
	log.info("subscribe, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().contains(ErrorCode.EVENT_CONTENT_EXCEEDS_MAX_LENGTH.getCode() + ""));
    }

    @Test
    public void testPublish_noGroupIdContainExt() {
	ResponseEntity<SendResult> rsp = rest.getForEntity(
		url + "publish?topic={topic}&content={content}&weevent-test1={value1}&weevent-test2={value2}",
		SendResult.class, this.restTopic, this.content, "test1value", "test2vlaue");
	log.info("subscribe, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testPublish_containGroupIdContainExt() {
	ResponseEntity<SendResult> rsp = rest.getForEntity(url
		+ "publish?topic={topic}&content={content}&weevent-test1={value1}&weevent-test2={value2}&groupId={groupId}",
		SendResult.class, this.restTopic, this.content, "test1value", "test2vlaue", "1");
	log.info("subscribe, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testSubscribe_containTopicUrl() {
	ResponseEntity<String> rsp = rest.getForEntity(url + "subscribe?topic={topic}&url={url}", String.class,
		this.restTopic, "http://localhost:" + listenPort + "/weevent/mock/rest/onEvent");
	log.info("subId: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().contains("-"));
    }

    @Test
    public void testSubscribe_errorUrl() {

	ResponseEntity<String> rsp = rest.getForEntity(url + "subscribe?topic={topic}&url={url}", String.class,
		this.restTopic, "http://localhost:" + 8088 + "/weevent/mock/rest/onEvent");
	log.info("result: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().contains(ErrorCode.URL_CONNECT_FAILED.getCodeDesc()));
    }

    @Test
    public void testSubscribe_containTopicGroupIdUrl() {
	ResponseEntity<String> rsp = rest.getForEntity(url + "subscribe?topic={topic}&url={url}&groupId={groupId}",
		String.class, this.restTopic, "http://localhost:" + listenPort + "/weevent/mock/rest/onEvent", "1");
	log.info("subId: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().contains("-"));
    }

    @Test
    public void testSubscribe_containTopicGroupIdErrorUrl() {
	ResponseEntity<String> rsp = rest.getForEntity(url + "subscribe?topic={topic}&url={url}&groupId={groupId}",
		String.class, this.restTopic, "http://localhost:" + 8088 + "/weevent/mock/rest/onEvent", "1");
	log.info("result: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().contains(ErrorCode.URL_CONNECT_FAILED.getCodeDesc()));
    }

    @Test
    public void testSubscribe_containTopicGroupIdSubIdErrorUrl() {
	ResponseEntity<String> result = rest.getForEntity(url + "subscribe?topic={topic}&url={url}", String.class,
		this.restTopic, "http://localhost:" + listenPort + "/weevent/mock/rest/onEvent");
	subId = result.getBody();
	ResponseEntity<String> rsp = rest.getForEntity(
		url + "subscribe?topic={topic}&url={url}&groupId={groupId}&subscriptionId={subscriptionId}",
		String.class, this.restTopic, "http://localhost:" + 8088 + "/weevent/mock/rest/onEvent", "1", subId);
	log.info("result: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().contains(ErrorCode.URL_CONNECT_FAILED.getCodeDesc()));
    }

    @Test
    public void testReSubscribe() {
	ResponseEntity<String> result = rest.getForEntity(url + "subscribe?topic={topic}&url={url}", String.class,
		this.restTopic, "http://localhost:" + listenPort + "/weevent/mock/rest/onEvent");
	subId = result.getBody();
	ResponseEntity<String> rsp = rest.getForEntity(
		url + "subscribe?topic={topic}&subscriptionId={subscriptionId}&url={url}", String.class, this.restTopic,
		subId, "http://localhost:" + listenPort + "/weevent/mock/rest/onEvent");
	log.info("subId: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().equals(subId));
    }

    @Test
    public void testUnsubscribe() {
	ResponseEntity<String> result = rest.getForEntity(url + "subscribe?topic={topic}&url={url}", String.class,
		this.restTopic, "http://localhost:" + listenPort + "/weevent/mock/rest/onEvent");
	subId = result.getBody();
	ResponseEntity<String> rsp = rest.getForEntity(url + "unSubscribe?subscriptionId={subscriptionId}",
		String.class, subId);
	log.info("unsubscribe, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
	assertTrue(rsp.getStatusCodeValue() == 200);
	assertTrue(rsp.getBody().equals("true"));
    }

    // get 10K string
    private String get10KStr() {
	StringBuilder result = new StringBuilder("");
	for (int i = 0; i < 1024; i++) {
	    result = result.append("abcdefghij");
	}
	return result.toString();
    }
}
