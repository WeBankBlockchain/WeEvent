package com.webank.weevent.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * IWeEventClient.Builder Tester.
 *
 * @author v_wbhwliu
 * @version 1.3
 * @since 2020/04/16
 */

@Slf4j
public class WeEventClientBuilderTest {

    private String topicName = "com.weevent.test";

    /**
     * Method: IWeEventClient.builder().brokerUrl()
     */
    @Test
    public void testBuilderBrokerUrl() throws Exception {
        IWeEventClient weEventClient = IWeEventClient.builder().brokerUrl("http://localhost:7000/weevent-broker").build();
        boolean result = weEventClient.open(topicName);
        Assert.assertTrue(result);
    }

    /**
     * Method: IWeEventClient.builder().brokerUrl().groupId()
     */
    @Test
    public void testBuilderGroupId() throws Exception {
        IWeEventClient weEventClient = IWeEventClient.builder().brokerUrl("http://localhost:7000/weevent-broker").groupId(WeEvent.DEFAULT_GROUP_ID).build();
        boolean result = weEventClient.open(topicName);
        Assert.assertTrue(result);
    }

    /**
     * Method: IWeEventClient.builder().brokerUrl().userName().password()
     */
    @Test
    public void testBuilderUserNamePassword() throws Exception {
        IWeEventClient weEventClient = IWeEventClient.builder().brokerUrl("http://localhost:7000/weevent-broker").userName("").password("").build();
        boolean result = weEventClient.open(topicName);
        Assert.assertTrue(result);
    }

    /**
     * Method: IWeEventClient.builder().brokerUrl().timeout()
     */
    @Test
    public void testBuilderTimeout() throws Exception {
        IWeEventClient weEventClient = IWeEventClient.builder().brokerUrl("http://localhost:7000/weevent-broker").timeout(5000).build();
        boolean result = weEventClient.open(topicName);
        Assert.assertTrue(result);
    }
}
