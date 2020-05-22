package com.webank.weevent.file.ftpclient;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

public class FtpClientService {

    public FTPClient ftpClient = new FTPClient();

    // connect to ftp service
    public boolean connect(String hostname, int port, String username, String password) throws Exception {
        try {
            ftpClient.connect(hostname, port);
        } catch (Exception e) {
            throw new Exception("login exception, please checkout ip and port!");
        }

        ftpClient.setControlEncoding("GBK");
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            if (ftpClient.login(username, password)) {
                return true;
            }
            else {
                throw new Exception("login to ftp failed, please checkout username and password!");
            }
        }
        else {
            throw new Exception("login to ftp failed!");
        }
    }

    // disconnect ftp service
    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }

    public String[] getFileList(String filedir) throws IOException {
        ftpClient.enterLocalPassiveMode();

        FTPFile[] files = ftpClient.listFiles(filedir);

        String[] sfiles = null;
        if (files != null) {
            sfiles = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                // System.out.println(files[i].getName());
                sfiles[i] = files[i].getName();
            }
        }
        return sfiles;
    }

}
