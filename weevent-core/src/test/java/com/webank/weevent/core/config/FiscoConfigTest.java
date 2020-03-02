package com.webank.weevent.core.config;

import com.webank.weevent.core.JUnitTestBase;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * FiscoConfig Tester.
 *
 * @author matthewliu
 * @version 1.0
 * @since 03/01/2020
 */
@Slf4j
public class FiscoConfigTest extends JUnitTestBase {
    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }

    /**
     * test load
     */
    @Test
    public void testLoadDefault() {
        FiscoConfig fiscoConfig = new FiscoConfig();
        Assert.assertTrue(fiscoConfig.load(""));
        // default value is empty
        Assert.assertFalse(fiscoConfig.getNodes().isEmpty());
    }

    /**
     * test load
     */
    @Test
    public void testLoad() {
        FiscoConfig fiscoConfig = new FiscoConfig();
        Assert.assertTrue(fiscoConfig.load("classpath:fisco.properties"));
        // default value is empty
        Assert.assertFalse(fiscoConfig.getNodes().isEmpty());
    }
}

