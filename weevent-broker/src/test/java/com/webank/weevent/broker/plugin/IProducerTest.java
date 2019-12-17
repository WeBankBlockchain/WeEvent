package com.webank.weevent.broker.plugin;

import java.util.concurrent.TimeUnit;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tester.
 *
 * @author matthewliu
 * @version 1.0
 * @since 11/08/2018
 */
@Slf4j
public class IProducerTest extends JUnitTestBase {
    private IProducer iProducer;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.iProducer = BrokerApplication.applicationContext.getBean("iProducer", IProducer.class);
        Assert.assertNotNull(this.iProducer);
        Assert.assertTrue(this.iProducer.open(this.topicName, this.groupId));
    }

    @After
    public void after() {
        this.iProducer.shutdownProducer();
    }

    /**
     * start producer test
     */
    @Test
    public void testStartProducer() throws Exception {
        Assert.assertTrue(this.iProducer.startProducer());
    }

    /**
     * shutdown producer
     */
    @Test
    public void testShutdownProducer() {
        Assert.assertTrue(this.iProducer.shutdownProducer());
    }

    /**
     * test shutdownProducer
     */
    @Test
    public void testShutdownProducerMultiple() {
        Assert.assertTrue(this.iProducer.shutdownProducer());
        Assert.assertTrue(this.iProducer.shutdownProducer());
    }

    /**
     * test publish
     */
    public void testPublish() throws Exception {
        SendResult result = this.iProducer.publish(new WeEvent(this.topicName, "hello world".getBytes()), this.groupId).get(transactionTimeout, TimeUnit.MILLISECONDS);
        Assert.assertEquals(result.getStatus(), SendResult.SendResultStatus.SUCCESS);
        Assert.assertFalse(result.getEventId().isEmpty());
    }
}
