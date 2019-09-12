package com.webank.weevent.broker.task;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * NotifyTask Tester.
 *
 * @author matthewliu
 * @version 1.0
 * @since 09/02/2019
 */
@Slf4j
public class SubscriptionTest extends JUnitTestBase {
    private final static String patternTopic = "com/weevent/test";
    private final static String tag = "tag_name";
    private final static byte[] content = "hello world".getBytes(StandardCharsets.UTF_8);

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }

    /**
     * test isTopicPattern
     */
    @Test
    public void testIsTopicPattern() {
        Assert.assertTrue(Subscription.isTopicPattern("#"));
        Assert.assertTrue(Subscription.isTopicPattern("/#"));
        Assert.assertTrue(Subscription.isTopicPattern("#/"));
        Assert.assertTrue(Subscription.isTopicPattern("+"));
        Assert.assertTrue(Subscription.isTopicPattern("/+"));
        Assert.assertTrue(Subscription.isTopicPattern("+/"));
        Assert.assertTrue(Subscription.isTopicPattern("com/#"));
        Assert.assertTrue(Subscription.isTopicPattern("com/+"));
        Assert.assertTrue(Subscription.isTopicPattern("com/+/test"));

        Assert.assertFalse(Subscription.isTopicPattern("com/"));
        Assert.assertFalse(Subscription.isTopicPattern(""));
    }

    /**
     * test validateTopicPattern
     */
    @Test
    public void testValidateTopicPattern() throws Exception {
        Subscription.validateTopicPattern("#");
        Subscription.validateTopicPattern("/#");
        Subscription.validateTopicPattern("#/");
        Subscription.validateTopicPattern("+");
        Subscription.validateTopicPattern("/+");
        Subscription.validateTopicPattern("+/");
        Subscription.validateTopicPattern("com/#");
        Subscription.validateTopicPattern("com/+");
        Subscription.validateTopicPattern("com/+/test");
        Assert.assertTrue(true);
    }

    /**
     * test validateTopicPattern
     */
    @Test
    public void testValidateTopicPattern1() {

        try {
            Subscription.validateTopicPattern("com/#/test");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.PATTERN_INVALID.getCode());
        }
    }

    /**
     * test validateTopicPattern
     */
    @Test
    public void testValidateTopicPattern3() {
        try {
            Subscription.validateTopicPattern("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            Assert.fail();
        } catch (BrokerException e) {
            Assert.assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * filter by topic
     */
    @Test
    public void testFilter() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(topicName, content));

        String[] topics = {topicName};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * empty from
     */
    @Test
    public void testFilter2() {
        List<WeEvent> from = new ArrayList<>();

        String[] topics = {topicName};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertTrue(to.isEmpty());
    }

    /**
     * empty topics
     */
    @Test
    public void testFilter3() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(topicName, content));

        String[] topics = {};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertTrue(to.isEmpty());
    }

    /**
     * filter topic, 2 event
     */
    @Test
    public void testFilter4() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(topicName, content));
        from.add(new WeEvent(topicName, content));

        String[] topics = {topicName};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 2);
    }

    /**
     * filter topic, 2 topic
     */
    @Test
    public void testFilter5() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(topicName, content));
        from.add(new WeEvent(topicName, content));

        String[] topics = {topicName, "topic"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 2);
    }

    /**
     * filter tag
     */
    @Test
    public void testFilterTag() {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);

        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(topicName, content, ext));

        String[] topics = {topicName};
        List<WeEvent> to = Subscription.filter(from, topics, tag);

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * empty tag
     */
    @Test
    public void testFilterTag2() {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);

        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(topicName, content, ext));

        String[] topics = {topicName};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * 2 topic
     */
    @Test
    public void testFilterTag3() {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);

        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(topicName, content, ext));

        String[] topics = {topicName, "topic"};
        List<WeEvent> to = Subscription.filter(from, topics, tag);

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * 2 event
     */
    @Test
    public void testFilterTag4() {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);

        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(topicName, content, ext));
        from.add(new WeEvent(topicName, content, ext));

        String[] topics = {topicName, "topic"};
        List<WeEvent> to = Subscription.filter(from, topics, tag);

        Assert.assertEquals(to.size(), 2L);
    }

    /**
     * other tag
     */
    @Test
    public void testFilterTag5() {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);

        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(topicName, content, ext));
        from.add(new WeEvent(topicName, content, ext));

        String[] topics = {topicName, "topic"};
        List<WeEvent> to = Subscription.filter(from, topics, "other");

        Assert.assertTrue(to.isEmpty());
    }

    /**
     * filter pattern, all
     */
    @Test
    public void testFilterPatternSharp() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));

        String[] topics = {"#"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * second layer
     */
    @Test
    public void testFilterPatternSharp2() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));

        String[] topics = {"com/#"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * third layer
     */
    @Test
    public void testFilterPatternSharp3() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));

        String[] topics = {"com/weevent/#"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * exceed layer
     */
    @Test
    public void testFilterPatternSharp4() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));

        String[] topics = {"com/weevent/test/#", "topic"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertTrue(to.isEmpty());
    }

    /**
     * exceed layer
     */
    @Test
    public void testFilterPatternSharp5() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));

        String[] topics = {"com/weevent/test/a/#"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertTrue(to.isEmpty());
    }

    /**
     * with tag
     */
    @Test
    public void testFilterPatternSharp6() {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);

        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content, ext));

        String[] topics = {"com/weevent/#", topicName};
        List<WeEvent> to = Subscription.filter(from, topics, tag);

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * # not in last layer
     */
    @Test
    public void testFilterPatternSharp7() {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);

        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content, ext));

        String[] topics = {"com/#/test", topicName};
        List<WeEvent> to = Subscription.filter(from, topics, tag);

        Assert.assertTrue(to.isEmpty());
    }

    /**
     * +
     */
    @Test
    public void testFilterPatternSingle() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));

        String[] topics = {"com/weevent/+"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * wrong format, means always false
     */
    @Test
    public void testFilterPatternSingle1() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));

        String[] topics = {"com/#/+"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertTrue(to.isEmpty());
    }

    /**
     * +
     */
    @Test
    public void testFilterPatternSingle2() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));

        String[] topics = {"com/weevent/+"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * two +
     */
    @Test
    public void testFilterPatternSingle3() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));

        String[] topics = {"+/weevent/+"};
        List<WeEvent> to = Subscription.filter(from, topics, "");

        Assert.assertEquals(to.size(), 1L);
    }

    /**
     * complex
     */
    @Test
    public void testFilterComplex() {
        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content));
        from.add(new WeEvent(patternTopic, content));
        from.add(new WeEvent(topicName, content));

        String[] topics = {"com/weevent/+", topicName};
        List<WeEvent> to = Subscription.filter(from, topics, "");
        Assert.assertEquals(to.size(), 3L);
    }

    /**
     * complex
     */
    @Test
    public void testFilterComplex2() {
        Map<String, String> ext = new HashMap<>();
        ext.put(WeEvent.WeEvent_TAG, tag);

        List<WeEvent> from = new ArrayList<>();
        from.add(new WeEvent(patternTopic, content, ext));
        from.add(new WeEvent(patternTopic, content));
        from.add(new WeEvent(topicName, content, ext));

        String[] topics = {"com/weevent/+", topicName};
        List<WeEvent> to = Subscription.filter(from, topics, tag);
        Assert.assertEquals(to.size(), 2L);
    }
}

