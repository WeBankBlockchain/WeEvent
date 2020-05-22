package com.webank.weevent.file;

import com.webank.weevent.file.ftpclient.FtpClientService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

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
            boolean connect = ftpClientService.connect(this.ftpHost, this.ftpPort, this.ftpUser, this.ftpPassWord);
            if (!connect) {
                System.out.println("connect failed!");
            }

            // list dir
            String[] dir = ftpClientService.getFileList("./");
            for (String fileName : dir) {
                System.out.println(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
