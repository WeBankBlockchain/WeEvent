package com.webank.weevent.core.config;

import com.webank.weevent.core.JUnitTestBase;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * FiscoConfig Tester.
 *
 * @author matthewliu
 * @version 1.0
 * @since 03/01/2020
 */
@Slf4j
public class FiscoConfigTest extends JUnitTestBase {
	
	public FiscoConfig fiscoConfig;
	
	@Autowired
	public void setFiscoConfig(FiscoConfig fiscoConfig) {
		this.fiscoConfig = fiscoConfig;
	}
	
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
        // default value is empty
        Assert.assertFalse(fiscoConfig.getNodes().isEmpty());
    }

    /**
     * test load
     */
    @Test
    public void testLoad() {
        // default value is empty
        Assert.assertFalse(fiscoConfig.getNodes().isEmpty());
    }
}

