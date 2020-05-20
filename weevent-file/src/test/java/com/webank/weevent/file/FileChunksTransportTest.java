package com.webank.weevent.file;

import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.file.service.FileChunksTransport;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * FileChunks Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>02/12/2020</pre>
 */
public class FileChunksTransportTest {
    private FileChunksTransport fileChunksTransport;

    /**
     * Method: upload(String localFile)
     */
    @Test
    @Ignore
    public void testUpload() throws Exception {
        SendResult sendResult = this.fileChunksTransport.upload("src/main/resources/log4j2.xml", "com.weevent.file", WeEvent.DEFAULT_GROUP_ID);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);
    }

    /**
     * Method: download(String host, String fileId)
     */
    @Test
    @Ignore
    public void testDownload() throws Exception {
        SendResult sendResult = this.fileChunksTransport.upload("src/main/resources/log4j2.xml", "com.weevent.file", WeEvent.DEFAULT_GROUP_ID);
        Assert.assertEquals(sendResult.getStatus(), SendResult.SendResultStatus.SUCCESS);

        // String localFile = this.fileChunksTransport.download("http://localhost:7000", "fileId");
        // Assert.assertFalse(localFile.isEmpty());
    }
} 
