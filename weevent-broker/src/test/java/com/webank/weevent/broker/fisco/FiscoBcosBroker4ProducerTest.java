package com.webank.weevent.broker.fisco;

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

    @Before
    public void before() throws Exception {
        iProducer = IProducer.build();
        assertTrue(iProducer != null);
        assertTrue(iProducer.open(this.topicName));
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
        assertTrue(iProducer.exist(this.topicName));
    }

    @Test
    public void state() throws Exception {
        TopicInfo topicInfo = iProducer.state(this.topicName);
        assertTrue(topicInfo != null);
        assertTrue(!topicInfo.getTopicAddress().equals(""));
        assertTrue(!topicInfo.getSenderAddress().equals(""));
        assertTrue(topicInfo.getCreatedTimestamp() != 0);
    }

    @Test
    public void testList() throws Exception {
        Integer pageIndex = 1;
        Integer pageSize = 10;

        TopicPage topicPage = iProducer.list(pageIndex, pageSize);
        assertTrue(topicPage != null);
        assertTrue(topicPage.getTotal() > 0);
    }

    @Test
    public void testPublishEventCharsetPerformances() throws Exception {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                try {
                    SendResult dto = iProducer.publish(new WeEvent(this.topicName, "中文消息.".getBytes()));
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
        SendResult dto = iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()));
        assertEquals(SendResult.SendResultStatus.SUCCESS, dto.getStatus());
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack)
     */
    @Test
    public void testPublishForEventCallBack() throws Exception {
        iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()), new IProducer.SendCallBack() {
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
