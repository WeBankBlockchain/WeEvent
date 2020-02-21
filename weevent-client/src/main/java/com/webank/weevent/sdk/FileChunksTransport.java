package com.webank.weevent.sdk;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        SendResult sendResult = new SendResult(SendResult.SendResultStatus.SUCCESS);
        sendResult.setTopic(topic);

        String fileId = "";
        String eventId = "";

        File file = new File(localFile);
        String md5;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            md5 = DigestUtils.md5DigestAsHex(fileInputStream);
        } catch (Exception e) {
            log.error("md5sum failed", e);
            throw e;
        }

        // get file initial information
        try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
            FileChunksMeta fileChunksMeta = new FileChunksMeta("",
                    file.getName(),
                    file.length(),
                    md5,
                    topic,
                    groupId);
            // get chunk information
            fileChunksMeta = this.openFileChunksInfo(fileChunksMeta);
            if (StringUtils.isBlank(fileId)) {
                fileId = fileChunksMeta.getFileId();
            }

            // upload every single chunk data
            for (int chunkIdx = 0; chunkIdx < fileChunksMeta.getChunkNum(); chunkIdx++) {
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
                SendResult chunkSendResult = this.uploadChunk(fileChunksMeta.getFileId(), file.getName(), chunkIdx, chunkData);
                log.info("upload file chunk data, {}@{}", chunkSendResult, chunkIdx);
                if (!StringUtils.isBlank(chunkSendResult.getEventId())) {
                    eventId = chunkSendResult.getEventId();
                }
            }
        }

        //TODO list chunk and check is it complete
        FileChunksMeta fileChunksMeta = this.getFileChunksInfo(fileId);
        while (!fileChunksMeta.checkChunkFull()) {
            // TODO try again
            fileChunksMeta = this.getFileChunksInfo(fileId);
            if (!fileChunksMeta.checkChunkFull()) {
                sendResult.setStatus(SendResult.SendResultStatus.ERROR);
                return sendResult;
            }
        }

        log.info("upload file complete, {}", localFile);
        // TODO invoke closeChunk
        SendResult closeChunkResult = this.closeChunk(fileId);
        if (Objects.equals(closeChunkResult.getStatus(), SendResult.SendResultStatus.ERROR)) {
            log.error("close chunk error, fileId:{}", fileId);
            sendResult.setStatus(SendResult.SendResultStatus.ERROR);
        }
        sendResult.setEventId(eventId);
        return sendResult;
    }

    public String download(String host, String fileId) throws BrokerException, IOException {
        log.info("try to download file, {}@{}", fileId, host);

        // get chunk information
        FileChunksMeta fileChunksMeta = this.getFileChunksInfo(fileId);

        // create file
        String fileName = this.downloadFilePath + "/" + fileChunksMeta.getFileName();

        try (RandomAccessFile f = new RandomAccessFile(fileName, "rw")) {
            // download every single chunk data
            for (int chunkIdx = 0; chunkIdx < fileChunksMeta.getChunkNum(); chunkIdx++) {
                byte[] chunkData = this.downloadChunk(fileChunksMeta.getFileId(), chunkIdx);
                f.seek(chunkIdx * fileChunksMeta.getChunkSize());
                f.write(chunkData);
            }
        } catch (BrokerException e) {
            throw e;
        }

        // check md5sum
        String md5;
        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
            md5 = DigestUtils.md5DigestAsHex(fileInputStream);
        } catch (Exception e) {
            throw e;
        }

        if (!fileChunksMeta.getFileMd5().equals(md5)) {
            log.error("md5 mismatch, {} <=> {}", fileChunksMeta.getFileMd5(), md5);
            throw new BrokerException("");
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
            String responseResult = EntityUtils.toString(httpResponse.getEntity());
            return JsonHelper.json2Object(responseResult, FileChunksMeta.class);
        } catch (IOException e) {
            log.error("execute http request: {} error", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }
    }

    private FileChunksMeta getFileChunksInfo(String fileId) throws BrokerException {
        HttpGet httpGet = new HttpGet(this.svrUrl + "/listChunk?fileId=" + fileId);

        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpGet)) {
            String responseResult = EntityUtils.toString(httpResponse.getEntity());
            return JsonHelper.json2Object(responseResult, FileChunksMeta.class);
        } catch (IOException e) {
            log.error("execute http request :{} error, e:{}", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }
    }

    private SendResult uploadChunk(String fileId, String fileName, int chunkIdx, byte[] chunkData) throws BrokerException, UnsupportedEncodingException {
        // this.svrUrl + "/uploadChunk"
        String uploadUrl = this.svrUrl + "/uploadChunk";
        HttpPost httpPost = new HttpPost(uploadUrl);

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.RFC6532);
        multipartEntityBuilder.addBinaryBody("chunkData", chunkData, ContentType.DEFAULT_BINARY, fileName);
        multipartEntityBuilder.addTextBody("fileId", fileId);
        multipartEntityBuilder.addTextBody("chunkIdx", String.valueOf(chunkIdx));
        HttpEntity requestEntity = multipartEntityBuilder.build();

        httpPost.setEntity(requestEntity);
        // httpPost.setHeader("Content-type", "application/octet-stream");
        SendResult sendResult;
        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpPost)) {
            String responseResult = EntityUtils.toString(httpResponse.getEntity());
            sendResult = JsonHelper.json2Object(responseResult, SendResult.class);
        } catch (IOException e) {
            log.error("execute http request url:{} error, e:{}", uploadUrl, e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }

        log.info("upload chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        return sendResult;
    }

    private byte[] downloadChunk(String fileId, int chunkIdx) throws BrokerException {
        // this.svrUrl + "/downloadChunk"
        String params = "fileId=" + fileId +
                "&chunkIdx=" + chunkIdx;
        HttpGet httpGet = new HttpGet(this.svrUrl + "/downloadChunk?" + params);

        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpGet)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            httpResponse.getEntity().writeTo(bos);
            byte[] chunkData = bos.toByteArray();
            log.info("download chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
            return chunkData;
        } catch (IOException e) {
            log.error("execute http request :{} error, e:{}", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }
    }

    private SendResult closeChunk(String fileId) throws BrokerException {
        // this.svrUrl + "/closeChunk"
        String params = "fileId=" + fileId;
        HttpGet httpGet = new HttpGet(this.svrUrl + "/closeChunk?" + params);

        try (CloseableHttpResponse httpResponse = this.httpClient.execute(httpGet)) {
            String responseResult = EntityUtils.toString(httpResponse.getEntity());
            SendResult closeChunkResult = JsonHelper.json2Object(responseResult, SendResult.class);
            log.info("closeChunk chunk success, {}, {}", fileId, closeChunkResult);
            return closeChunkResult;
        } catch (IOException e) {
            log.error("execute http request :{} error, e:{}", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        }
    }
}
