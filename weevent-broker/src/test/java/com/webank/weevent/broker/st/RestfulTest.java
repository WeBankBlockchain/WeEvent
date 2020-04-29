package com.webank.weevent.broker.st;

import com.webank.weevent.broker.JUnitTestBase;
import com.webank.weevent.client.BaseResponse;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.TopicInfo;
import com.webank.weevent.client.TopicPage;
import com.webank.weevent.client.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RestfulTest extends JUnitTestBase {
    private final String topicName = "com.weevent.test";

    private String url;
    private RestTemplate rest = null;
    private String eventId = "";
    private final String content = "hello restful";

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.url = "http://localhost:" + this.listenPort + "/weevent-broker/rest/";

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        this.rest = new RestTemplate(requestFactory);

        this.rest.exchange(url + "open?topic={topic}&groupId={groupId}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<Boolean>>() {
        }, this.topicName, WeEvent.DEFAULT_GROUP_ID);

        SendResult sendResult = rest.getForEntity(url + "publish?topic={topic}&content={content}", SendResult.class, this.topicName,
                this.content).getBody();
        Assert.assertNotNull(sendResult);
        this.eventId = sendResult.getEventId();
    }

    @Test
    public void testOpenNoGroupId() {
        ResponseEntity<BaseResponse<Boolean>> rsp = this.rest.exchange(url + "open?topic={topic}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<Boolean>>() {
        }, this.topicName);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getData());
    }

    @Test
    public void testOpenWithGroupId() {
        ResponseEntity<BaseResponse<Boolean>> rsp = this.rest.exchange(url + "open?topic={topic}&groupId={groupId}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<Boolean>>() {
        }, this.topicName, WeEvent.DEFAULT_GROUP_ID);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getData());
    }

    @Test
    public void testCloseNoGroupId() {
        ResponseEntity<BaseResponse<Boolean>> rsp = this.rest.exchange(url + "close?topic={topic}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<Boolean>>() {
        }, this.topicName);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getData());
    }

    @Test
    public void testCloseContainGroupId() {
        ResponseEntity<BaseResponse<Boolean>> rsp = this.rest.exchange(url + "close?topic={topic}&groupId={groupId}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<Boolean>>() {
        }, this.topicName, WeEvent.DEFAULT_GROUP_ID);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getData());
    }

    @Test
    public void testExistNoGroupId() {
        ResponseEntity<BaseResponse<Boolean>> rsp = this.rest.exchange(url + "exist?topic={topic}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<Boolean>>() {
        }, this.topicName);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getData());
    }

    @Test
    public void testExistContainGroupId() {
        ResponseEntity<BaseResponse<Boolean>> rsp = this.rest.exchange(url + "exist?topic={topic}&groupId={groupId}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<Boolean>>() {
        }, this.topicName, WeEvent.DEFAULT_GROUP_ID);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getData());
    }

    @Test
    public void testStateNoGroupId() {
        ResponseEntity<BaseResponse<TopicInfo>> rsp = rest.exchange(url + "state?topic={topic}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<TopicInfo>>() {
        }, this.topicName);
        log.info("state, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(this.topicName, rsp.getBody().getData().getTopicName());
    }

    @Test
    public void testStateWithGroupId() {
        ResponseEntity<BaseResponse<TopicInfo>> rsp = rest.exchange(url + "state?topic={topic}&groupId={groupId}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<TopicInfo>>() {
        }, this.topicName, WeEvent.DEFAULT_GROUP_ID);
        log.info("state, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(this.topicName, rsp.getBody().getData().getTopicName());
    }

    @Test
    public void testListNoGroupId() {
        ResponseEntity<BaseResponse<TopicPage>> rsp = rest.exchange(url + "list?pageIndex={pageIndex}&pageSize={pageSize}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<TopicPage>>() {
        }, "0", "10");

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getData().getTotal() > 0);
    }

    @Test
    public void testListWithGroupId() {
        ResponseEntity<BaseResponse<TopicPage>> rsp = rest.exchange(url + "list?pageIndex={pageIndex}&pageSize={pageSize}&groupId={groupId}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<TopicPage>>() {
        }, "0", "10", WeEvent.DEFAULT_GROUP_ID);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getData().getTotal() > 0);
    }

    @Test
    public void testGetEventNoGroupId() {
        ResponseEntity<BaseResponse<WeEvent>> rsp = rest.exchange(url + "getEvent?eventId={eventId}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<WeEvent>>() {
        }, this.eventId);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        WeEvent event = rsp.getBody().getData();
        String returnContent = new String(event.getContent());
        Assert.assertEquals(this.content, returnContent);
    }

    @Test
    public void testGetEventWithGroupId() {
        ResponseEntity<BaseResponse<WeEvent>> rsp = rest.exchange(url + "getEvent?eventId={eventId}&groupId={groupId}", HttpMethod.GET, null, new ParameterizedTypeReference<BaseResponse<WeEvent>>() {
        }, this.eventId, WeEvent.DEFAULT_GROUP_ID);

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        WeEvent event = rsp.getBody().getData();
        String returnContent = new String(event.getContent());
        Assert.assertEquals(this.content, returnContent);
    }

    @Test
    public void testPublishNoGroupId() {
        ResponseEntity<SendResult> rsp = rest.getForEntity(url + "publish?topic={topic}&content={content}",
                SendResult.class, this.topicName, this.content);
        log.info("publish, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testPublishContainGroupId() {
        ResponseEntity<SendResult> rsp = rest.getForEntity(
                url + "publish?topic={topic}&content={content}&groupId={groupId}", SendResult.class, this.topicName,
                this.content, WeEvent.DEFAULT_GROUP_ID);
        log.info("publish, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());

        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testPublishContentIs10K() {
        MultiValueMap<String, String> eventData = new LinkedMultiValueMap<>();
        eventData.add("topic", this.topicName);
        eventData.add("content", get10KStr());

        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.APPLICATION_FORM_URLENCODED;
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(eventData, headers);
        ResponseEntity<SendResult> rsp = rest.postForEntity(url + "publish", request, SendResult.class);
        log.info("publish, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testPublishContentGt10K() {
        MultiValueMap<String, String> eventData = new LinkedMultiValueMap<>();
        eventData.add("topic", this.topicName);
        eventData.add("content", get10KStr() + "s");

        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.APPLICATION_FORM_URLENCODED;
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(eventData, headers);
        ResponseEntity<String> rsp = rest.postForEntity(url + "publish", request, String.class);
        log.info("publish, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().contains(ErrorCode.EVENT_CONTENT_EXCEEDS_MAX_LENGTH.getCode() + ""));
    }

    @Test
    public void testPublishNoGroupIdContainExt() {
        ResponseEntity<SendResult> rsp = rest.getForEntity(
                url + "publish?topic={topic}&content={content}&weevent-test1={value1}&weevent-test2={value2}",
                SendResult.class, this.topicName, this.content, "test1value", "test2vlaue");
        log.info("publish, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    @Test
    public void testPublishContainGroupIdContainExt() {
        ResponseEntity<SendResult> rsp = rest.getForEntity(url
                        + "publish?topic={topic}&content={content}&weevent-test1={value1}&weevent-test2={value2}&groupId={groupId}",
                SendResult.class, this.topicName, this.content, "test1value", "test2vlaue", WeEvent.DEFAULT_GROUP_ID);
        log.info("publish, status: " + rsp.getStatusCode() + " body: " + rsp.getBody());
        Assert.assertEquals(200, rsp.getStatusCodeValue());
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getEventId().contains("-"));
    }

    // get 10K string
    private String get10KStr() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            result.append("abcdefghij");
        }
        return result.toString();
    }
}
