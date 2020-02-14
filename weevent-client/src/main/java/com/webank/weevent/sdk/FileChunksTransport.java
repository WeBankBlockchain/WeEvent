package com.webank.weevent.sdk;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.util.DigestUtils;

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
    private CloseableHttpClient httpClient;
    private String groupId;

    public FileChunksTransport(String svrUrl, CloseableHttpClient httpClient) {
        this.svrUrl = svrUrl;
        this.httpClient = httpClient;
    }

    public FileChunksTransport(String svrUrl, String downloadFilePath, CloseableHttpClient httpClient) {
        this.svrUrl = svrUrl;
        this.downloadFilePath = downloadFilePath;
        this.httpClient = httpClient;
    }

    public String upload(String localFile, String groupId) throws BrokerException, IOException {
        log.info("try to upload file {}", localFile);

        // get file initial information
        File file = new File(localFile);
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        fileChunksMeta.setFileName(file.getName());
        fileChunksMeta.setFileSize(file.length());
        FileInputStream fileInputStream = new FileInputStream(file);
        fileChunksMeta.setFileMd5(DigestUtils.md5DigestAsHex(fileInputStream));

        // get chunk information
        fileChunksMeta = this.getFileChunksInfo(fileChunksMeta, groupId);

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
        fileChunksMeta = this.getFileChunksInfo(fileChunksMeta, "");

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

    private FileChunksMeta getFileChunksInfo(FileChunksMeta fileChunksMeta, String groupId) throws BrokerException {
        FileChunksMeta fileChunksMetaResponse;
        StringBuffer params = new StringBuffer();

        HttpGet httpGet;

        if (StringUtils.isEmpty(fileChunksMeta.getFileId())) { // first create
            params.append("groupId=" + groupId);
            params.append("fileName=" + fileChunksMeta.getFileName());
            params.append("fileSize=" + fileChunksMeta.getFileSize());
            params.append("md5=" + fileChunksMeta.getFileMd5());
            httpGet = new HttpGet(this.svrUrl + "/createChunk" + "?" + params);
        } else { // continue upload
            params.append("fileId=" + fileChunksMeta.getFileId());
            params.append("chunkIdx=" + fileChunksMeta.getChunkNum());
            // params.append("chunkData="+chunkData);
            httpGet = new HttpGet(this.svrUrl + "/uploadChunk" + "?" + params);
        }

        CloseableHttpResponse closeResponse;
        try {
            closeResponse = httpClient.execute(httpGet);
            String responseResult = EntityUtils.toString(closeResponse.getEntity());
            fileChunksMetaResponse = DataUtils.parseObject(responseResult, FileChunksMeta.class);
        } catch (IOException e) {
            log.error("execute http request :{} error, e:{}", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }

//        fileChunksMeta.setFileId("a");
//        fileChunksMeta.setChunkSize(256);
//        fileChunksMeta.setChunkNum((int) fileChunksMeta.getFileSize() / fileChunksMeta.getChunkSize() + 1);
        return fileChunksMetaResponse;
    }

    private boolean uploadChunk(String fileId, int chunkIdx, byte[] chunkData) throws BrokerException {
        // this.svrUrl + "/uploadChunk"
        StringBuffer params = new StringBuffer();
        params.append("fileId=" + fileId);
        params.append("chunkIdx=" + chunkIdx);
        params.append("chunkData=" + chunkData);
        HttpGet httpGet = new HttpGet(this.svrUrl + "/uploadChunk" + "?" + params);

        FileChunksMeta fileChunksMetaResponse;
        CloseableHttpResponse closeResponse;
        try {
            closeResponse = httpClient.execute(httpGet);
            String responseResult = EntityUtils.toString(closeResponse.getEntity());
            fileChunksMetaResponse = DataUtils.parseObject(responseResult, FileChunksMeta.class);
        } catch (IOException e) {
            log.error("execute http request :{} error, e:{}", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }

        log.info("upload chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        return fileChunksMetaResponse.getChunkStatus().get(chunkIdx);
    }

    private byte[] downloadChunk(String fileId, int chunkIdx) throws BrokerException {
        // this.svrUrl + "/downloadChunk"
        StringBuffer params = new StringBuffer();
        params.append("fileId=" + fileId);
        params.append("chunkIdx=" + chunkIdx);
        HttpGet httpGet = new HttpGet(this.svrUrl + "/downloadChunk" + "?" + params);

        CloseableHttpResponse closeResponse;
        byte[] chunkData;
        try {
            closeResponse = httpClient.execute(httpGet); // byte[]
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            closeResponse.getEntity().writeTo(bos);
            chunkData = bos.toByteArray();
        } catch (IOException e) {
            log.error("execute http request :{} error, e:{}", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }

        // byte[] chunkData = new byte[1];
        log.info("download chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        return chunkData;
    }


}
