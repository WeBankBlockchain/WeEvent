package com.webank.weevent.client;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * FileChunksMeta Tester.
 *
 * @author <matthewliu>
 * @version 1.0
 * @since <pre>03/03/2020</pre>
 */
@Slf4j
public class FileChunksMetaTest {
    @Rule
    public TestName testName = new TestName();

    @Before
    public void before() {
        log.info("=============================={}.{}==============================",
                this.getClass().getSimpleName(),
                this.testName.getMethodName());
    }

    /**
     * Method: setChunkSize(int chunkSize)
     */
    @Test
    public void testSetChunkSize001() {
        FileChunksMeta fileChunksMeta = new FileChunksMeta("abc",
                "abc.txt",
                100,
                "5291aeb50bddf9ef9ad8824ee7df34ba",
                "com.weevent.test",
                "1");
        fileChunksMeta.setChunkSize(1024);
        Assert.assertEquals(1, fileChunksMeta.getChunkNum());
    }

    /**
     * Method: setChunkSize(int chunkSize)
     */
    @Test
    public void testSetChunkSize002() {
        FileChunksMeta fileChunksMeta = new FileChunksMeta("abc",
                "abc.txt",
                1024,
                "5291aeb50bddf9ef9ad8824ee7df34ba",
                "com.weevent.test",
                "1");
        fileChunksMeta.setChunkSize(1024);
        Assert.assertEquals(1, fileChunksMeta.getChunkNum());
    }

    /**
     * Method: setChunkSize(int chunkSize)
     */
    @Test
    public void testSetChunkSize003() {
        FileChunksMeta fileChunksMeta = new FileChunksMeta("abc",
                "abc.txt",
                1025,
                "5291aeb50bddf9ef9ad8824ee7df34ba",
                "com.weevent.test",
                "1");
        fileChunksMeta.setChunkSize(1024);
        Assert.assertEquals(2, fileChunksMeta.getChunkNum());
    }

    /**
     * Method: setChunkSize(int chunkSize)
     */
    @Test
    public void testSetChunkSize004() {
        FileChunksMeta fileChunksMeta = new FileChunksMeta("abc",
                "abc.txt",
                2048,
                "5291aeb50bddf9ef9ad8824ee7df34ba",
                "com.weevent.test",
                "1");
        fileChunksMeta.setChunkSize(1024);
        Assert.assertEquals(2, fileChunksMeta.getChunkNum());
    }

    /**
     * Method: setChunkSize(int chunkSize)
     */
    @Test
    public void testSetChunkSize005() {
        FileChunksMeta fileChunksMeta = new FileChunksMeta("abc",
                "abc.txt",
                1023,
                "5291aeb50bddf9ef9ad8824ee7df34ba",
                "com.weevent.test",
                "1");
        fileChunksMeta.setChunkSize(1048576);
        Assert.assertEquals(1, fileChunksMeta.getChunkNum());
    }

    /**
     * Method: setChunkSize(int chunkSize)
     */
    @Test
    public void testSetChunkSize006() {
        FileChunksMeta fileChunksMeta = new FileChunksMeta("abc",
                "abc.txt",
                1048576,
                "5291aeb50bddf9ef9ad8824ee7df34ba",
                "com.weevent.test",
                "1");
        fileChunksMeta.setChunkSize(1048576);
        Assert.assertEquals(1, fileChunksMeta.getChunkNum());
    }

    /**
     * Method: setChunkSize(int chunkSize)
     */
    @Test
    public void testSetChunkSize100() {
        FileChunksMeta fileChunksMeta = new FileChunksMeta("abc",
                "abc.txt",
                8589934592L,
                "5291aeb50bddf9ef9ad8824ee7df34ba",
                "com.weevent.test",
                "1");
        fileChunksMeta.setChunkSize(1048576);
        Assert.assertEquals(8192, fileChunksMeta.getChunkNum());
    }
} 
