package com.webank.weevent.broker.fisco;

import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class RedisServiceTest extends JUnitTestBase {
    @Autowired
    private RedisService redisService;

    private String blockNum = "1";

    @Before
    public void before() {
        log.info("===================={}", this.testName.getMethodName());

        List<WeEvent> list = new ArrayList<>();
        WeEvent event = new WeEvent();
        event.setTopic(this.topicName);
        event.setContent("hello".getBytes());
        event.setEventId("317e7c4c-75-1");
        list.add(event);
        redisService.writeEventsToRedis(blockNum, list);
    }

    @Test
    public void testWriteEventsToRedis() {
        log.info("===================={}", this.testName.getMethodName());

        String blockNum = "3";
        List<WeEvent> list = new ArrayList<>();
        WeEvent event = new WeEvent();
        event.setTopic("com.weevent.test");
        event.setContent("write to redis test".getBytes());
        event.setEventId("sdfsff-345-3");
        list.add(event);
        redisService.writeEventsToRedis(blockNum, list);
        Assert.assertTrue(true);
    }

    @Test
    public void testReadEventsFromRedis() {
        log.info("===================={}", this.testName.getMethodName());

        List<WeEvent> result = redisService.readEventsFromRedis(blockNum);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.get(0).getTopic(), this.topicName);
    }

    @Test
    public void testIsEventsExistInRedis() {
        log.info("===================={}", this.testName.getMethodName());

        Assert.assertTrue(redisService.isEventsExistInRedis(blockNum));
    }

}
