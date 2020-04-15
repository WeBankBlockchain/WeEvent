package com.webank.weevent.client;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

    private String topicName = "com.weevent.test";

    private IWeEventClient weEventClient;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.extensions.put(WeEvent.WeEvent_TAG, "test");
        this.weEventClient = new IWeEventClient.Builder().brokerUrl("http://localhost:7000/weevent-broker").build();
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
        Thread.sleep(5000);
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
        BaseResponse<Boolean> response = this.weEventClient.open(this.topicName);
        Assert.assertTrue(response.getData());
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
        BaseResponse<Boolean> response = this.weEventClient.close(topicName);
        Assert.assertTrue(response.getData());
    }

    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExist() throws Exception {
        BaseResponse<Boolean> response = this.weEventClient.exist(this.topicName);
        Assert.assertTrue(response.getData());
    }

    /**
     * Method: list(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testList() throws Exception {
        BaseResponse<TopicPage> response = this.weEventClient.list(0, 10);
        Assert.assertEquals(0, response.getCode());
        Assert.assertTrue(response.getData().getTotal() > 0);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testState() throws Exception {
        BaseResponse<TopicInfo> response = this.weEventClient.state(this.topicName);
        Assert.assertEquals(0, response.getCode());
        Assert.assertEquals(response.getData().getTopicName(), this.topicName);
    }

    /**
     * Method: getEvent(String eventId)
     */
    @Test(expected = BrokerException.class)
    public void testGetEvent() throws Exception {
        this.weEventClient.getEvent("not exist");
    }

    @Test
    @Ignore
    public void testPublishFile() throws Exception {
        BaseResponse<Boolean> response = this.weEventClient.open("com.weevent.file");
        Assert.assertTrue(response.getData());

        SendResult sendResult = this.weEventClient.publishFile("com.weevent.file",
                new File("src/main/resources/log4j2.xml").getAbsolutePath());
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    @Ignore
    @Test
    public void testSubscribeFile() throws Exception {
        boolean result = this.weEventClient.open("com.weevent.file").getData();
        Assert.assertTrue(result);


        String subscriptionId = this.weEventClient.subscribeFile("com.weevent.file", "./logs", new IWeEventClient.FileListener() {
            @Override
            public void onFile(String subscriptionId, String localFile) {
                Assert.assertFalse(subscriptionId.isEmpty());
                Assert.assertFalse(localFile.isEmpty());

                // file data stored in localFile
            }

            @Override
            public void onException(Throwable e) {

            }
        });

        Assert.assertFalse(subscriptionId.isEmpty());
        this.weEventClient.unSubscribe(subscriptionId);
    }
}
