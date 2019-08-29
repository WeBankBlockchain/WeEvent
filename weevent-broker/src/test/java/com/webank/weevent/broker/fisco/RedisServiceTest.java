package com.webank.weevent.broker.fisco;

import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class RedisServiceTest extends JUnitTestBase {

    private RedisService redisService;

    @Before
    public void before() {
        log.info("===================={}", this.testName.getMethodName());

        super.redisServiceMockUp();
        redisService = new RedisService();
    }

    @Test
    public void testWriteEventsToRedis() {
        log.info("===================={}", this.testName.getMethodName());

        String blockNum = "100";
        List<WeEvent> list = new ArrayList<>();
        WeEvent event = new WeEvent();
        event.setTopic(super.topicName);
        event.setContent("write to redis test".getBytes());
        event.setEventId("sdfsff-345-3");
        list.add(event);
        redisService.writeEventsToRedis(blockNum, list);
        Assert.assertTrue(true);

        List<WeEvent> result = redisService.readEventsFromRedis(blockNum);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.get(0).getTopic(), super.topicName);
    }

    @Test
    public void testReadEventsFromRedis() {
        log.info("===================={}", super.testName.getMethodName());

        List<WeEvent> result = redisService.readEventsFromRedis(super.blockNum);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.get(0).getTopic(), super.topicName);
    }

    @Test
    public void testReadEventsFromRedisBlockNumNotExist() {
        log.info("===================={}", super.testName.getMethodName());

        List<WeEvent> result = redisService.readEventsFromRedis("000000");
        Assert.assertNull(result);
    }

    @Test
    public void testIsEventsExistInRedis() {
        log.info("===================={}", super.testName.getMethodName());

        Assert.assertTrue(redisService.isEventsExistInRedis(super.blockNum));
    }

    @Test
    public void testIsEventsNotExistInRedis() {
        log.info("===================={}", super.testName.getMethodName());

        Assert.assertFalse(redisService.isEventsExistInRedis("000000"));
    }

}
