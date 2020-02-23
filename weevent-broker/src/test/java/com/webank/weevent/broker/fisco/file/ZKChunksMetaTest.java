package com.webank.weevent.broker.fisco.file;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.JUnitTestBase;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.FileChunksMeta;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * ZKChunksMeta Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>02/18/2020</pre>
 */
@Slf4j
public class ZKChunksMetaTest extends JUnitTestBase {
    private ZKChunksMeta zkChunksMeta;
    private FileChunksMeta fileChunksMeta;

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

        this.zkChunksMeta = BrokerApplication.applicationContext.getBean(ZKChunksMeta.class);

        this.fileChunksMeta = new FileChunksMeta(WeEventUtils.generateUuid(),
                "abc.txt",
                100,
                "fce6f5f5d390fc1928c48eeb4e9271e9",
                "com.weevent.file",
                "1");
        this.fileChunksMeta.setChunkSize(32);
        this.fileChunksMeta.getChunkStatus().set(0);
        this.fileChunksMeta.getChunkStatus().set(2);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getChunks(String fileId)
     */
    @Test(expected = BrokerException.class)
    public void testGetChunks() throws Exception {
        this.zkChunksMeta.getChunks("notexist");
    }

    /**
     * Method: addChunks(String fileId, FileChunksMeta fileChunksMeta)
     */
    @Test
    public void testAddChunks() throws Exception {
        this.zkChunksMeta.addChunks(this.fileChunksMeta.getFileId(), this.fileChunksMeta);
        Assert.assertTrue(true);
    }

    /**
     * Method: removeChunks(String fileId)
     */
    @Test
    public void testRemoveChunks() throws Exception {
        this.zkChunksMeta.addChunks(this.fileChunksMeta.getFileId(), this.fileChunksMeta);
        Assert.assertTrue(true);

        this.zkChunksMeta.removeChunks(this.fileChunksMeta.getFileId());
        Assert.assertTrue(true);
    }

    /**
     * Method: updateChunks(String fileId, FileChunksMeta fileChunksMeta)
     */
    @Test
    public void testUpdateChunks() throws Exception {
        this.zkChunksMeta.addChunks(this.fileChunksMeta.getFileId(), this.fileChunksMeta);
        Assert.assertTrue(true);

        this.fileChunksMeta.getChunkStatus().set(1);
        this.zkChunksMeta.updateChunks(this.fileChunksMeta.getFileId(), this.fileChunksMeta);
        Assert.assertTrue(true);

        FileChunksMeta fileChunksMeta = this.zkChunksMeta.getChunks(this.fileChunksMeta.getFileId());
        Assert.assertTrue(fileChunksMeta.getChunkStatus().get(0));
        Assert.assertTrue(fileChunksMeta.getChunkStatus().get(1));
        Assert.assertTrue(fileChunksMeta.getChunkStatus().get(2));
    }
}
