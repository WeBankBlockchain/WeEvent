package com.webank.weevent.file.ftpclient;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FtpInfo {
    private String host;
    private int port;
    private String userName;
    private String passWord;
    private String ftpReceivePath;

    public FtpInfo(String host, int port, String userName, String passWord, String ftpReceivePath) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.passWord = passWord;
        this.ftpReceivePath = ftpReceivePath;
    }
}
