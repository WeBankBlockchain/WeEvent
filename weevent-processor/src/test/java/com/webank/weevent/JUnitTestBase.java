package com.webank.weevent;

import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Junit base class.
 *
 * @author cristic
 * @version 1.0
 * @since 2019/09/01
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class JUnitTestBase {
    @Value("${server.port}")
    public String listenPort;

    @Rule
    public TestName testName = new TestName();

    @Rule
    public Timeout timeout = new Timeout(120, TimeUnit.SECONDS);

    @Test
    public void testCaseVoid() {

    }
}
