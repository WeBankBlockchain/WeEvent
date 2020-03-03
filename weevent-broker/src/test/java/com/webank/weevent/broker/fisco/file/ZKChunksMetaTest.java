package com.webank.weevent.broker.fisco.file;

import com.webank.weevent.broker.JUnitTestBase;
import com.webank.weevent.core.fisco.util.WeEventUtils;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.FileChunksMeta;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    public void setZkChunksMeta(ZKChunksMeta zkChunksMeta) {
        this.zkChunksMeta = zkChunksMeta;
    }

    @Before
    public void before() throws Exception {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());

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
