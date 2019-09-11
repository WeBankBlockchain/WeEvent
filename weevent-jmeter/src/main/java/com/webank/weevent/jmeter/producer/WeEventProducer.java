
package com.webank.weevent.jmeter.producer;


/**
 * WeEvent publish performance.
 * puremilkfan
 *
 * @since 2019/09/11
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
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
 * @since 2019/09/10
 */


public class WeEventProducer extends AbstractJavaSamplerClient {
    private String topic = "com.webank.weevent.jmeter";
    private String content = "this is jmeter test!";
    private String format = "json";

    private Map<String, String> extensions = new ConcurrentHashMap<>();

    private String groupId = WeEvent.DEFAULT_GROUP_ID;

    private IWeEventClient weEventClient;

    private WeEvent weEvent;

    private final String http = "http://";

    private static String defaultUrl = "127.0.0.1:7000";

    // 每个压测线程启动时跑一次
    @Override
    public void setupTest(JavaSamplerContext context) {
        getNewLogger().info("this is producer setupTest");
        super.setupTest(context);
        try {
            this.defaultUrl = context.getParameter("url") == null ? this.defaultUrl : context.getParameter("url");
            this.weEventClient = IWeEventClient.build(http + defaultUrl + "/weevent");
            getNewLogger().info("weEventClient:{}", this.weEventClient);

            this.topic = context.getParameter("topic") == null ? this.topic : context.getParameter("topic");
            this.content = context.getParameter("content") == null ? this.content : context.getParameter("content");
            this.format = context.getParameter("format") == null ? this.format : context.getParameter("format");
            this.groupId = context.getParameter("groupId") == null ? WeEvent.DEFAULT_GROUP_ID : context.getParameter("groupId");
            extensions.put(WeEvent.WeEvent_FORMAT, format);
            weEvent = new WeEvent(this.topic, this.content.getBytes(), this.extensions);
            getNewLogger().info("weEvent:{}", weEvent);

            boolean result = this.weEventClient.open(this.topic);
            getNewLogger().info("open topic result: {}", result);
        } catch (BrokerException e) {
            getNewLogger().error("open ClientException", e);
        }
    }

    // 每次runTest运行完执行
    @Override
    public void teardownTest(JavaSamplerContext context) {
        getNewLogger().debug(getClass().getName() + ": teardownTest");

    }

    // JMeter GUI params
    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("topic", this.topic);
        arguments.addArgument("content", this.content);
        arguments.addArgument("format", this.format);
        arguments.addArgument("url", this.defaultUrl);
        return arguments;
    }

    // Jmeter跑一次runTest算一个事物，会重复跑
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel("producer");
        try {

            result.sampleStart();
            SendResult sendResult = this.weEventClient.publish(this.weEvent, this.groupId);
            result.sampleEnd();
            result.setSuccessful(sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS && sendResult.getEventId().length() > 0);
            result.setResponseMessage(sendResult.getEventId());

        } catch (Exception e) {
            getNewLogger().error("publish exception", e);
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


