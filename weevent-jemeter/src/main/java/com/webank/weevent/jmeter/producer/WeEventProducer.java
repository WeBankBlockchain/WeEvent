package com.webank.weevent.jmeter.producer;


import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.IWeEventClient;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * WeEvent publish performance.
 *
 * @author puremilkfan
 * @since 2018/11/27
 */
public class WeEventProducer extends AbstractJavaSamplerClient {
    private String topic;
    private String buffer;
    private IWeEventClient weEventClient;

    // 每个压测线程启动时跑一次
    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);

        try {
            this.topic = context.getParameter("topic");
            int size = context.getIntParameter("size_byte");
            getNewLogger().info("params topic: {}, size: {}kb", this.topic, size);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                // 1kb
                sb.append("a");
            }
            this.buffer = sb.toString();

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
        arguments.addArgument("topic", "com.webank.weevent.jmeter");
        arguments.addArgument("size_byte","1");
        return arguments;
    }

    // Jmeter跑一次runTest算一个事物，会重复跑
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.setSampleLabel("publish");
        try {
            result.sampleStart();
            SendResult sendResult = this.weEventClient.publish(new WeEvent(this.topic, this.buffer.getBytes()));
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
}
