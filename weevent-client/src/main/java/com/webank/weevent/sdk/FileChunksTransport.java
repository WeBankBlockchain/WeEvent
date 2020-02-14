package com.webank.weevent.sdk;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

/**
 * Upload&Download File in chunks.
 *
 * @author matthewliu
 * @since 2020/02/12
 */
@Slf4j
public class FileChunksTransport {
    //like http://localhost:8080/weevent-broker/file
    private String svrUrl;
    private String downloadFilePath = "";

    public FileChunksTransport(String svrUrl) {
        this.svrUrl = svrUrl;
    }

    public FileChunksTransport(String svrUrl, String downloadFilePath) {
        this.svrUrl = svrUrl;
        this.downloadFilePath = downloadFilePath;
    }

    public String upload(String localFile) throws BrokerException, IOException {
        log.info("try to upload file {}", localFile);

        // get file initial information
        File file = new File(localFile);
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        fileChunksMeta.setFileName(file.getName());
        fileChunksMeta.setFileSize(file.length());
        FileInputStream fileInputStream = new FileInputStream(file);
        fileChunksMeta.setFileMd5(DigestUtils.md5DigestAsHex(fileInputStream));

        // get chunk information
        fileChunksMeta = this.getFileChunksInfo(fileChunksMeta);

        // upload every single chunk data
        for (int chunkIdx = 0; chunkIdx < fileChunksMeta.getChunkNum(); chunkIdx++) {
            byte[] chunkData = new byte[fileChunksMeta.getChunkSize()];
            fileInputStream.read(chunkData, chunkIdx * fileChunksMeta.getChunkSize(), fileChunksMeta.getChunkSize());
            boolean finish = this.uploadChunk(fileChunksMeta.getFileId(), chunkIdx, chunkData);
            if (finish) {
                log.info("upload file complete, {}", localFile);
                return fileChunksMeta.getFileId();
            }
        }

        log.error("upload file not complete, {}", localFile);
        return "";
    }

    public String download(String host, String fileId) throws BrokerException, IOException {
        log.info("try to download file, {}@{}", fileId, host);

        // get chunk information
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        fileChunksMeta.setFileId(fileId);
        fileChunksMeta = this.getFileChunksInfo(fileChunksMeta);

        // create file
        String fileName = this.downloadFilePath + "/" + fileChunksMeta.getFileName();
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        // download every single chunk data
        for (int chunkIdx = 0; chunkIdx < fileChunksMeta.getChunkNum(); chunkIdx++) {
            byte[] chunkData = this.downloadChunk(fileChunksMeta.getFileId(), chunkIdx);
            if (chunkData != null) {
                fileOutputStream.write(chunkData, chunkIdx * fileChunksMeta.getChunkSize(), chunkData.length);
            }
        }

        // check md5sum
        String md5 = DigestUtils.md5DigestAsHex(new FileInputStream(fileName));
        if (!fileChunksMeta.getFileMd5().equals(md5)) {
            log.error("md5 mismatch, {} <=> {}", fileChunksMeta.getFileMd5(), md5);
            throw new BrokerException("");
        }

        log.info("download file success, {} -> {}", fileId, fileName);
        return fileName;
    }

    private FileChunksMeta getFileChunksInfo(FileChunksMeta fileChunksMeta) {
        if (StringUtils.isEmpty(fileChunksMeta.getFileId())) { // first create
            // this.svrUrl + "/createChunk"
        } else { // continue upload
            // this.svrUrl + "/listChunk"
        }
        fileChunksMeta.setFileId("a");
        fileChunksMeta.setChunkSize(256);
        fileChunksMeta.setChunkNum((int) fileChunksMeta.getFileSize() / fileChunksMeta.getChunkSize() + 1);
        return fileChunksMeta;
    }

    private boolean uploadChunk(String fileId, int chunkIdx, byte[] chunkData) {
        // this.svrUrl + "/uploadChunk"

        log.info("upload chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        return false;
    }

    private byte[] downloadChunk(String fileId, int chunkIdx) {
        // this.svrUrl + "/downloadChunk"
        byte[] chunkData = new byte[1];
        log.info("download chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        return chunkData;
    }
}
