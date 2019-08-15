package com.webank.weevent.broker.plugin;

import com.webank.weevent.JUnitTestBase;

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
public class IConsumerTest extends JUnitTestBase {
    private IConsumer iConsumer;

    @Before
    public void before() {
        this.iConsumer = IConsumer.build();
    }

    @After
    public void after() {
    }

    /**
     * start Consumer Test
     */
    @Test
    public void testStartConsumer() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        Assert.assertTrue(this.iConsumer.startConsumer());
        Assert.assertTrue(this.iConsumer.isStarted());
    }

    /**
     * shutdownConsumer Test
     */
    @Test
    public void testShutdownConsumer() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        Assert.assertTrue(this.iConsumer.startConsumer());
        Assert.assertTrue(this.iConsumer.isStarted());
        Assert.assertTrue(this.iConsumer.shutdownConsumer());
    }

    /**
     * test shutdown Multiple 3 times
     */
    @Test
    public void testShutdownConsumer_multiple() throws Exception {
        log.info("===================={}", this.testName.getMethodName());

        Assert.assertTrue(this.iConsumer.startConsumer());
        Assert.assertTrue(this.iConsumer.isStarted());
        Assert.assertTrue(this.iConsumer.shutdownConsumer());
        Assert.assertFalse(this.iConsumer.isStarted());
        Assert.assertTrue(this.iConsumer.startConsumer());
        Assert.assertTrue(this.iConsumer.isStarted());
        Assert.assertTrue(this.iConsumer.shutdownConsumer());
        Assert.assertFalse(this.iConsumer.isStarted());
        Assert.assertTrue(this.iConsumer.shutdownConsumer());
        Assert.assertFalse(this.iConsumer.isStarted());
    }
}