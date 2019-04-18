package com.webank.weevent.ST.sdk;

import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLContext;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;
import com.webank.weevent.sdk.WeEventClient;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * WeEventClient Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>04/09/2019</pre>
 */
@Slf4j
public class WeEventClientTest extends JUnitTestBase {
    private WeEventClient weEventClient;

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: publish(String topic, byte[] content)
     */
    @Test
    public void testPublish() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        SendResult sendResult = this.weEventClient.publish(topicName, "hello world".getBytes(StandardCharsets.UTF_8));
        assertTrue(sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * Method: publish(String topic, byte[] content)
     */
    @Test(expected = BrokerException.class)
    public void testPublish_001() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.weEventClient.publish(this.topicName, null);
    }

    @Test(expected = BrokerException.class)
    public void testPublish_002() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.weEventClient.publish(null, "hello world".getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Method: publish(String topic, byte[] content)
     */
    @Test(expected = BrokerException.class)
    public void testPublish_003() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.weEventClient.publish("111111111111111111111111111111111111111111111111111111", null);
    }

    /**
     * Method: subscribe(String topic, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {

            }
        });
        assertTrue(!subscriptionId.isEmpty());
    }

    /**
     * Method: unSubscribe(String subscriptionId)
     */
    @Test
    public void testUnSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        String subscriptionId = this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_LAST, new WeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {

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
     * Method: exist(String topic)
     */
    @Test
    public void testExist_001() throws Exception {
        boolean result = this.weEventClient.exist("not exist");
        assertFalse(result);
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
