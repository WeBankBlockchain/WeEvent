package com.webank.weevent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.webank.weevent.broker.fisco.RedisService;
import com.webank.weevent.sdk.WeEvent;

import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Junit base class.
 *
 * @author matthewliu
 * @version 1.0
 * @since 2019/02/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = BrokerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class JUnitTestBase {
    protected String groupId = WeEvent.DEFAULT_GROUP_ID;
    protected String topicName = "com.weevent.test";
    protected String blockNum = "1";

    @Value("${server.port}")
    public String listenPort;

    @Rule
    public TestName testName = new TestName();

    @Rule
    public Timeout timeout = new Timeout(120, TimeUnit.SECONDS);

    @Test
    public void testBuild() {
        Assert.assertTrue(true);
    }

    @Ignore
    public void redisServiceMockUp() {
        new MockUp<RedisService>(RedisService.class) {
            Map<String, List<WeEvent>> redisMap = new HashMap<>();

            {
                List<WeEvent> eventList = new ArrayList<>();
                WeEvent event = new WeEvent();
                event.setTopic(topicName);
                event.setContent("hello".getBytes());
                event.setEventId("317e7c4c-75-1");
                eventList.add(event);
                redisMap.put(blockNum, eventList);
            }

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
