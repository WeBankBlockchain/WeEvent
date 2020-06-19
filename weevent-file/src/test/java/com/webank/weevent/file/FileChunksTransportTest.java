package com.webank.weevent.file;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.FiscoBcosInstance;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.core.fisco.util.WeEventUtils;
import com.webank.weevent.file.inner.FileTransportService;
import com.webank.weevent.file.service.FileChunksMeta;
import com.webank.weevent.file.service.FileChunksTransport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * FileChunks Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>02/12/2020</pre>
 */
public class FileChunksTransportTest {
    private FiscoConfig fiscoConfig;
    private FileTransportService fileTransportService;

    private String topicName = "com.weevent.file";
    private String groupId = "1";
    private String localReceivePath = "./logs";
    // chunk size 1MB
    private int fileChunkSize = 1048576;

    @Before
    public void before() throws BrokerException {
        this.fiscoConfig = new FiscoConfig();
        this.fiscoConfig.load("");
        FiscoBcosInstance fiscoBcosInstance = new FiscoBcosInstance(this.fiscoConfig);

        // create producer
        IProducer iProducer = fiscoBcosInstance.buildProducer();
        iProducer.startProducer();

        // create consumer
        IConsumer iConsumer = fiscoBcosInstance.buildConsumer();
        iConsumer.startConsumer();

        // create FileTransportService instance
        FileTransportService fileTransportService = new FileTransportService(this.fiscoConfig, iProducer, "", this.localReceivePath, this.fileChunkSize, this.groupId);
        this.fileTransportService = fileTransportService;
    }

    /**
     * Method: upload(String localFile)
     */
    @Test
    @Ignore
    public void testUpload() throws Exception {
        FileChunksTransport fileChunksTransport = new FileChunksTransport(this.fileTransportService);
        FileChunksMeta fileChunksMeta = fileChunksTransport.upload("src/main/resources/log4j2.xml", "com.weevent.file", WeEvent.DEFAULT_GROUP_ID, true);
        Assert.assertNotNull(fileChunksMeta);
    }

    /**
     * Method: download(String host, String fileId)
     */
    @Test
    public void testDownload() {
        try {
            FileTransportService fileTransportService1 = mock(FileTransportService.class);

            FileChunksMeta fileChunksMeta = null;
            try {
                fileChunksMeta = new FileChunksMeta(WeEventUtils.generateUuid(),
                        URLDecoder.decode("log4j2.xml", StandardCharsets.UTF_8.toString()),
                        1114,
                        "d41d8cd98f00b204e9800998ecf8427e",
                        this.topicName,
                        this.groupId, true);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            fileChunksMeta.initChunkSize(1024);

            when(fileTransportService1.openChannel(Mockito.any())).thenReturn(fileChunksMeta);
            when(fileTransportService1.getReceiverFileChunksMeta(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(fileChunksMeta);

            FileChunksTransport fileChunksTransport = new FileChunksTransport(fileTransportService1);

            FileChunksMeta fileChunksMeta1 = fileChunksTransport.upload("src/main/resources/log4j2.xml", "com.weevent.file", WeEvent.DEFAULT_GROUP_ID, true);
            Assert.assertNotNull(fileChunksMeta1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
} 
