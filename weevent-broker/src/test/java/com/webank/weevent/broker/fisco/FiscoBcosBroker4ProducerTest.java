package com.webank.weevent.broker.fisco;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * FiscoBcosBroker4Producer Tester.
 *
 * @author websterchen
 * @version 1.0
 * @since 11/08/2018
 */
@Slf4j
public class FiscoBcosBroker4ProducerTest extends JUnitTestBase {
    private IProducer iProducer;
    private String groupId = "1";
    private Map<String, String> extensions = new HashMap<>();

    @Before
    public void before() throws Exception {
        extensions.put("weevent-url", "https://github.com/WeBankFinTech/WeEvent");
        iProducer = IProducer.build();
        iProducer.startProducer();
        assertTrue(iProducer != null);
        assertTrue(iProducer.open(this.topicName, groupId));
    }

    /**
     * Method: startProducer(String topic)
     */
    @Test
    public void testStartProducer() throws Exception {
        assertTrue(iProducer.startProducer());
    }

    /**
     * Method: shutdownProducer()
     */
    @Test
    public void testShutdownProducer() throws Exception {
        assertTrue(iProducer.shutdownProducer());
    }

    @Test
    public void testExist() throws Exception {
        assertTrue(iProducer.exist(this.topicName, groupId));
    }

    @Test
    public void state() throws Exception {
        TopicInfo topicInfo = iProducer.state(this.topicName, groupId);
        assertTrue(topicInfo != null);
        assertTrue(!topicInfo.getTopicAddress().equals(""));
        assertTrue(!topicInfo.getSenderAddress().equals(""));
        assertTrue(topicInfo.getCreatedTimestamp() != 0);
    }

    @Test
    public void testList() throws Exception {
        Integer pageIndex = 1;
        Integer pageSize = 10;

        TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
        assertTrue(topicPage != null);
        assertTrue(topicPage.getTotal() > 0);
    }

    @Test
    public void testPublishEventCharsetPerformances() throws Exception {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                try {
                    SendResult dto = iProducer.publish(new WeEvent(this.topicName, "中文消息.".getBytes(), extensions), groupId);
                    assertEquals(SendResult.SendResultStatus.SUCCESS, dto.getStatus());
                    sleep(100);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }).start();
        }
    }

    /**
     * Method: publish(WeEvent event)
     */
    @Test
    public void testPublishEvent() throws Exception {
        SendResult dto = iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes(), extensions), groupId);
        assertEquals(SendResult.SendResultStatus.SUCCESS, dto.getStatus());
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack)
     */
    @Test
    public void testPublishForEventCallBack() throws Exception {
        iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
            @Override
            public void onComplete(SendResult sendResult) {
                assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
            }

            @Override
            public void onException(Throwable e) {
                log.error(e.getMessage());
            }
        });
    }
}
