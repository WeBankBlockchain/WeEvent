package com.webank.weevent.client;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
    private static final int HTTP_RESPONSE_STATUS_SUCCESS = 200;

    public FileChunksTransport(String svrUrl) {
        this.svrUrl = svrUrl;
        this.httpClient = HttpClientBuilder.create().build();
    }

    public FileChunksTransport(String svrUrl, String downloadFilePath) {
        this.svrUrl = svrUrl;
        this.downloadFilePath = downloadFilePath;
        this.httpClient = HttpClientBuilder.create().build();
    }

    public SendResult upload(String localFile, String topic, String groupId) throws BrokerException, IOException {
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
        String fileId;
        try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
            fileChunksMeta = new FileChunksMeta("",
                    file.getName(),
                    file.length(),
                    md5,
                    topic,
                    groupId);
            // get chunk information
            fileChunksMeta = this.openFileChunksInfo(fileChunksMeta);
            fileId = fileChunksMeta.getFileId();

            // upload every single chunk data
            for (int chunkIdx = 0; chunkIdx < fileChunksMeta.getChunkNum(); chunkIdx++) {
                this.uploadChunkDetails(fileChunksMeta.getHost(), f, fileChunksMeta, chunkIdx);
            }

            boolean chunkFullUpload = checkChunkFullUpload(fileChunksMeta.getHost(), f, fileId);
            if (!chunkFullUpload) {
                log.error("upload file :{} failed", file.getName());
                throw new BrokerException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        log.info("upload file complete, {}", localFile);
        return this.closeChunk(fileChunksMeta.getHost(), fileId);
    }

    private void uploadChunkDetails(String host, RandomAccessFile f, FileChunksMeta fileChunksMeta, int chunkIdx) throws IOException, BrokerException {
        int size = fileChunksMeta.getChunkSize();
        if (chunkIdx == fileChunksMeta.getChunkNum() - 1) {
            size = (int) (fileChunksMeta.getFileSize() % fileChunksMeta.getChunkSize());
        }
        f.seek(chunkIdx * fileChunksMeta.getChunkSize());
        byte[] chunkData = new byte[size];
        int readSize = f.read(chunkData);
        if (readSize != size) {
            log.error("read file exception, chunkIdx: {}", chunkIdx);
            throw new BrokerException(ErrorCode.FILE_READ_EXCEPTION);
        }
        this.uploadChunk(fileChunksMeta.getFileId(), host, chunkIdx, chunkData);

        log.info("upload file chunk data, {}@{}", fileChunksMeta.getFileId(), chunkIdx);
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
                for (int i = 0; i < CHUNK_RETRY_COUNT; i++) {
                    chunkData = this.downloadChunk(host, fileChunksMeta.getFileId(), chunkIdx);
                    if (chunkData.length > 0) {
                        break;
                    }
                }
                if (chunkData.length == 0) {
                    log.error("download file failed, host:{} fileId:{}", host, fileId);
                    throw new BrokerException(ErrorCode.FILE_DOWNLOAD_ERROR);
                }

                f.seek(chunkIdx * fileChunksMeta.getChunkSize());
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
        String params = "topic=" + fileChunksMeta.getTopic() +
                "&groupId=" + fileChunksMeta.getGroupId() +
                "&fileName=" + fileChunksMeta.getFileName() +
                "&fileSize=" + fileChunksMeta.getFileSize() +
                "&md5=" + fileChunksMeta.getFileMd5();
        HttpGet httpGet = new HttpGet(this.svrUrl + "/openChunk?" + params);

        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpGet)) {
            byte[] responseResult = this.parseHttpResponse(httpResponse);
            BaseResponse baseResponse = JsonHelper.json2Object(responseResult, BaseResponse.class);
            if (ErrorCode.SUCCESS.getCode() != baseResponse.getCode()) {
                log.error("open chunk failed, filedId:{} msg:{}", fileChunksMeta.getFileName(), baseResponse.getMessage());
                throw new BrokerException(baseResponse.getCode(), baseResponse.getMessage());
            }
            return JsonHelper.object2Bean(baseResponse.getData(), FileChunksMeta.class);
        } catch (IOException e) {
            log.error("execute http request: {} error", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }
    }

    private FileChunksMeta getFileChunksInfo(String host, String fileId) throws BrokerException {
        HttpGet httpGet = new HttpGet(this.svrUrl + "/listChunk?fileId=" + fileId);
        httpGet.addHeader("file_host", host);

        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpGet)) {
            byte[] responseResult = this.parseHttpResponse(httpResponse);
            BaseResponse baseResponse = JsonHelper.json2Object(responseResult, BaseResponse.class);
            if (ErrorCode.SUCCESS.getCode() != baseResponse.getCode()) {
                log.error("get chunk list failed, filedId:{} msg:{}", fileId, baseResponse.getMessage());
                throw new BrokerException(baseResponse.getCode(), baseResponse.getMessage());
            }
            return JsonHelper.object2Bean(baseResponse.getData(), FileChunksMeta.class);
        } catch (IOException e) {
            log.error("execute http request :{} error", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }
    }

    private void uploadChunk(String fileId, String host, int chunkIdx, byte[] chunkData) {
        // this.svrUrl + "/uploadChunk"
        boolean isUploadSuccess = true;
        String uploadUrl = this.svrUrl + "/uploadChunk";
        HttpPost httpPost = new HttpPost(uploadUrl);
        httpPost.addHeader("file_host", host);

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
        multipartEntityBuilder.addBinaryBody("chunkData", chunkData, ContentType.DEFAULT_BINARY, fileId);
        multipartEntityBuilder.addTextBody("fileId", fileId);
        multipartEntityBuilder.addTextBody("chunkIdx", String.valueOf(chunkIdx));
        HttpEntity requestEntity = multipartEntityBuilder.build();

        httpPost.setEntity(requestEntity);
        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpPost)) {
            byte[] responseResult = this.parseHttpResponse(httpResponse);
            BaseResponse baseResponse = JsonHelper.json2Object(responseResult, BaseResponse.class);
            if (ErrorCode.SUCCESS.getCode() != baseResponse.getCode()) {
                isUploadSuccess = false;
                log.error("upload chunk failed, {}@{} msg:{}", fileId, chunkIdx, baseResponse.getMessage());
            }
        } catch (IOException | BrokerException e) {
            isUploadSuccess = false;
            log.error("execute http request url:{} error", uploadUrl, e);
        }

        if (isUploadSuccess) {
            log.info("upload chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        }
    }

    private byte[] downloadChunk(String host, String fileId, int chunkIdx) {
        String params = "fileId=" + fileId + "&chunkIdx=" + chunkIdx;
        HttpGet httpGet = new HttpGet(this.svrUrl + "/downloadChunk?" + params);
        httpGet.addHeader("file_host", host);
        byte[] chunkDataBytes = new byte[0];

        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpGet)) {
            chunkDataBytes = this.parseHttpResponse(httpResponse);
            if (chunkDataBytes.length == 0) {
                log.error("download chunk failed, {}@{}", fileId, chunkIdx);
                return chunkDataBytes;
            }
            log.info("download chunk success, {}@{} {}", fileId, chunkIdx, chunkDataBytes.length);
        } catch (IOException | BrokerException e) {
            log.error("execute http request :{} error", httpGet.getURI(), e);
            return chunkDataBytes;
        }
        return chunkDataBytes;
    }

    private SendResult closeChunk(String host, String fileId) throws BrokerException {
        String params = "fileId=" + fileId;
        HttpGet httpGet = new HttpGet(this.svrUrl + "/closeChunk?" + params);
        httpGet.addHeader("file_host", host);

        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpGet)) {
            byte[] responseResult = this.parseHttpResponse(httpResponse);
            BaseResponse baseResponse = JsonHelper.json2Object(responseResult, BaseResponse.class);
            if (ErrorCode.SUCCESS.getCode() != baseResponse.getCode()) {
                log.error("close chunk failed, filedId:{} host:{} msg:{}", fileId, host, baseResponse.getMessage());
                throw new BrokerException(baseResponse.getCode(), baseResponse.getMessage());
            }
            return JsonHelper.object2Bean(baseResponse.getData(), SendResult.class);
        } catch (IOException e) {
            log.error("execute http request :{} error", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }
    }

    private boolean checkChunkFullUpload(String host, RandomAccessFile f, String fileId) {
        boolean isFullUpload = false;
        for (int i = 0; i < CHUNK_RETRY_COUNT; i++) {
            try {
                FileChunksMeta fileChunksMeta = this.getFileChunksInfo(host, fileId);
                if (fileChunksMeta.checkChunkFull()) {
                    isFullUpload = true;
                    break;
                } else {
                    List<Integer> missChunkIdxList = this.missChunkIdxList(fileChunksMeta.getChunkStatus());
                    for (Integer missChunkIdx : missChunkIdxList) {
                        this.uploadChunkDetails(fileChunksMeta.getHost(), f, fileChunksMeta, missChunkIdx);
                    }
                }
            } catch (BrokerException | IOException e) {
                log.error("checkChunkFullUpload failed ,fileId:{} retry count: {}", fileId, i);
            }
        }
        return isFullUpload;
    }

    private List<Integer> missChunkIdxList(BitSet bitSet) {
        List<Integer> missChunksList = new ArrayList<>();
        for (int i = 0; i < bitSet.length(); i++) {
            if (!bitSet.get(i)) {
                missChunksList.add(i);
            }
        }
        return missChunksList;
    }

    private byte[] parseHttpResponse(CloseableHttpResponse httpResponse) throws BrokerException {
        if (HTTP_RESPONSE_STATUS_SUCCESS != httpResponse.getStatusLine().getStatusCode()) {
            throw new BrokerException(ErrorCode.HTTP_RESPONSE_FAILED);
        }
        if (null == httpResponse.getEntity()) {
            throw new BrokerException(ErrorCode.HTTP_RESPONSE_ENTITY_EMPTY);
        }
        byte[] responseResult;
        try {
            responseResult = EntityUtils.toByteArray(httpResponse.getEntity());
        } catch (IOException e) {
            log.error("convert httpEntity to byte[] error", e);
            throw new BrokerException(ErrorCode.HTTPENTITY_TO_BYTEARRAY_ERROR);
        }
        return responseResult;
    }
}
