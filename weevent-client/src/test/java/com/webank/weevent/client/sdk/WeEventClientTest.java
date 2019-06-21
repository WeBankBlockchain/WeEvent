package com.webank.weevent.client.sdk;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.WeEventClient;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

    public String topicName = "com.webank.weevent";

    private WeEventClient weEventClient;

    @Before
    public void before() throws Exception {
        weEventClient = new WeEventClient("http://127.0.0.1:8080/weevent");
        weEventClient.open(topicName);

    }

    @After
    public void after() throws Exception {
        weEventClient.close(topicName);
    }


    /**
     * Method: publish(String topic, byte[] content, Map<String, String> extensions)
     */
    @Test
    public void testPublish() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        SendResult sendResult = this.weEventClient.publish(topicName, "hello world".getBytes(StandardCharsets.UTF_8));
        assertTrue(sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS);
    }


    /**
     * Method: subscribe(String topic, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // create subscriber
        String groupId = "1";//if not set default 1
        this.weEventClient.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {
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
        sleep(1000000);
    }

    /**
     * test topic length > 64
     */
    @Test
    public void testOpen_topicOverMaxLen() {
        try {
            String topic = "topiclengthexceeding64-123456789012345678901234567890123456789012";
            boolean result = weEventClient.open(topic);
            assertNull(result);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * Method: subscribe(String topic, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribeEventId() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        SendResult sendResult = this.weEventClient.publish(topicName, "hello world".getBytes(StandardCharsets.UTF_8));
        System.out.print("sendResult getEventId" + sendResult.getEventId());
        String groupId = "1";//if not set default 1
        this.weEventClient.subscribe(this.topicName, groupId, sendResult.getEventId(), new WeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                System.out.print(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
        sleep(100000);
    }


    /**
     * Method: unSubscribe(String subscriptionId)
     */
    @Test
    public void testUnSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        String groupId = "1";//if not set default 1
        String subscriptionId = this.weEventClient.subscribe(this.topicName, groupId, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
        assertTrue(!subscriptionId.isEmpty());
        boolean result = this.weEventClient.unSubscribe(subscriptionId);
        assertTrue(result);
    }

    /**
     * Method: open(String topic)
     */
    @Test
    public void testOpen() throws Exception {
        boolean result = this.weEventClient.open(this.topicName);
        assertTrue(result);
    }


    /**
     * Method: close(String topic)
     */
    @Test
    public void testClose() throws Exception {
        boolean result = weEventClient.close(topicName);
        assertTrue(result);
    }


    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExist() throws Exception {
        boolean result = this.weEventClient.exist(this.topicName);
        assertTrue(result);
    }

    /**
     * Method: list(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testList() throws Exception {
        TopicPage list = this.weEventClient.list(0, 10);
        assertTrue(list.getTotal() > 0);
    }

    /**
     * Method: state(String topic)
     */
    @Test
    public void testState() throws Exception {
        TopicInfo info = this.weEventClient.state(this.topicName);
        assertTrue(info.getTopicName().equals(this.topicName));
    }


    /**
     * Method: getEvent(String eventId)
     */
    @Test(expected = BrokerException.class)
    public void testGetEvent() throws Exception {
        this.weEventClient.getEvent("not exist");
    }

    /**
     * Method: getSSLContext()
     */
    @Test
    public void testGetSSLContext() throws Exception {
        SSLContext sslContext = WeEventClient.getSSLContext();
        assertTrue(sslContext != null);
    }
}
