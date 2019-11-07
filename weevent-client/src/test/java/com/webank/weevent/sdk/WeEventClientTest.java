package com.webank.weevent.sdk;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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

    @Rule
    public TestName testName = new TestName();

    private String topicName = "com.webank.weevent";

    private IWeEventClient weEventClient;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.extensions.put(WeEvent.WeEvent_TAG, "test");
        this.weEventClient = IWeEventClient.build("http://localhost:7000/weevent");
        this.weEventClient.open(this.topicName);
    }

    @After
    public void after() throws Exception {
        this.weEventClient.close(this.topicName);
    }


    /**
     * Method: publish( WeEvent weEvent)
     */
    @Test
    public void testPublish() throws Exception {
        WeEvent weEvent = new WeEvent(this.topicName, "hello world".getBytes(StandardCharsets.UTF_8), this.extensions);
        SendResult sendResult = this.weEventClient.publish(weEvent);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
        Assert.assertFalse(sendResult.getEventId().isEmpty());
    }

    @Test
    public void testSubscribe() throws Exception {
        // create subscriber
        String subscribeId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info("onEvent: {}", event.toString());

                Assert.assertFalse(event.getTopic().isEmpty());
                Assert.assertFalse(event.getEventId().isEmpty());
            }

            @Override
            public void onException(Throwable e) {
                log.error("onException", e);
                Assert.fail();
            }
        });

        Assert.assertFalse(subscribeId.isEmpty());
        Thread.sleep(10000);
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
        TopicPage list = this.weEventClient.list(0, 10);
        Assert.assertTrue(list.getTotal() > 0);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testState() throws Exception {
        TopicInfo info = this.weEventClient.state(this.topicName);
        Assert.assertEquals(info.getTopicName(), this.topicName);
    }

    /**
     * Method: getEvent(String eventId)
     */
    @Test(expected = BrokerException.class)
    public void testGetEvent() throws Exception {
        this.weEventClient.getEvent("not exist");
    }
}
