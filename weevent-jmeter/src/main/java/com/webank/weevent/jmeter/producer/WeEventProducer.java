
package com.webank.weevent.jmeter.producer;


/**
 * WeEvent publish performance.
 * puremilkfan
 *
 * @since 2019/09/11
 */

import java.nio.charset.Charset;
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

/**
 * WeEvent consumer performance.
 * puremilkfan
 *
 * @since 2019/09/10
 */


public class WeEventProducer extends AbstractJavaSamplerClient {
    private String topic = "com.webank.weevent.jmeter";
    private int size = 1;
    private String format = "json";

    private Map<String, String> extensions = new HashMap<>();

    private String groupId = WeEvent.DEFAULT_GROUP_ID;

    private IWeEventClient weEventClient;

    private WeEvent weEvent;


    private String defaultUrl = "http://127.0.0.1:8080/weevent";

    // Run every time the pressure thread starts
    @Override
    public void setupTest(JavaSamplerContext context) {
        getNewLogger().info("this is producer setupTest");
        super.setupTest(context);
        try {
            this.defaultUrl = context.getParameter("url") == null ? this.defaultUrl : context.getParameter("url");
            this.size = context.getIntParameter("size") <= 0 ? this.size : context.getIntParameter("size");
            this.topic = context.getParameter("topic") == null ? this.topic : context.getParameter("topic");
            this.format = context.getParameter("format") == null ? this.format : context.getParameter("format");
            this.groupId = context.getParameter("groupId") == null ? WeEvent.DEFAULT_GROUP_ID : context.getParameter("groupId");
            extensions.put(WeEvent.WeEvent_FORMAT, format);
            this.weEventClient = IWeEventClient.build(defaultUrl, this.groupId);
            getNewLogger().info("weEventClient:{}", this.weEventClient);

            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < size; i++) {
                buffer.append("a");
            }

            weEvent = new WeEvent(this.topic, buffer.toString().getBytes(), this.extensions);
            getNewLogger().info("weEvent:{}", weEvent);

            boolean result = this.weEventClient.open(this.topic);
            getNewLogger().info("open topic result: {}", result);
        } catch (BrokerException e) {
            getNewLogger().error("open ClientException", e);
        }
    }

    // Execute every runTest run
    @Override
    public void teardownTest(JavaSamplerContext context) {
        getNewLogger().debug(getClass().getName() + ": teardownTest");

    }

    // JMeter GUI parameters
    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("topic", this.topic);
        arguments.addArgument("size", String.valueOf(this.size));
        arguments.addArgument("groupId", this.groupId);
        arguments.addArgument("format", this.format);
        arguments.addArgument("url", this.defaultUrl);
        return arguments;
    }

    // Jmeter runs once runTest to calculate a thing, it will run repeatedly
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel("producer");
        try {
            result.sampleStart();
            SendResult sendResult = this.weEventClient.publish(this.weEvent);
            result.sampleEnd();
            result.setSuccessful(sendResult.getStatus() == SendResult.SendResultStatus.SUCCESS && sendResult.getEventId().length() > 0);
            result.setResponseMessage(sendResult.getEventId());
            result.setResponseHeaders(sendResult.getStatus().toString());
            result.setResponseData(sendResult.toString(), Charset.defaultCharset().name());
            getNewLogger().info("sendResult:{}", sendResult);
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
