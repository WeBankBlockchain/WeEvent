package com.webank.weevent.jmeter.consumer;


import java.nio.charset.Charset;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.IWeEventClient;
import com.webank.weevent.client.WeEvent;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;

/**
 * WeEvent consumer performance.
 * puremilkfan
 *
 * @since 2019/09/11
 */

public class WeEventConsumer extends AbstractJavaSamplerClient {
    private String topic = "com.weevent.test.jmeter";

    private String groupId = WeEvent.DEFAULT_GROUP_ID;

    private IWeEventClient weEventClient;

    private String defaultUrl = "http://127.0.0.1:7000/weevent-broker";

    // Run every time the pressure thread starts
    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        getNewLogger().info("this is consumer setupTest");
        super.setupTest(context);
        try {
            this.defaultUrl = context.getParameter("url") == null ? this.defaultUrl : context.getParameter("url");

            this.topic = context.getParameter("topic") == null ? this.topic : context.getParameter("topic");
            this.groupId = context.getParameter("groupId") == null ? WeEvent.DEFAULT_GROUP_ID : context.getParameter("groupId");
            this.weEventClient = new IWeEventClient.Builder().brokerUrl(defaultUrl).groupId(this.groupId).build();
            getNewLogger().info("weEventClient:{}", this.weEventClient);
            boolean result = this.weEventClient.open(this.topic);
            getNewLogger().info("open topic result: {}", result);
        } catch (BrokerException e) {
            getNewLogger().error("open ClientException", e);
        }
    }

    // Execute every runTest run
    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }

    // JMeter GUI parameters
    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("topic", this.topic);
        arguments.addArgument("groupId", this.groupId);
        arguments.addArgument("url", this.defaultUrl);
        return arguments;
    }

    // Jmeter runs once runTest to calculate a thing, it will run repeatedly
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel("consumer");
        try {
            this.weEventClient = new IWeEventClient.Builder().brokerUrl(defaultUrl).groupId(this.groupId).build();
            result.sampleStart();
            String subscribeId = this.weEventClient.subscribe(this.topic, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    getNewLogger().info("eventId,{}", event.getEventId());
                }

                @Override
                public void onException(Throwable e) {
                    getNewLogger().error("subscribe Exception", e);
                }
            });
            result.setSuccessful(true);
            result.setResponseMessage(subscribeId);
            result.setResponseData(subscribeId, Charset.defaultCharset().name());
            result.setResponseHeaders("subscribe success");

            result.sampleEnd();
        } catch (Exception e) {
            getNewLogger().error("subscribe exception", e);
            result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseMessage(e.getMessage());
        }
        return result;
    }

    @Override
    protected Logger getNewLogger() {
        return super.getNewLogger();
    }
}

