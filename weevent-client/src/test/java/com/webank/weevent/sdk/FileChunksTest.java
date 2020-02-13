package com.webank.weevent.sdk;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * FileChunks Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>02/12/2020</pre>
 */
public class FileChunksTest {
    private FileChunks fileChunks;

    @Before
    public void before() throws Exception {
        this.fileChunks = new FileChunks("http://localhost:7000/weevent-broker/file", "./logs");
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: upload(String localFile)
     */
    @Test
    public void testUpload() throws Exception {
        String fileId = this.fileChunks.upload("src/main/resources/log4j2.xml");
        Assert.assertFalse(fileId.isEmpty());
    }

    /**
     * Method: download(String host, String fileId)
     */
    @Test
    public void testDownload() throws Exception {
        String fileId = this.fileChunks.upload("src/main/resources/log4j2.xml");
        Assert.assertFalse(fileId.isEmpty());

        String localFile = this.fileChunks.download("http://localhost:7000", "fileId");
        Assert.assertFalse(localFile.isEmpty());
    }
} 
