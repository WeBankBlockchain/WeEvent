
package com.webank.weevent.jmeter.producer;


/**
 * WeEvent publish performance.
 *
 * @since 2018/11/27
 */

import java.util.HashMap;
import java.util.Map;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;

public class WeEventProducer extends AbstractJavaSamplerClient {
    private String topic;
    private String buffer;
    private IWeEventClient weEventClient;
    private Map<String, String> extensions = new HashMap<>();
    ;

    private WeEvent weEvent;

    private final String http = "http://";

    private static String defaultUrl = "127.0.0.1:7000";

    // 每个压测线程启动时跑一次
    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        try {
            if (context.getParameter("url") != null) {
                this.defaultUrl = context.getParameter("url");
            }
            System.out.println("this is setupTest");
            this.weEventClient = IWeEventClient.build(http + defaultUrl + "/weevent");
            getNewLogger().info("weEventClient:{}", this.weEventClient);
            this.topic = context.getParameter("topic");
            int size = context.getIntParameter("size_byte");
            String format = context.getParameter("format");
            extensions.put(WeEvent.WeEvent_FORMAT, format);
            getNewLogger().info("params topic: {}, size: {}kb", this.topic, size);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                // 1kb
                sb.append("a");
            }
            this.buffer = sb.toString();
            weEvent = new WeEvent(this.topic, buffer.getBytes(), this.extensions);

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

    // Jmeter GUI参数
    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("topic", "com.webank.weevent.jmeter");
        arguments.addArgument("size_byte", "1");
        arguments.addArgument("format", "json");
        return arguments;
    }

    // Jmeter跑一次runTest算一个事物，会重复跑
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel("runTest");
        try {
            System.out.println("this is setupTest");

            result.sampleStart();
            if (context.getParameter("url") != null) {
                this.defaultUrl = context.getParameter("url");
            }
            this.weEventClient = IWeEventClient.build(http + defaultUrl + "/weevent");
            getNewLogger().info("weEventClient:{}", this.weEventClient);
            this.topic = context.getParameter("topic");
            int size = context.getIntParameter("size_byte");
            String format = context.getParameter("format");
            extensions.put(WeEvent.WeEvent_FORMAT, format);
            getNewLogger().info("params topic: {}, size: {}kb", this.topic, size);
            weEvent = new WeEvent(this.topic, buffer.getBytes(), this.extensions);
            getNewLogger().info("weEvent:{}", weEvent);
            SendResult sendResult = this.weEventClient.publish(weEvent);
            result.sampleEnd();
            result.setSuccessful(sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS && sendResult.getEventId().length() > 0);
            result.setResponseMessage(sendResult.getEventId());

        } catch (Exception e) {
            getNewLogger().error("publish Exception", e);
            result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseMessage(e.getMessage());
        }
        return result;
    }

    public WeEventProducer() {
        super();
    }


    @Override
    protected Logger getNewLogger() {
        return super.getNewLogger();
    }

}

