package com.webank.weevent.client.sdk;

import java.nio.charset.StandardCharsets;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static java.lang.Thread.sleep;

/**
 * WeEventClient Tester.
 *
 * @author <cristic>
 * @version 1.0
 * @since <pre>05/10/2019</pre>
 */
@Slf4j
public class WeEventClientTest {

    @Rule
    public TestName testName = new TestName();

    private String topicName = "com.webank.weevent";

    private IWeEventClient weEventClient;

    @Before
    public void before() throws Exception {
        this.weEventClient = IWeEventClient.build("http://localhost:7000/weevent");
        this.weEventClient.open(this.topicName);
    }

    @After
    public void after() throws Exception {
        this.weEventClient.close(this.topicName);
    }


    /**
     * Method: publish(String topic, byte[] content, Map<String, String> extensions)
     */
    @Test
    public void testPublish() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        WeEvent weEvent = new WeEvent();
        weEvent.setTopic(this.topicName);
        weEvent.setContent("hello world".getBytes(StandardCharsets.UTF_8));
        SendResult sendResult = this.weEventClient.publish(weEvent);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
        WeEvent event = this.weEventClient.getEvent(sendResult.getEventId());
        Assert.assertNotNull(event);
        Assert.assertEquals(this.topicName,event.getTopic());
    }

    @Test
    public void testSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // create subscriber
        String subscribeId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                System.out.println("onEvent:" + event.toString());
                log.info("onEvent:" + event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });

        Assert.assertFalse(subscribeId.isEmpty());
        sleep(1000000);
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
