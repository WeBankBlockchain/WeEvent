package com.webank.weevent.broker.fisco.file;

import java.io.File;

import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.sdk.FileChunksMeta;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DiskFiles Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>02/18/2020</pre>
 */
public class DiskFilesTest {
    private DiskFiles diskFiles;
    private FileChunksMeta fileChunksMeta;

    @Before
    public void before() throws Exception {
        this.diskFiles = new DiskFiles("./logs/file");

        this.fileChunksMeta = new FileChunksMeta(WeEventUtils.generateUuid(),
                "abc.txt",
                100,
                "fce6f5f5d390fc1928c48eeb4e9271e9",
                "com.weevent.file",
                "1");
        this.fileChunksMeta.setChunkSize(32);
    }

    @After
    public void after() throws Exception {
        File file = new File("./logs/file");
        for (File f : file.listFiles()) {
            f.delete();
        }
        file.delete();
    }

    /**
     * Method: loadFileMeta(String fileId)
     */
    @Test
    public void testLoadFileMeta() throws Exception {
        this.diskFiles.saveFileMeta(this.fileChunksMeta);
        Assert.assertTrue(true);

        FileChunksMeta fileChunksMeta = this.diskFiles.loadFileMeta(this.fileChunksMeta.getFileId());
        Assert.assertEquals(this.fileChunksMeta.getFileId(), fileChunksMeta.getFileId());
        Assert.assertEquals(this.fileChunksMeta.getChunkNum(), fileChunksMeta.getChunkNum());
        Assert.assertTrue(true);
    }

    /**
     * Method: saveFileMeta(FileChunksMeta fileChunksMeta)
     */
    @Test
    public void testSaveFileMeta() throws Exception {
        this.diskFiles.saveFileMeta(this.fileChunksMeta);
        Assert.assertTrue(true);
    }

    /**
     * Method: createFixedLengthFile(String fileId, long len)
     */
    @Test
    public void testCreateFixedLengthFile() throws Exception {
        this.diskFiles.createFixedLengthFile(WeEventUtils.generateUuid(), this.fileChunksMeta.getFileSize());
        Assert.assertTrue(true);
    }

    /**
     * Method: writeChunkData(String fileId, int chunkIndex, byte[] chunkData)
     */
    @Test
    public void testWriteChunkData() throws Exception {
        this.diskFiles.createFixedLengthFile(this.fileChunksMeta.getFileId(), this.fileChunksMeta.getFileSize());
        this.diskFiles.saveFileMeta(this.fileChunksMeta);
        FileChunksMeta fileChunksMeta = this.diskFiles.writeChunkData(this.fileChunksMeta.getFileId(), 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes());

        Assert.assertTrue(fileChunksMeta.getChunkStatus().get(0));
        Assert.assertFalse(fileChunksMeta.getChunkStatus().get(1));
        Assert.assertFalse(fileChunksMeta.checkChunkFull());
    }

    /**
     * Method: readChunkData(String fileId, int chunkIndex)
     */
    @Test
    public void testReadChunkData() throws Exception {
        byte[] chunkData = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes();
        this.diskFiles.createFixedLengthFile(this.fileChunksMeta.getFileId(), this.fileChunksMeta.getFileSize());
        this.diskFiles.saveFileMeta(this.fileChunksMeta);
        this.diskFiles.writeChunkData(this.fileChunksMeta.getFileId(), 1, chunkData);
        byte[] data = this.diskFiles.readChunkData(this.fileChunksMeta.getFileId(), 1);

        Assert.assertArrayEquals(data, chunkData);
    }
} 
