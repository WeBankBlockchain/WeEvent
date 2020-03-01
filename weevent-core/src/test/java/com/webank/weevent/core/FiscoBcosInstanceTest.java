package com.webank.weevent.core;

import java.util.HashMap;

import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * FiscoBcosInstance Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>03/01/2020</pre>
 */
@Slf4j
public class FiscoBcosInstanceTest extends JUnitTestBase {
    private String groupId = WeEvent.DEFAULT_GROUP_ID;
    private String topicName = "com.weevent.test";

    private FiscoConfig fiscoConfig;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.fiscoConfig = new FiscoConfig();
        Assert.assertTrue(this.fiscoConfig.load(""));
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: buildProducer()
     */
    @Test
    public void testBuildProducer() throws Exception {
        FiscoBcosInstance fiscoBcosInstance = new FiscoBcosInstance(this.fiscoConfig);
        IProducer iProducer = fiscoBcosInstance.buildProducer();
        iProducer.startProducer();

        WeEvent weEvent = new WeEvent(this.topicName, "hello weevent".getBytes());
        SendResult sendResult = iProducer.publish(weEvent, this.groupId, 10);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * Method: buildConsumer()
     */
    @Test
    public void testBuildConsumer() throws Exception {
        FiscoBcosInstance fiscoBcosInstance = new FiscoBcosInstance(this.fiscoConfig);
        IConsumer iConsumer = fiscoBcosInstance.buildConsumer();
        iConsumer.startConsumer();
        Assert.assertTrue(iConsumer.isStarted());

        String subscriptionId = iConsumer.subscribe(this.topicName, this.groupId, WeEvent.OFFSET_LAST, new HashMap<>(), new IConsumer.ConsumerListener() {
            @Override
            public void onEvent(String subscriptionId, WeEvent event) {

            }

            @Override
            public void onException(Throwable e) {

            }
        });

        Thread.sleep(3000);
        iConsumer.unSubscribe(subscriptionId);
    }
} 
