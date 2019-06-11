package com.webank.weevent.broker.fisco;

import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.Assert.*;

@Slf4j
public class RedisServiceTest extends JUnitTestBase {
    
    @Autowired
    private RedisService redisService;
    
    private String blocknum = "1";
    
    @Before
    public void before() {
	List<WeEvent> list = new ArrayList<>();
	WeEvent event= new WeEvent();
	event.setTopic(this.topicName);
	event.setContent("hello".getBytes());
	event.setEventId("317e7c4c-75-1");
	list.add(event);
	redisService.writeEventsToRedis(blocknum, list);
    }

    @Test
    public void testWriteEventsToRedis() {
	
	String blockNum = "3";
	List<WeEvent> list = new ArrayList<>();
	WeEvent event= new WeEvent();
	event.setTopic("com.weevent.test");
	event.setContent("write to redis test".getBytes());
	event.setEventId("sdfsff-345-3");
	list.add(event);
	redisService.writeEventsToRedis(blockNum, list);
    }

    @Test
    public void testReadEventsFromRedis() {
	List<WeEvent> result = redisService.readEventsFromRedis(blocknum);
	assertNotNull(result);
	assertEquals(result.get(0).getTopic(), this.topicName);
    }

    @Test
    public void testIsEventsExistInRedis() {
	assertTrue(redisService.isEventsExistInRedis(blocknum));
    }

}
