package com.webank.weevent.file;

import com.webank.weevent.client.IWeEventClient;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.file.inner.FileTransportService;
import com.webank.weevent.file.service.WeEventFileClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WeEventFileClientTest {

    private Map<String, String> extensions = new HashMap<>();
    private final long FIVE_SECOND= 5000L;

    @Rule
    public TestName testName = new TestName();

    private String topicName = "com.weevent.test";
    private IWeEventClient weEventClient;


    private FileTransportService fileTransportService;
    private FiscoConfig fiscoConfig;


    @Test
    @Ignore
    public void testPublishFile() throws Exception {
        FiscoConfig fiscoConfig = new FiscoConfig();
        Assert.assertTrue(fiscoConfig.load(""));
        WeEventFileClient weEventFileClient = new WeEventFileClient("1", fiscoConfig);

        SendResult sendResult = weEventFileClient.publishFile("com.weevent.file",
                new File("src/main/resources/apache-jmeter-5.1.1.zip").getAbsolutePath());

        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    @Test
    @Ignore
    public void testSubscribeFile() throws Exception {
        FiscoConfig fiscoConfig = new FiscoConfig();
        fiscoConfig.load("");
        IWeEventFileClient.FileListener fileListener = new IWeEventFileClient.FileListener() {
            @Override
            public void onFile(String topicName, String fileName) {
                log.info("+++++++topic name: {}, file name: {}", topicName, fileName);
                System.out.println(new File("./logs1"+fileName).getAbsolutePath());
            }

            @Override
            public void onException(Throwable e) {

            }
        };

        WeEventFileClient weEventFileClient = new WeEventFileClient("1", "./logs1", fiscoConfig);
        weEventFileClient.subscribeFile("com.weevent.file", "./logs1", fileListener);

        Thread.sleep(1000*60*10);
    }
}
