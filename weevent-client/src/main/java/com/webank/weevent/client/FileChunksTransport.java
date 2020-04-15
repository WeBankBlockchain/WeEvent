package com.webank.weevent.client;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.util.DigestUtils;

/**
 * Upload and Download File in chunks.
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
    // retry upload/download chunk times
    private static final int CHUNK_RETRY_COUNT = 5;
    private static final int INVOKE_CGI_FAIL_RETRY_COUNT = 20;
    private static final long INVOKE_CGI_FAIL_SLEEP_MILLISECOND = 3000;

    public FileChunksTransport(CloseableHttpClient httpClient, String svrUrl) {
        this.svrUrl = svrUrl;
        this.httpClient = httpClient;
    }

    public FileChunksTransport(CloseableHttpClient httpClient, String svrUrl, String downloadFilePath) {
        this.svrUrl = svrUrl;
        this.downloadFilePath = downloadFilePath;
        this.httpClient = httpClient;
    }

    public SendResult upload(String localFile, String topic, String groupId) throws BrokerException, IOException, InterruptedException {
        log.info("try to upload file {}", localFile);

        File file = new File(localFile);
        String md5;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            md5 = DigestUtils.md5DigestAsHex(fileInputStream);
        } catch (Exception e) {
            log.error("md5sum failed", e);
            throw new BrokerException(ErrorCode.FILE_GENERATE_MD5_ERROR);
        }

        // get file initial information
        FileChunksMeta fileChunksMeta;
        try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
            fileChunksMeta = new FileChunksMeta("",
                    file.getName(),
                    file.length(),
                    md5,
                    topic,
                    groupId);
            // get chunk information
            fileChunksMeta = this.openFileChunksInfo(fileChunksMeta);

            // upload every single chunk data
            for (int chunkIdx = 0; chunkIdx < fileChunksMeta.getChunkNum(); chunkIdx++) {
                this.uploadChunkDetails(f, fileChunksMeta, chunkIdx);
            }

            boolean chunkFullUpload = checkChunkFullUpload(f, fileChunksMeta);
            if (!chunkFullUpload) {
                log.error("upload file :{} failed", file.getName());
                throw new BrokerException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        log.info("upload file complete, {}", localFile);
        return this.closeChunk(fileChunksMeta);
    }

    private void uploadChunkDetails(RandomAccessFile f, FileChunksMeta fileChunksMeta, int chunkIdx) throws IOException, BrokerException, InterruptedException {
        int size = fileChunksMeta.getChunkSize();
        if (chunkIdx == fileChunksMeta.getChunkNum() - 1) {
            size = (int) (fileChunksMeta.getFileSize() % fileChunksMeta.getChunkSize());
        }
        f.seek((long) chunkIdx * fileChunksMeta.getChunkSize());
        byte[] chunkData = new byte[size];
        int readSize = f.read(chunkData);
        if (readSize != size) {
            log.error("read file exception, chunkIdx: {}", chunkIdx);
            throw new BrokerException(ErrorCode.FILE_READ_EXCEPTION);
        }

        if (this.uploadChunk(fileChunksMeta, chunkIdx, chunkData)) {
            log.info("upload file chunk data success, {}@{}", fileChunksMeta.getFileId(), chunkIdx);
            return;
        }

        // if chunk upload failed, sleep and retry again
        for (int i = 1; i <= INVOKE_CGI_FAIL_RETRY_COUNT; i++) {
            Thread.sleep(INVOKE_CGI_FAIL_SLEEP_MILLISECOND * i);
            if (this.uploadChunk(fileChunksMeta, chunkIdx, chunkData)) {
                log.info("upload file chunk data success, {}@{}", fileChunksMeta.getFileId(), chunkIdx);
                return;
            }
        }

        log.error("retry {}th upload chunk failed, {}@{}", INVOKE_CGI_FAIL_RETRY_COUNT, fileChunksMeta.getFileId(), chunkIdx);
        throw new BrokerException(ErrorCode.FILE_UPLOAD_FAILED);
    }

    public String download(FileChunksMeta fileChunksMeta) throws BrokerException, IOException {
        String fileId = fileChunksMeta.getFileId();
        String host = fileChunksMeta.getHost();
        log.info("try to download file, {}@{}", fileId, host);

        // create file
        String fileName = this.downloadFilePath + "/" + fileChunksMeta.getFileName();

        try (RandomAccessFile f = new RandomAccessFile(fileName, "rw")) {
            // download every single chunk data
            for (int chunkIdx = 0; chunkIdx < fileChunksMeta.getChunkNum(); chunkIdx++) {
                byte[] chunkData = new byte[0];
                for (int i = 0; i <= CHUNK_RETRY_COUNT; i++) {
                    chunkData = this.downloadChunk(host, fileChunksMeta.getFileId(), chunkIdx);
                    if (chunkData.length > 0) {
                        break;
                    }
                }
                if (chunkData.length == 0) {
                    log.error("download file failed, host:{} fileId:{}", host, fileId);
                    throw new BrokerException(ErrorCode.FILE_DOWNLOAD_ERROR);
                }

                f.seek((long) chunkIdx * fileChunksMeta.getChunkSize());
                f.write(chunkData);
            }
        }

        // check md5sum
        String md5;
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            md5 = DigestUtils.md5DigestAsHex(fileInputStream);
        } catch (Exception e) {
            log.error("generate md5 error, fileId:{}", fileId, e);
            throw new BrokerException(ErrorCode.FILE_GENERATE_MD5_ERROR);
        }

        if (!fileChunksMeta.getFileMd5().equals(md5)) {
            log.error("md5 mismatch, {} <=> {}", fileChunksMeta.getFileMd5(), md5);
            throw new BrokerException(ErrorCode.FILE_MD5_MISMATCH);
        }

        log.info("download file success, {} -> {}", fileId, fileName);
        return fileName;
    }

    private FileChunksMeta openFileChunksInfo(FileChunksMeta fileChunksMeta) throws BrokerException {
        HttpGet httpGet;
        try {
            httpGet = new HttpGet(String.format("%s/openChunk?topic=%s&groupId=%s&fileName=%s&fileSize=%s&md5=%s", this.svrUrl,
                    fileChunksMeta.getTopic(),
                    fileChunksMeta.getGroupId(),
                    URLEncoder.encode(fileChunksMeta.getFileName(), StandardCharsets.UTF_8.toString()),
                    fileChunksMeta.getFileSize(),
                    fileChunksMeta.getFileMd5()));
        } catch (UnsupportedEncodingException e) {
            log.error("encode fileName error, fileName: {}", fileChunksMeta.getFileName(), e);
            throw new BrokerException(ErrorCode.ENCODE_FILE_NAME_ERROR);
        }

        return HttpClientUtils.invokeCGI(this.httpClient, httpGet, new TypeReference<BaseResponse<FileChunksMeta>>() {
        }).getData();
    }


    private FileChunksMeta getFileChunksInfo(String topic, String groupId, String fileId) throws BrokerException {
        HttpGet httpGet = new HttpGet(String.format("%s/listChunk?topic=%s&groupId=%s&fileId=%s", this.svrUrl, topic, groupId, fileId));

        return HttpClientUtils.invokeCGI(this.httpClient, httpGet, new TypeReference<BaseResponse<FileChunksMeta>>() {
        }).getData();
    }

    private boolean uploadChunk(FileChunksMeta local, int chunkIdx, byte[] chunkData) {
        HttpPost httpPost = new HttpPost(this.svrUrl + "/uploadChunk");

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
        multipartEntityBuilder.addTextBody("topic", local.getTopic());
        multipartEntityBuilder.addTextBody("groupId", local.getGroupId());
        multipartEntityBuilder.addTextBody("fileId", local.getFileId());
        multipartEntityBuilder.addTextBody("chunkIdx", String.valueOf(chunkIdx));
        multipartEntityBuilder.addBinaryBody("chunkData", chunkData, ContentType.DEFAULT_BINARY, local.getFileId());
        HttpEntity requestEntity = multipartEntityBuilder.build();
        httpPost.setEntity(requestEntity);

        try {
            HttpClientUtils.invokeCGI(this.httpClient, httpPost, new TypeReference<BaseResponse<ObjectUtils.Null>>() {
            });
        } catch (BrokerException e) {
            log.error("upload chunk failed, {}@{} ", local.getFileId(), chunkIdx);
            return false;
        }
        return true;
    }

    private byte[] downloadChunk(String host, String fileId, int chunkIdx) {
        byte[] chunkDataBytes = new byte[0];
        long requestStartTime = System.currentTimeMillis();
        HttpGet httpGet = new HttpGet(String.format("%s/downloadChunk?fileId=%s&chunkIdx=%s", this.svrUrl, fileId, chunkIdx));
        httpGet.addHeader("file_host", host);

        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpGet)) {
            log.info("invokeCGI {} in {} millisecond, response:{}", httpGet.getURI(),
                    System.currentTimeMillis() - requestStartTime, httpResponse.getStatusLine().toString());
            if (HttpStatus.SC_OK != httpResponse.getStatusLine().getStatusCode() || null == httpResponse.getEntity()) {
                log.error("download chunk failed, {}@{}", fileId, chunkIdx);
                return chunkDataBytes;
            }

            chunkDataBytes = EntityUtils.toByteArray(httpResponse.getEntity());
            if (chunkDataBytes.length == 0) {
                log.error("download chunk failed, {}@{}", fileId, chunkIdx);
                return chunkDataBytes;
            }
            log.info("download chunk success, {}@{} {}", fileId, chunkIdx, chunkDataBytes.length);
        } catch (IOException e) {
            log.error("download chunk error, request url:{}", httpGet.getURI(), e);
        }
        return chunkDataBytes;
    }

    private SendResult closeChunk(FileChunksMeta local) throws BrokerException {
        HttpGet httpGet = new HttpGet(String.format("%s/closeChunk?topic=%s&groupId=%s&fileId=%s", this.svrUrl, local.getTopic(), local.getGroupId(), local.getFileId()));

        return HttpClientUtils.invokeCGI(this.httpClient, httpGet, new TypeReference<BaseResponse<SendResult>>() {
        }).getData();
    }

    private boolean checkChunkFullUpload(RandomAccessFile f, FileChunksMeta local) throws InterruptedException {
        boolean isFullUpload = false;
        for (int i = 0; i <= CHUNK_RETRY_COUNT; i++) {
            try {
                FileChunksMeta fileChunksMeta = this.getFileChunksInfo(local.getTopic(), local.getGroupId(), local.getFileId());
                if (fileChunksMeta.checkChunkFull()) {
                    isFullUpload = true;
                    break;
                } else {
                    for (int k = 0; k < fileChunksMeta.getChunkNum(); k++) {
                        if (!fileChunksMeta.getChunkStatus().get(k)) {
                            this.uploadChunkDetails(f, fileChunksMeta, k);
                        }
                    }

                    if (i == CHUNK_RETRY_COUNT) {
                        fileChunksMeta = this.getFileChunksInfo(local.getTopic(), local.getGroupId(), local.getFileId());
                        isFullUpload = fileChunksMeta.checkChunkFull();
                    }
                }
            } catch (BrokerException | IOException e) {
                log.error("checkChunkFullUpload failed ,fileId:{} retry count: {}", local.getFileId(), i);
            }
        }
        return isFullUpload;
    }
}
