package com.webank.weevent.sdk;


import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Upload&Download File in chunks.
 *
 * @author matthewliu
 * @since 2020/02/12
 */
public class FileChunks {
    private String downloadFilePath = "";

    public FileChunks() {
    }

    public FileChunks(String downloadFilePath) {
        this.downloadFilePath = downloadFilePath;
    }

    public String upload(String localFile) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(localFile);

        return "";
    }

    public String download(String url) {
        return "";
    }
}
