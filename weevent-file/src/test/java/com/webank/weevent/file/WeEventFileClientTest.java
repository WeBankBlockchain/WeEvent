package com.webank.weevent.file;

import com.webank.weevent.client.SendResult;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.file.service.WeEventFileClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;

@Slf4j
public class WeEventFileClientTest {

    private String topicName = "com.weevent.test";
    private String groupId = "1";
    private String receiverFilePath = "./logs";
    // chunk size 1MB
    private int fileChunkSize = 1048576;
    private FiscoConfig fiscoConfig;


    @Before
    public void before() {
        this.fiscoConfig = new FiscoConfig();
        this.fiscoConfig.load("");
    }


    @Test
    @Ignore
    public void testPublishFile() throws Exception {
        WeEventFileClient weEventFileClient = new WeEventFileClient(this.groupId, this.receiverFilePath, this.fileChunkSize, this.fiscoConfig);

        weEventFileClient.openTransport4Sender(topicName);
        SendResult sendResult = weEventFileClient.publishFile(this.topicName,
                new File("src/main/resources/ca.crt").getAbsolutePath());

        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    @Test
    @Ignore
    public void testSubscribeFile() throws Exception {
        IWeEventFileClient.FileListener fileListener = new IWeEventFileClient.FileListener() {
            @Override
            public void onFile(String topicName, String fileName) {
                log.info("+++++++topic name: {}, file name: {}", topicName, fileName);
                System.out.println(new File(receiverFilePath +fileName).getAbsolutePath());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        };

        WeEventFileClient weEventFileClient = new WeEventFileClient(this.groupId, this.receiverFilePath, this.fileChunkSize, this.fiscoConfig);
        weEventFileClient.openTransport4Receiver(this.topicName, fileListener);

        Thread.sleep(1000*60*5);
        Assert.assertTrue(true);
    }

    @Test
    @Ignore
    public void testPublishFileWithVerify() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:" + "0x2809a9902e47d6fcaabe6d0183855d9201c93af1.public.pem");

        WeEventFileClient weEventFileClient = new WeEventFileClient(this.groupId, this.receiverFilePath, this.fileChunkSize, this.fiscoConfig);

        weEventFileClient.openTransport4Sender(this.topicName, resource.getInputStream());

        // handshake time delay for web3sdk
        Thread.sleep(1000*10);

        SendResult sendResult = weEventFileClient.publishFile("com.weevent.file",
                new File("src/main/resources/ca.crt").getAbsolutePath());

        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    @Test
    @Ignore
    public void testSubscribeFileWithVerify() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:" + "0x2809a9902e47d6fcaabe6d0183855d9201c93af1.pem");

        IWeEventFileClient.FileListener fileListener = new IWeEventFileClient.FileListener() {
            @Override
            public void onFile(String topicName, String fileName) {
                log.info("+++++++topic name: {}, file name: {}", topicName, fileName);
                System.out.println(new File(receiverFilePath +fileName).getAbsolutePath());
            }

            @Override
            public void onException(Throwable e) {
                e.printStackTrace();
            }
        };

        WeEventFileClient weEventFileClient = new WeEventFileClient(this.groupId, this.receiverFilePath, this.fileChunkSize, this.fiscoConfig);
        weEventFileClient.openTransport4Receiver("com.weevent.file", fileListener, resource.getInputStream());


        Thread.sleep(1000*60*5);
        Assert.assertTrue(true);
    }

    @Test
    @Ignore
    public void testCloseTransport() {
        WeEventFileClient weEventFileClient = new WeEventFileClient(this.groupId, this.receiverFilePath,this.fileChunkSize, this.fiscoConfig);
        weEventFileClient.closeTransport(this.topicName);
        Assert.assertTrue(true);
    }

    @Test
    @Ignore
    public void testListFile() {
        WeEventFileClient weEventFileClient = new WeEventFileClient(this.groupId, this.receiverFilePath, this.fileChunkSize, this.fiscoConfig);
        weEventFileClient.listFiles(this.topicName);
        Assert.assertTrue(true);
    }

    @Test
    @Ignore
    public void testStatus() {
        WeEventFileClient weEventFileClient = new WeEventFileClient(this.groupId, this.receiverFilePath, this.fileChunkSize, this.fiscoConfig);
        weEventFileClient.status(this.topicName);
        Assert.assertTrue(true);
    }
}
