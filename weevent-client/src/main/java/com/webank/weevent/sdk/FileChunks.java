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
public class FileChunks {
    private String svrUrl;
    private String downloadFilePath = "";

    public FileChunks(String svrUrl) {
        this.svrUrl = svrUrl;
    }

    public FileChunks(String svrUrl, String downloadFilePath) {
        this.svrUrl = svrUrl;
        this.downloadFilePath = downloadFilePath;
    }

    public String upload(String localFile) throws BrokerException, IOException {
        log.info("try to upload file {}", localFile);

        // get file initial information
        FileInputStream fileInputStream = new FileInputStream(localFile);
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        File file = new File(localFile);
        fileChunksMeta.setFileName(file.getName());
        fileChunksMeta.setFileSize(file.length());
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
        String url = host + fileId;
        log.info("try to download file, {}", url);

        // get chunk information
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        fileChunksMeta.setFileId(fileId);
        fileChunksMeta = this.getFileChunksInfo(fileChunksMeta);

        // create file
        FileOutputStream fileOutputStream = new FileOutputStream(fileChunksMeta.getFileName());

        // download every single chunk data
        for (int chunkIdx = 0; chunkIdx < fileChunksMeta.getChunkNum(); chunkIdx++) {
            byte[] chunkData = this.downloadChunk(fileChunksMeta.getFileId(), chunkIdx);
            if (chunkData != null) {
                fileOutputStream.write(chunkData, chunkIdx * fileChunksMeta.getChunkSize(), chunkData.length);
            }
        }

        // check md5sum
        String md5 = DigestUtils.md5DigestAsHex(new FileInputStream(fileChunksMeta.getFileName()));
        if (!fileChunksMeta.getFileMd5().equals(md5)) {
            log.error("md5 mismatch, {} <=> {}", fileChunksMeta.getFileMd5(), md5);
            throw new BrokerException("");
        }

        log.info("download file success, {}", fileChunksMeta.getFileName());
        return fileChunksMeta.getFileName();
    }

    private FileChunksMeta getFileChunksInfo(FileChunksMeta fileChunksMeta) {
        if (StringUtils.isEmpty(fileChunksMeta.getFileId())) { // first create
            // this.svrUrl + "/createChunk"
        } else { // continue upload
            // this.svrUrl + "/listChunk"
        }

        return fileChunksMeta;
    }

    private boolean uploadChunk(String fileId, int chunkIdx, byte[] chunkData) {
        // this.svrUrl + "/uploadChunk"

        log.info("upload chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        return false;
    }

    private byte[] downloadChunk(String fileId, int chunkIdx) {
        // this.svrUrl + "/downloadChunk"
        byte[] chunkData = null;
        log.info("download chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        return chunkData;
    }
}
