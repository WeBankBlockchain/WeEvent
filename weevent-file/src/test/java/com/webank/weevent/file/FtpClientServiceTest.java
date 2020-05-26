package com.webank.weevent.file;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.file.ftpclient.FtpClientService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Slf4j
public class FtpClientServiceTest {
    private String ftpHost = "127.0.0.1";
    private int ftpPort = 21;
    private String ftpUser = "ftpuser";
    private String ftpPassWord = "abcd1234";


    @Test
    @Ignore
    public void testFtpService() {
        FtpClientService ftpClientService = new FtpClientService();

        try {
            // connect
            ftpClientService.connect(this.ftpHost, this.ftpPort, this.ftpUser, this.ftpPassWord);

            // list dir
            String[] dir = ftpClientService.getFileList("./");
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
    public void testFtpUpLoad() throws  BrokerException {
        FtpClientService ftpClientService = new FtpClientService();
        ftpClientService.connect(this.ftpHost, this.ftpPort, this.ftpUser, this.ftpPassWord);

        File uploadFile = new File("D:\\bigFileTest\\test");
        if (!uploadFile.exists()) {
            System.out.println("read file failed!");
        }
        boolean upload = ftpClientService.upLoadFile(uploadFile);
        Assert.assertTrue(upload);
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

}
