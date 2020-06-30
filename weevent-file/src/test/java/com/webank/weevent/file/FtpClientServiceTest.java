package com.webank.weevent.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.file.ftpclient.FtpClientService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Slf4j
public class FtpClientServiceTest {
    private String ftpHost = "127.0.0.1";
    private int ftpPort = 21;
    private String ftpUser = "ftpuser";
    private String ftpPassWord = "";


    @Test
    @Ignore
    public void testFtpService() {
        FtpClientService ftpClientService = new FtpClientService();

        try {
            // connect
            ftpClientService.connect(this.ftpHost, this.ftpPort, this.ftpUser, this.ftpPassWord);

            // list dir
            List<String> dir = ftpClientService.getFileList("./");
            for (String fileName : dir) {
                System.out.println(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertTrue(true);
    }



    @Test
    @Ignore
    public void testFtpUpLoad() throws BrokerException {
        FtpClientService ftpClientService = new FtpClientService();
        ftpClientService.connect(this.ftpHost, this.ftpPort, this.ftpUser, this.ftpPassWord);

        File uploadFile = new File("D:\\bigFileTest\\test");
        if (!uploadFile.exists()) {
            System.out.println("read file failed!");
        }
        ftpClientService.upLoadFile(uploadFile);
        Assert.assertTrue(true);
    }

    @Test
    @Ignore
    public void testFtpUpLoad2SpecifyDir() throws BrokerException {
        FtpClientService ftpClientService = new FtpClientService();
        ftpClientService.connect(this.ftpHost, this.ftpPort, this.ftpUser, this.ftpPassWord);

        File uploadFile = new File("D:\\bigFileTest\\test");
        Assert.assertTrue(uploadFile.exists());

        ftpClientService.upLoadFile("./test", uploadFile);
        Assert.assertTrue(true);
    }

    @Test
    @Ignore
    public void testFtpDownLoadFile() throws BrokerException {
        FtpClientService ftpClientService = new FtpClientService();
        ftpClientService.connect(this.ftpHost, this.ftpPort, this.ftpUser, this.ftpPassWord);

        ftpClientService.downLoadFile("./fisco/nodes/cert/agency/cert.cnf", "D:\\bigFileTest");

        Assert.assertTrue(true);
    }

    @Test
    @Ignore
    public void testFtpDownLoadDirectory() throws BrokerException {
        FtpClientService ftpClientService = new FtpClientService();
        ftpClientService.connect(this.ftpHost, this.ftpPort, this.ftpUser, this.ftpPassWord);

        ftpClientService.downLoadDirectory("./fisco/nodes/cert/", "D:\\bigFileTest");

        Assert.assertTrue(true);
    }

    @Test
    public void testGetFileList() throws BrokerException {
        FtpClientService ftpClientService = mock(FtpClientService.class);
        List<String> fileList = new ArrayList<>();
        fileList.add("test1.txt");
        fileList.add("test2.txt");
        when(ftpClientService.getFileList("./")).thenReturn(fileList);
        List<String> ret = ftpClientService.getFileList("./");
        Assert.assertEquals(ret, fileList);
    }

}
