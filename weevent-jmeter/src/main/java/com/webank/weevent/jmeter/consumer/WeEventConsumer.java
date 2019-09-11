package com.webank.weevent.jmeter.consumer;


import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.WeEvent;

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
    private String topic = "com.webank.weevent.jmeter";

    private String groupId = WeEvent.DEFAULT_GROUP_ID;

    private IWeEventClient weEventClient;

    private final String http = "http://";

    private String defaultUrl = "127.0.0.1:7000";

    // 每个压测线程启动时跑一次
    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        getNewLogger().info("this is consumer setupTest");
        super.setupTest(context);
        try {
            this.defaultUrl = context.getParameter("url") == null ? this.defaultUrl : context.getParameter("url");
            this.weEventClient = IWeEventClient.build(http + defaultUrl + "/weevent");
            getNewLogger().info("weEventClient:{}", this.weEventClient);

            this.topic = context.getParameter("topic") == null ? this.topic : context.getParameter("topic");
            this.groupId = context.getParameter("groupId") == null ? WeEvent.DEFAULT_GROUP_ID : context.getParameter("groupId");
            boolean result = this.weEventClient.open(this.topic);
            getNewLogger().info("open topic result: {}", result);
        } catch (BrokerException e) {
            getNewLogger().error("open ClientException", e);
        }
    }

    // 每次runTest运行完执行
    @Override
    public void teardownTest(JavaSamplerContext context) {
        super.teardownTest(context);
    }

    // Jmeter GUI参数
    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("topic", this.topic);
        arguments.addArgument("groupId", this.groupId);
        arguments.addArgument("url", this.defaultUrl);
        return arguments;
    }

    // Jmeter跑一次runTest算一个事物，会重复跑
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel("consumer");
        try {
            result.sampleStart();
            String subscribeId = this.weEventClient.subscribe(this.topic, this.groupId, WeEvent.OFFSET_LAST, new IWeEventClient.EventListener() {
                @Override
                public void onEvent(WeEvent event) {
                    getNewLogger().info("event,{}", event);
                }

                @Override
                public void onException(Throwable e) {
                    getNewLogger().error("subscribe Exception", e);
                }
            });
            result.setSuccessful(true);
            result.setResponseMessage(subscribeId);
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

