package com.webank.weevent.ST.plugin;

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.TopicPage;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * FiscoBcosBroker4Producer Tester.
 *
 * @author websterchen
 * @version 1.0
 * @since 11/08/2018
 */
@Slf4j
public class ProducerTest extends JUnitTestBase {
    private IProducer iProducer;
    private static String eventId = "";
    private String groupId = "1";
    private Map<String, String> extensions = new HashMap<>();

    @Before
    public void before() {
        try {
            iProducer = IProducer.build();
            extensions.put("weevent-url", "https://github.com/WeBankFinTech/WeEvent");
            Assert.assertTrue(iProducer != null);
            Assert.assertTrue(iProducer.open(this.topicName, groupId));
            SendResult result = iProducer.publish(new WeEvent(this.topicName, "你好吗？".getBytes(), extensions), groupId);
            eventId = result.getEventId();
        } catch (BrokerException e) {
            log.error("test method before error: ", e);
        }
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: test startProducer(String topic)
     */
    @Test
    public void testStartProducer() {
        try {
            Assert.assertTrue(iProducer.startProducer());
        } catch (BrokerException e) {
            log.error("start producer error:", e);
        }
    }

    /**
     * Method: test shutdownProducer()
     */
    @Test
    public void testShutdownProducer() {
        Assert.assertTrue(iProducer.shutdownProducer());
    }

    /**
     * Method: open topic exists
     */
    @Test
    public void testOpen1() {
        try {
            boolean result = iProducer.open(this.topicName, groupId);
            assertTrue(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertNull(e);
        }
    }

    /**
     * Method: open topic not exists
     */
    @Test
    public void testOpen2() {
        try {
            String topicStr = "testtopic" + System.currentTimeMillis();
            boolean result = iProducer.open(topicStr, groupId);
            assertTrue(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertNull(e);
        }
    }

    /**
     * Method: open topic length > 64
     */
    @Test
    public void testOpen3() {
        try {
            String topicStr = "topiclengthlonger64asdfghjklpoiuytrewqazxswcdevfrbg-" + System.currentTimeMillis();
            boolean result = iProducer.open(topicStr, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * Method: open topic length equal 64
     */
    @Test
    public void testOpen4() {
        try {
            String topicStr = "topiclengthequal64zxcvbnmlkjhgfdsaqwertyuioplokiuj-" + System.currentTimeMillis();
            boolean result = iProducer.open(topicStr, groupId);
            assertTrue(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertNull(e);
        }
    }

    /**
     * Method: open topic is null
     */
    @Test
    public void testOpen5() {
        try {
            boolean result = iProducer.open(null, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: open topic is ""
     */
    @Test
    public void testOpen6() {
        try {
            boolean result = iProducer.open("", groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error::", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: open topic is " "
     */
    @Test
    public void testOpen7() {
        try {
            boolean result = iProducer.open(" ", groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: open topic contain special character withoutin[32,128]
     */
    @Test
    public void testOpen8() {
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            boolean result = iProducer.open(illegalTopic, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * Method: open topic contain Chinese character
     */
    @Test
    public void testOpen9() {
        try {
            boolean result = iProducer.open("中国", groupId);
            assert (result);
        } catch (BrokerException e) {
            log.error("producer open error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * Method: Close this.topicName exists
     */
    @Test
    public void testClose1() {
        try {
            Assert.assertTrue(iProducer.close(this.topicName, groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertNull(e);
        }
    }

    /**
     * Method: Close topic is null
     */
    @Test
    public void testClose2() {
        try {
            Assert.assertFalse(iProducer.close(null, groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: Close topic为""
     */
    @Test
    public void testClose3() {
        try {
            Assert.assertFalse(iProducer.close("", groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: Close topic is " "
     */
    @Test
    public void testClose4() {
        try {
            Assert.assertFalse(iProducer.close(" ", groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * Method: Close topic length  > 64
     */
    @Test
    public void testClose5() {
        try {
            String topicStr = "topiclengthlonger64azxsqwedcvfrtgbnhyujmkiolpoiuytr-" 
        	    + System.currentTimeMillis();
            Assert.assertFalse(iProducer.close(topicStr, groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * Method: Close topic topic contain special character withoutin[32,128]
     */
    @Test
    public void testClose6() {
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            Assert.assertFalse(iProducer.close(illegalTopic, groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * Method: Close topic contain Chinese character
     */
    @Test
    public void testClose7() {
        try {
            Assert.assertFalse(iProducer.close("中国", groupId));
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }


    /**
     * topic exist topic exists
     */
    @Test
    public void testExist1() {
        try {
            boolean result = iProducer.exist(this.topicName, groupId);
            Assert.assertTrue(result);
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNull(e);
        }
    }

    /**
     * topic not exists
     */
    @Test
    public void testExist2() {
        try {
            String falseTopic = "fasssglsjgg";
            Assert.assertFalse(iProducer.exist(falseTopic, groupId));
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNull(e);
        }
    }

    /**
     * topic is null
     */
    @Test
    public void testExist3() {
        try {
            Assert.assertFalse(iProducer.exist(null, groupId));
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic length > 64
     */
    @Test
    public void testExist4() {
        try {
            String falseTopic = "fasssglsjggtyuioplkjhgfdsaqwezxcvqazxswedcvfrtgbnhyujmkiolpoiuytr";
            iProducer.exist(falseTopic, groupId);
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * topic blank
     */
    @Test
    public void testExist5() {
        try {
            String falseTopic = "";
            Assert.assertFalse(iProducer.exist(falseTopic, groupId));
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic blank
     */
    @Test
    public void testExist6() {
        try {
            String falseTopic = "  ";
            Assert.assertFalse(iProducer.exist(falseTopic, groupId));
        } catch (BrokerException e) {
            log.error("method Exist error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic length = 64 not exists
     */
    @Test
    public void testExist7() {
        try {
            String falseTopic = "sdfghjklpoiuytrewqazxcvbnmklopiuqazxswedcvfrtgbnhyujmkiolpoiuytr";
            boolean result = iProducer.exist(falseTopic, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            Assert.assertNull(e);
        }
    }

    /**
     * topic contain special character withoutin [32,128]
     */
    @Test
    public void testExist8() {
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            boolean result = iProducer.exist(illegalTopic, groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * topic contain Chinese character
     */
    @Test
    public void testExist9() {
        try {
            boolean result = iProducer.exist("中国", groupId);
            assertFalse(result);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * this.topicName is exists
     */
    @Test
    public void state1() {
        try {
            TopicInfo topicInfo = iProducer.state(this.topicName, groupId);
            Assert.assertTrue(topicInfo != null);
            Assert.assertTrue(!topicInfo.getTopicAddress().equals(""));
            Assert.assertTrue(!topicInfo.getSenderAddress().equals(""));
            Assert.assertTrue(!(topicInfo.getCreatedTimestamp() == 0));
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNull(e);
        }
    }

    /**
     * topic not exists
     */
    @Test
    public void state2() {
        TopicInfo topicInfo = null;
        try {
            String notExistTopic = "hdflsjglsg";
            topicInfo = iProducer.state(notExistTopic, groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
        }
    }

    /**
     * topic length > 64
     */
//    @Test
    public void state3() {
        TopicInfo topicInfo = null;
        try {
            String notExistTopic = "hdflsjglsgqwertyuioplkjhgfdsazxcvbqwertyuioplkjhgfdsazxcvbnmkoiujy";
            topicInfo = iProducer.state(notExistTopic, groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
    }

    /**
     * topic is null
     */
//    @Test
    public void state4() {
        TopicInfo topicInfo = null;
        try {
            topicInfo = iProducer.state(null, groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic is blank
     */
//    @Test
    public void state5() {
        TopicInfo topicInfo = null;
        try {
            topicInfo = iProducer.state("", groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic is blank topic " "
     */
//    @Test
    public void state6() {
        TopicInfo topicInfo = null;
        try {
            topicInfo = iProducer.state(" ", groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
    }

    /**
     * topic length = 64
     */
    @Test
    public void state7() {
        TopicInfo topicInfo = null;
        try {
            String notExistTopic = "hdflsjglsgqwertyuioplkjhgfdsazxcqazxswedcvfrtgbnhyujmkiolppoiuyt";
            topicInfo = iProducer.state(notExistTopic, groupId);
        } catch (BrokerException e) {
            log.error("method state error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
        }
    }

    /**
     * topic contain special character withoutin [32,128]
     */
//    @Test
    public void state8() {
        TopicInfo topicInfo = null;
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            topicInfo = iProducer.state(illegalTopic, groupId);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * topic contain Chinese character
     */
//    @Test
    public void state9() {
        TopicInfo topicInfo = null;
        try {
            topicInfo = iProducer.state("中国", groupId);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 10
     */
    @Test
    public void testList1() {
        Integer pageIndex = 0;
        Integer pageSize = 10;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage != null);
            Assert.assertTrue(topicPage.getTopicInfoList().size() > 0);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertTrue(e == null);
        }
    }

    /**
     * list test pageIndex = 1 & pageSize = 10
     */
    @Test
    public void testList2() {
        Integer pageIndex = 1;
        Integer pageSize = 10;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage != null);
            Assert.assertTrue(topicPage.getTotal() > 0);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertTrue(e == null);
        }
    }

    /**
     * list test pageIndex = null & pageSize = 10
     */
    @Test
    public void testList3() {
        Integer pageIndex = null;
        Integer pageSize = 10;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_INDEX_INVALID.getCode());
        }
    }

    /**
     * list test pageIndex < 0 & pageSize = 10
     */
    @Test
    public void testList4() {
        Integer pageIndex = -1;
        Integer pageSize = 10;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_INDEX_INVALID.getCode());
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = -1
     */
    @Test
    public void testList5() {
        Integer pageIndex = 0;
        Integer pageSize = -1;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertNull(topicPage);
        } catch (BrokerException e) {
            log.error("method list error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode());
        }

    }

    /**
     * list test pageIndex = 0 & pageSize = 100
     */
    @Test
    public void testList6() {
        Integer pageIndex = 0;
        Integer pageSize = 100;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage != null);
            Assert.assertTrue(topicPage.getTotal() > 0);
        } catch (BrokerException e) {
            Assert.assertTrue(e == null);
            log.error("method list error:", e);
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 101
     */
    @Test
    public void testList7() {
        Integer pageIndex = 0;
        Integer pageSize = 101;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage == null);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode());
            log.error("method list error:", e);
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = 0
     */
    @Test
    public void testList8() {
        Integer pageIndex = 0;
        Integer pageSize = 0;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage == null);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode());
            log.error("method list error:", e);
        }
    }

    /**
     * list test pageIndex = 0 & pageSize = null
     */
    @Test
    public void testList9() {
        Integer pageIndex = 0;
        Integer pageSize = null;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage == null);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_PAGE_SIZE_INVALID.getCode());
            log.error("method list error:", e);
        }
    }

    /**
     * list test pageIndex is out of all page
     */
    @Test
    public void testList10() {
        Integer pageIndex = 1000;
        Integer pageSize = 50;

        try {
            TopicPage topicPage = iProducer.list(pageIndex, pageSize, groupId);
            Assert.assertTrue(topicPage != null);
            Assert.assertTrue(topicPage.getTotal() > 0);
            Assert.assertTrue(topicPage.getTopicInfoList().size() == 0);
        } catch (BrokerException e) {
            Assert.assertNull(e);
            log.error("method list error:", e);
        }
    }

    /**
     * topic is exits and content is Chinese
     */
    @Test
    public void testPublishEventCharset1() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, "中文消息.".getBytes(), extensions), groupId);
            assertEquals(SendResult.SendResultStatus.SUCCESS, dto.getStatus());
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
        }
    }

    /**
     * topic is not exists
     */
    @Test
    public void testPublishEventCharset2() {
        try {
            String topicNotExists = "fsgdsggdgerer";
            SendResult dto = iProducer.publish(new WeEvent(topicNotExists, "中文消息.".getBytes(), extensions), groupId);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
            log.error("method PublishEventCharset error:", e);
        }
    }

    /**
     * topic is blank
     */
    @Test
    public void testPublishEventCharset3() {
        try {
            SendResult dto = iProducer.publish(new WeEvent("", "中文消息.".getBytes(), extensions), groupId);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("method PublishEventCharset error:", e);
        }
    }

    /**
     * topic is null
     */
    @Test
    public void testPublishEventCharset4() {
        try {
            SendResult dto = iProducer.publish(new WeEvent(null, "中文消息.".getBytes(), extensions), groupId);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("method PublishEventCharset error:", e);
        }
    }


    /**
     * topic length > 64
     */
    @Test
    public void testPublishEventCharset5() {
        try {
            String topicNotExists = "fsgdsggdgererqwertyuioplkjhgfdsazxqazwsxedcrfvtgbyhnujmikolppoiuyt";
            SendResult dto = iProducer.publish(new WeEvent(topicNotExists, "中文消息.".getBytes(), extensions), groupId);
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
            log.error("method PublishEventCharset error:", e);
        }
    }

    /**
     * topic is exits and content is null
     */
    @Test
    public void testPublishEventCharset6() {

        try {
            byte[] bytes = null;
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, bytes, extensions), groupId);
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_CONTENT_IS_BLANK.getCode());
        }
    }

    /**
     * topic is exits and content is blank
     */
    @Test
    public void testPublishEventCharset7() {

        try {
            byte[] bytes = "".getBytes();
            SendResult dto = iProducer.publish(new WeEvent(this.topicName, bytes, extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("method PublishEventCharset error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_CONTENT_IS_BLANK.getCode());
        }
    }

    /**
     * topic contain special character withoutin [32,128]
     */
    @Test
    public void testPublishEventCharset8() {

        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            byte[] bytes = "helloworld".getBytes();
            SendResult dto = iProducer.publish(new WeEvent(illegalTopic, bytes, extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * topic is Chinese character
     */
    @Test
    public void testPublishEventCharset9() {

        try {
            byte[] bytes = "".getBytes();
            SendResult dto = iProducer.publish(new WeEvent("中国", bytes, extensions), groupId);
            Assert.assertNull(dto);
        } catch (BrokerException e) {
            log.error("producer close error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
    }

    /**
     * test get Event : eventId is exists
     */
    @Test
    public void testGetEvent1() {
        try {
            WeEvent weEvent = iProducer.getEvent(eventId, groupId);
            assertEquals(weEvent.getEventId(), eventId);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
        }
    }

    /**
     * test get Event : eventId is illegal1
     */
    @Test
    public void testGetEvent2() {
        try {
            WeEvent weEvent = iProducer.getEvent("sfshfwefjf", groupId);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("get event error: ", e);
        }
    }

    /**
     * test get Event : eventId is illegal2
     */
    @Test
    public void testGetEvent3() {
        try {
            WeEvent weEvent = iProducer.getEvent("317e7c4c-75-hkhgjhg", groupId);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("get event error: ", e);
        }
    }

    /**
     * test get Event : eventId is legal but not exists & eventId > blockNumber
     */
    @Test
    public void testGetEvent4() {
        try {
            WeEvent weEvent = iProducer.getEvent("317e7c4c-75-32900000", groupId);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_MISMATCH.getCode());
            log.error("get event error: ", e);
        }
    }

    /**
     * test get Event : eventId is legal but not exists
     */
    @Test
    public void testGetEvent5() {
        try {
            WeEvent weEvent = iProducer.getEvent("317e7c4c-278-3", groupId);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_NOT_EXIST.getCode());
        }
    }

    /**
     * test get Event : eventId is null
     */
    @Test
    public void testGetEvent6() {
        try {
            WeEvent weEvent = iProducer.getEvent(null, groupId);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    /**
     * test get Event : eventId is blank
     */
    @Test
    public void testGetEvent7() {
        try {
            WeEvent weEvent = iProducer.getEvent("", groupId);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
        }
    }

    /**
     * test get Event : eventId is blank
     */
    @Test
    public void testGetEvent8() {
        try {
            WeEvent weEvent = iProducer.getEvent(" ", groupId);
        } catch (BrokerException e) {
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_IS_ILLEGAL.getCode());
            log.error("get event error: ", e);
        }
    }

    /**
     * test get Event : eventId length > 64
     */
    @Test
    public void testGetEvent9() {
        try {
            String id = "317e7c4csdxcfvbhjklpoutredwsaqsdfghjkoiuf-2782345678901234567-329";
            WeEvent weEvent = iProducer.getEvent(id, groupId);
        } catch (BrokerException e) {
            log.error("get event error: ", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_ID_EXCEEDS_MAX_LENGTH.getCode());
        }
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is exists ,content is Chinese
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack1() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(this.topicName, "中文消息.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is not exists
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack2() throws InterruptedException {
        try {
            String notExistsTopic = "sglsjhglsj";
            iProducer.publish(new WeEvent(notExistsTopic, "hello world.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_NOT_EXIST.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic length > 64
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack3() throws InterruptedException {
        try {
            String notExistsTopic = "qazwsxedcrfvtgbnhyujmkiolpoiuytrsglsjhglsjqwertyuioplkjhgfdsazxcvbnm";
            iProducer.publish(new WeEvent(notExistsTopic, "hello world.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishEventCallback error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_EXCEED_MAX_LENGTH.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack4() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(null, "hello world.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishEventCallback error:", e);
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is blank
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack5() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent("", "hello world.".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            Assert.assertNotNull(e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_IS_BLANK.getCode());
            log.error("method PublishEventCallback error:", e);
        }
        Thread.sleep(3000);
    }

    /**
     * content is null
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack6() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(this.topicName, null, extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_CONTENT_IS_BLANK.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * content is blank
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack7() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(this.topicName, "".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.ERROR, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.EVENT_CONTENT_IS_BLANK.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is exists ,content is English
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack8() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent(this.topicName, "helloWorld".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {
                    assertEquals(SendResult.SendResultStatus.SUCCESS, sendResult.getStatus());
                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic contain special character withoutin [32,128]
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack9() throws InterruptedException {
        char[] charStr = {69, 72, 31};
        try {
            String illegalTopic = new String(charStr);
            iProducer.publish(new WeEvent(illegalTopic, "helloWorld".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
        Thread.sleep(3000);
    }

    /**
     * Method: publish(WeEvent event, SendCallBack callBack) topic is Chinese character
     *
     * @throws InterruptedException
     */
    @Test
    public void testPublishForEventCallBack10() throws InterruptedException {
        try {
            iProducer.publish(new WeEvent("中国", "helloWorld".getBytes(), extensions), groupId, new IProducer.SendCallBack() {
                @Override
                public void onComplete(SendResult sendResult) {

                }

                @Override
                public void onException(Throwable e) {
                }
            });
        } catch (BrokerException e) {
            log.error("method PublishForEventCallBack error:", e);
            assertEquals(e.getCode(), ErrorCode.TOPIC_CONTAIN_INVALID_CHAR.getCode());
        }
        Thread.sleep(3000);
    }
}

