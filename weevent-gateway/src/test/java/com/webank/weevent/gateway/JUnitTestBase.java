package com.webank.weevent.gateway;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
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
@SpringBootTest(classes = GatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class JUnitTestBase {
    @Rule
    public TestName testName = new TestName();

    @Rule
    public Timeout timeout = new Timeout(60, TimeUnit.SECONDS);

    @Test
    public void testBuild() {
        Assert.assertTrue(true);
    }
}
