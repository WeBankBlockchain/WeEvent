package com.webank.weevent.jmeter;


import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;


@Slf4j
public class JMeterApplication  extends AbstractJavaSamplerClient {

    public static void main(String[] args) {
        log.info("Start jmeter success0");
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult results = new SampleResult();
        results.setResponseCode("22");
        results.setResponseMessage("33");
        results.setSampleLabel("44");
        return results;
    }
}
