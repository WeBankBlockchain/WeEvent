package com.webank.weevent.broker.fisco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Slf4j
public class RedisServiceTest extends JUnitTestBase {

    private Map<String, List<WeEvent>> redisMap = new HashMap<>();

    private RedisService redisService;

    private String blockNum = "1";

    @Before
    public void before() {
        log.info("===================={}", this.testName.getMethodName());

        List<WeEvent> eventList = new ArrayList<>();
        WeEvent event = new WeEvent();
        event.setTopic(super.topicName);
        event.setContent("hello".getBytes());
        event.setEventId("317e7c4c-75-1");
        eventList.add(event);
        redisMap.put(this.blockNum, eventList);

        this.redisServiceMockUp();
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

        List<WeEvent> result = redisService.readEventsFromRedis(this.blockNum);
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

        Assert.assertTrue(redisService.isEventsExistInRedis(this.blockNum));
    }

    @Test
    public void testIsEventsNotExistInRedis() {
        log.info("===================={}", super.testName.getMethodName());

        Assert.assertFalse(redisService.isEventsExistInRedis("000000"));
    }

    @Ignore
    public void redisServiceMockUp() {
        new MockUp<RedisService>(RedisService.class) {
            @Mock
            public void writeEventsToRedis(String blockNum, List<WeEvent> list) {
                redisMap.put(blockNum, list);
            }

            @Mock
            public List<WeEvent> readEventsFromRedis(String blockNum) {
                return redisMap.get(blockNum);
            }

            @Mock
            public boolean isEventsExistInRedis(String blocknum) {
                return redisMap.containsKey(blocknum);
            }
        };
    }

}
