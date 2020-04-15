package com.webank.weevent.broker;

import java.util.concurrent.TimeUnit;

import com.webank.weevent.client.WeEvent;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;

/**
 * Junit base class.
 *
 * @author matthewliu
 * @version 1.0
 * @since 2019/02/14
 */
public class JUnitTestBase {
    protected String groupId = WeEvent.DEFAULT_GROUP_ID;
    protected String topicName = "com.weevent.test";

    @Rule
    public TestName testName = new TestName();

    @Rule
    public Timeout timeout = new Timeout(60, TimeUnit.SECONDS);

    @Test
    public void testBuild() {
        Assert.assertTrue(true);
    }
}
