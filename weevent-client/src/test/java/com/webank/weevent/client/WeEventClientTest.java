package com.webank.weevent.client;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * WeEventClient Tester.
 *
 * @author <cristic>
 * @version 1.0
 * @since <pre>05/10/2019</pre>
 */
@Slf4j
public class WeEventClientTest {

    private Map<String, String> extensions = new HashMap<>();
    private final long FIVE_SECOND = 5000L;
    private final long transactionTimeout = 10;

    @Rule
    public TestName testName = new TestName();

    private String topicName = "com.weevent.test";

    private IWeEventClient weEventClient;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.extensions.put(WeEvent.WeEvent_TAG, "test");
        this.weEventClient = IWeEventClient.builder().brokerUrl("http://localhost:7000/weevent-broker").build();
        this.weEventClient.open(this.topicName);
    }

    @After
    public void after() throws Exception {
        this.weEventClient.close(this.topicName);
    }


    /**
     * Method: publish(WeEvent weEvent)
     */
    @Test
    public void testPublish() throws Exception {
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
        Assert.assertFalse(sendResult.getEventId().isEmpty());
    }

    /**
     * Method: publishAsync(WeEvent weEvent)
     */
    @Test
    public void testPublishAsync() throws Exception {
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publishAsync(weEvent).get(this.transactionTimeout, TimeUnit.SECONDS);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
        Assert.assertFalse(sendResult.getEventId().isEmpty());
    }

    @Test
    public void testSubscribe() throws Exception {
        // create subscriber
        String subscribeId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, extensions, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info("onEvent: {}", event.toString());

                Assert.assertFalse(event.getTopic().isEmpty());
                Assert.assertFalse(event.getEventId().isEmpty());
            }

            @Override
            public void onException(Throwable e) {
                log.error("onException:", e);
                Assert.fail();
            }
        });

        Assert.assertFalse(subscribeId.isEmpty());
        Thread.sleep(this.FIVE_SECOND);
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testOpen_topicOverMaxLen() {
        try {
            String topic = "topiclengthexceeding64-123456789012345678901234567890123456789012";
            this.weEventClient.open(topic);
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * Method: open(String topic)
     */
    @Test
    public void testOpen() throws Exception {
        boolean result = this.weEventClient.open(this.topicName);
        Assert.assertTrue(result);
    }

    /**
     * Method: open(String topic)
     */
    @Test
    public void testOpenChinese() throws Exception {
        try {
            this.weEventClient.open("中文");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * Method: close(String topic)
     */
    @Test
    public void testClose() throws Exception {
        boolean result = this.weEventClient.close(topicName);
        Assert.assertTrue(result);
    }

    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExist() throws Exception {
        boolean result = this.weEventClient.exist(this.topicName);
        Assert.assertTrue(result);
    }

    /**
     * Method: list(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testList() throws Exception {
        TopicPage topicPage = this.weEventClient.list(0, 10);
        Assert.assertTrue(topicPage.getTotal() > 0);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testState() throws Exception {
        TopicInfo topicInfo = this.weEventClient.state(this.topicName);
        Assert.assertEquals(topicInfo.getTopicName(), this.topicName);
    }
}
