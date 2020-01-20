package com.webank.weevent.governance;

import java.security.Security;
import java.util.concurrent.TimeUnit;

import com.webank.weevent.governance.utils.JwtUtils;

import org.junit.Assert;
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
@SpringBootTest(classes = GovernanceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class JUnitTestBase {
    @Value("${server.port}")
    public String listenPort;

    @Value("${ci.broker.ip}")
    private String ciBrokerIp;

    @Rule
    public TestName testName = new TestName();
    @Rule
    public Timeout timeout = new Timeout(120, TimeUnit.SECONDS);


    @Test
    public void testBuild() {
        Assert.assertTrue(true);
    }

    public  String getCiBrokerUrl() {
        return "http://" + ciBrokerIp + "/weevent";
    }

    public String createToken() {
        String token = JwtUtils.encodeToken("admin", GovernanceApplication.environment.getProperty("jwt.private.secret"), JwtUtils.EXPIRE_TIME);
        Security.setProperty(token, "1");
        return token;
    }
}
