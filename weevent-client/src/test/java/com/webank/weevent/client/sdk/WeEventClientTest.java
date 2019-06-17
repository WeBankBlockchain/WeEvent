package com.webank.weevent.client.sdk;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import com.webank.weevent.sdk.BrokerException;
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

import static org.junit.Assert.assertFalse;
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
    private Map<String, String> extensions = new HashMap<>();
    @Rule
    public TestName testName = new TestName();

    public String topicName = "com.webank.weevent";

    public String groupId = "1";

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
     * Method: publish(String topic, String groupId, byte[] content, Map<String, String> extensions)
     */
    @Test
    public void testPublishGroupId() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // test groupId
        SendResult sendResult = this.weEventClient.publish(topicName, groupId, "hello world".getBytes(StandardCharsets.UTF_8), extensions);
        assertTrue(sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS);
    }


    /**
     * Method: publish(String topic, byte[] content, Map<String, String> extensions)
     */
    @Test
    public void testPublish() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        SendResult sendResult = this.weEventClient.publish(topicName, "hello world".getBytes(StandardCharsets.UTF_8), extensions);
        assertTrue(sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * Method: publish(String topic, String groupId, byte[] content, Map<String, String> extensions)
     */
    @Test(expected = BrokerException.class)
    public void testPublish_001() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.weEventClient.publish(this.topicName, groupId, null, null);
    }

    @Test(expected = BrokerException.class)
    public void testPublish_002() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.weEventClient.publish(null, groupId, "hello world".getBytes(StandardCharsets.UTF_8), extensions);
    }


    @Test(expected = BrokerException.class)
    public void testPublish_004() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // test groupId
        this.weEventClient.publish(topicName, groupId, "hello world".getBytes(StandardCharsets.UTF_8), extensions);
    }

    @Test(expected = BrokerException.class)
    public void testPublish_005() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // test groupId
        this.weEventClient.publish(topicName, groupId, "hello world".getBytes(StandardCharsets.UTF_8), null);
    }


    /**
     * Method: publish(String topic, byte[] content)
     */
    @Test(expected = BrokerException.class)
    public void testPublish_003() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        this.weEventClient.publish("111111111111111111111111111111111111111111111111111111", null, null, extensions);
    }


    /**
     * Method: subscribe(String topic, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribe() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // create subscriber
        this.weEventClient.subscribe(this.topicName, WeEvent.OFFSET_FIRST, new WeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                System.out.println(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(60000);
    }

    /**
     * Method: subscribe(String topic, String offset, IConsumer.ConsumerListener listener)
     */
    @Test
    public void testSubscribe_01() throws Exception {
        log.info("===================={}", this.testName.getMethodName());
        // create subscriber
        this.weEventClient.subscribe(this.topicName, "317e7c4c-8-26", new WeEventClient.EventListener() {
            @Override
            public void onEvent(WeEvent event) {
                log.info(event.toString());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        });
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
     * Method: open(String topic,String groupId)
     */
    @Test
    public void testOpenGroupId() throws Exception {
        boolean result = this.weEventClient.open(this.topicName, this.groupId);
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
     * Method: testCloseGroupId(String topic,String groupId)
     */
    @Test
    public void testCloseGroupId() throws Exception {
        boolean result = weEventClient.close(topicName, groupId);
        assertTrue(result);
    }


    /**
     * Method: exist(String topic)
     */
    @Test
    public void testExistGroupId() throws Exception {
        boolean result = this.weEventClient.exist(this.topicName, groupId);
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
        boolean result = this.weEventClient.exist("not exist", groupId);
        assertFalse(result);
    }

    /**
     * Method: testListGroupId(Integer pageIndex, Integer pageSize)
     */
    @Test
    public void testListGroupId() throws Exception {
        TopicPage list = this.weEventClient.list(0, 10, groupId);
        assertTrue(list.getTotal() > 0);
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
    public void testStateGroupId() throws Exception {
        TopicInfo info = this.weEventClient.state(this.topicName, groupId);
        assertTrue(info.getTopicName().equals(this.topicName));
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
     * Method: testGetEventGroupId(String eventId,String groupId)
     */
    @Test(expected = BrokerException.class)
    public void testGetEventGroupId() throws Exception {
        this.weEventClient.getEvent("not exist", groupId);
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
