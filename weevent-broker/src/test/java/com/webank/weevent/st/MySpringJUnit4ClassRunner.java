package com.webank.weevent.st;


import com.webank.weevent.broker.config.FiscoConfig;

import lombok.extern.slf4j.Slf4j;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * MySpringJUnit4ClassRunner
 *
 * @author matthewliu
 * @since 2019/09/11
 */
@Slf4j
public class MySpringJUnit4ClassRunner extends SpringJUnit4ClassRunner {
    public MySpringJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);

        System.setProperty(FiscoConfig.propertiesFileKey, "fisco1.3-sample.properties");
    }
}
