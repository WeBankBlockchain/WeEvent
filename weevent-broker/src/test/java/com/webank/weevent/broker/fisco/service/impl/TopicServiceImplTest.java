package com.webank.weevent.broker.fisco.service.impl;

import java.util.List;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ConsumerServiceImpl Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>11/09/2018</pre>
 */
@Slf4j
public class TopicServiceImplTest extends JUnitTestBase {
    private TopicServiceImpl topicService;

    @Before
    public void before() throws Exception {
        this.topicService = new TopicServiceImpl();
    }

    @After
    public void after() throws Exception {
        this.topicService = null;
    }

    /**
     * Method: getBlockHeight()
     */
    @Test
    public void testGetBlockHeight() throws Exception {
        log.info("===================={}", testName.getMethodName());

        assertTrue(this.topicService.getBlockHeight() > 1);
    }

    /**
     * Method: loop(Long blockNum)
     */
    @Test
    public void testLoop() throws Exception {
        log.info("===================={}", testName.getMethodName());

        IProducer iProducer = IProducer.build();
        iProducer.startProducer();
        SendResult sendResultDto = iProducer.publish(new WeEvent(this.topicName, "hello world.".getBytes()));
        assertEquals(SendResult.SendResultStatus.SUCCESS, sendResultDto.getStatus());
        log.info("publish, {}", sendResultDto.getEventId());

        Long block = Long.valueOf(sendResultDto.getEventId().split("-")[1]);
        List<WeEvent> events = this.topicService.loop(block);
        assertTrue(!events.isEmpty());
    }

    /**
     * Method: loop(Long blockNum, String topic)
     */
    @Test
    public void testLoop_check_01() throws Exception {
        log.info("===================={}", testName.getMethodName());

        List<WeEvent> events = this.topicService.loop(1L);
        assertTrue(events.isEmpty());
    }

    /**
     * Method: loop(Long blockNum, String topic)
     */
    @Test
    public void testLoop_check_02() throws Exception {
        log.info("===================={}", testName.getMethodName());

        List<WeEvent> events = this.topicService.loop(-1L);
        assertTrue(events.isEmpty());
    }
} 
