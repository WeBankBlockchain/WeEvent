package com.webank.weevent.st;

import java.util.concurrent.TimeUnit;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Ignore("FiscoV1 is not default setting")
@RunWith(MySpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BrokerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RestfulFiscoV1Test {
    private String url;
    private String content = "hello world";
    private String eventId;

    protected String topicName = "com.weevent.test";

    private RestTemplate rest = new RestTemplate(new SimpleClientHttpRequestFactory());

    @Value("${server.port}")
    public String listenPort;

    @Rule
    public TestName testName = new TestName();

    @Rule
    public Timeout timeout = new Timeout(120, TimeUnit.SECONDS);

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.url = "http://localhost:" + listenPort + "/weevent/rest/";

        this.rest.getForEntity(this.url + "open?topic={topic}", Boolean.class, this.topicName);
        ResponseEntity<SendResult> rsp = this.rest.getForEntity(this.url + "publish?topic={topic}&content={content}",
                SendResult.class,
                this.topicName,
                this.content);

        Assert.assertNotNull(rsp.getBody());
        this.eventId = rsp.getBody().getEventId();
    }

    @Test
    public void testOpen() {
        ResponseEntity<Boolean> rsp = this.rest.getForEntity(this.url + "open?topic={topic}", Boolean.class, this.topicName);

        Assert.assertEquals(rsp.getStatusCodeValue(), 200);
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody());
    }

    @Test
    public void testClose() {
        ResponseEntity<Boolean> rsp = this.rest.getForEntity(this.url + "close?topic={topic}", Boolean.class, this.topicName);

        Assert.assertEquals(rsp.getStatusCodeValue(), 200);
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody());
    }

    @Test
    public void testExist() {
        ResponseEntity<Boolean> rsp = this.rest.getForEntity(this.url + "exist?topic={topic}", Boolean.class, this.topicName);

        Assert.assertEquals(rsp.getStatusCodeValue(), 200);
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody());
    }

    @Test
    public void testState() {
        ResponseEntity<TopicInfo> rsp = this.rest.getForEntity(this.url + "state?topic={topic}", TopicInfo.class, this.topicName);

        Assert.assertEquals(rsp.getStatusCodeValue(), 200);
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(this.topicName, rsp.getBody().getTopicName());
    }

    @Test
    public void testList() {
        ResponseEntity<TopicPage> rsp = this.rest.getForEntity(this.url + "list?pageIndex={pageIndex}&pageSize={pageSize}",
                TopicPage.class, "0", "10");

        Assert.assertEquals(rsp.getStatusCodeValue(), 200);
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getTotal() > 0);
    }

    @Test
    public void testGetEvent() {
        ResponseEntity<WeEvent> rsp = this.rest.getForEntity(this.url + "getEvent?eventId={eventId}", WeEvent.class, this.eventId);

        Assert.assertEquals(rsp.getStatusCodeValue(), 200);
        Assert.assertNotNull(rsp.getBody());
        Assert.assertEquals(this.eventId, rsp.getBody().getEventId());
        Assert.assertEquals(this.content, new String(rsp.getBody().getContent()));
    }

    @Test
    public void testPublish() {
        ResponseEntity<SendResult> rsp = this.rest.getForEntity(this.url + "publish?topic={topic}&content={content}",
                SendResult.class, this.topicName, this.content);

        Assert.assertEquals(rsp.getStatusCodeValue(), 200);
        Assert.assertNotNull(rsp.getBody());
        Assert.assertTrue(rsp.getBody().getEventId().contains("-"));
    }
}
