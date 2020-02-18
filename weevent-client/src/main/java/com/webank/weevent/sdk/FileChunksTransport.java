package com.webank.weevent.sdk;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
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

    public SendResult upload(String localFile, String groupId, String topic) throws BrokerException, IOException {
        log.info("try to upload file {}", localFile);
        SendResult sendResult = new SendResult();

        // get file initial information
        File file = new File(localFile);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
            FileChunksMeta fileChunksMeta = new FileChunksMeta(file.getName(), file.length(), md5, topic, groupId);
            // get chunk information
            fileChunksMeta = this.getFileChunksInfo(fileChunksMeta);

            // upload every single chunk data
            for (int chunkIdx = 0; chunkIdx < fileChunksMeta.getChunkNum(); chunkIdx++) {
                int size = fileChunksMeta.getChunkSize();
                if (chunkIdx == fileChunksMeta.getChunkNum() - 1) {
                    size = (int) (fileChunksMeta.getFileSize() % fileChunksMeta.getChunkSize());
                }
                byte[] chunkData = new byte[size];
                int readSize = fileInputStream.read(chunkData, chunkIdx * fileChunksMeta.getChunkSize(), size);
                if (readSize != size) {
                    log.error("read file exception, chunkIdx: {}", chunkIdx);
                    throw new BrokerException(ErrorCode.FILE_READ_EXCEPTION);
                }
                sendResult = this.uploadChunk(topic, fileChunksMeta.getFileId(), chunkIdx, chunkData);
                log.info("upload file chunk data, {}@{}", sendResult, chunkIdx);
            }
        }

        //TODO list chunk and check is it complete
        FileChunksMeta fileChunksMeta = null;// = this.getFileChunksInfo(fileChunksMeta);
        while (!fileChunksMeta.isFull()) {
            // TODO try again
        }

        log.info("upload file complete, {}", localFile);
        // TODO invoke removeChunk
        return sendResult;
    }

    public String download(String host, String fileId) throws BrokerException, IOException {
        log.info("try to download file, {}@{}", fileId, host);

        // get chunk information
        FileChunksMeta fileChunksMeta = new FileChunksMeta(fileId);
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

    private FileChunksMeta getFileChunksInfo(FileChunksMeta fileChunksMeta) throws BrokerException {
        FileChunksMeta fileChunksMetaResponse;
        StringBuffer params = new StringBuffer();

        HttpGet httpGet;

        if (StringUtils.isEmpty(fileChunksMeta.getFileId())) { // first create
            params.append("groupId=").append(fileChunksMeta.getGroupId());
            params.append("&fileName=").append(fileChunksMeta.getFileName());
            params.append("&fileSize=").append(fileChunksMeta.getFileSize());
            params.append("&md5=").append(fileChunksMeta.getFileMd5());
            httpGet = new HttpGet(this.svrUrl + "/createChunk" + "?" + params.toString());
        } else { // continue listChunk
            params.append("fileId=").append(fileChunksMeta.getFileId());
            httpGet = new HttpGet(this.svrUrl + "/listChunk" + "?" + params.toString());
        }

        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = this.httpClient.execute(httpGet);
            String responseResult = EntityUtils.toString(httpResponse.getEntity());
            fileChunksMetaResponse = JsonHelper.json2Object(responseResult, FileChunksMeta.class);
        } catch (IOException e) {
            log.error("execute http request :{} error, e:{}", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        } finally {
            closeHttpResponse(httpResponse, "getFileChunksInfo");
        }

//        fileChunksMeta.setFileId("a");
//        fileChunksMeta.setChunkSize(256);
//        fileChunksMeta.setChunkNum((int) fileChunksMeta.getFileSize() / fileChunksMeta.getChunkSize() + 1);
        return fileChunksMetaResponse;
    }

    private SendResult uploadChunk(String topic, String fileId, int chunkIdx, byte[] chunkData) throws BrokerException {
        // this.svrUrl + "/uploadChunk"
        URI svrUri = URI.create(this.svrUrl + "/uploadChunk");
        URI uploadUri;
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("topic", topic));
            params.add(new BasicNameValuePair("fileId", fileId));
            params.add(new BasicNameValuePair("chunkIdx", String.valueOf(chunkIdx)));
            uploadUri = new URIBuilder().setScheme(svrUri.getScheme()).setHost(svrUri.getHost()).setPort(svrUri.getPort())
                    .setPath(svrUri.getPath()).setParameters(params).build();
        } catch (URISyntaxException e) {
            log.error("build upload url error, fileId:{}, chunkIdx:{}, e:{}", fileId, chunkIdx, e);
            throw new BrokerException(ErrorCode.BUILD_HTTP_URL_ERROR);
        }

        HttpPost httpPost = new HttpPost(uploadUri);

        httpPost.setEntity(new ByteArrayEntity(chunkData));
        httpPost.setHeader("Content-type", "application/octet-stream");
        CloseableHttpResponse httpResponse = null;
        SendResult sendResult;
        try {
            httpResponse = this.httpClient.execute(httpPost);
            String responseResult = EntityUtils.toString(httpResponse.getEntity());
            sendResult = JsonHelper.json2Object(responseResult, SendResult.class);
        } catch (IOException e) {
            log.error("execute http request url:{} error, e:{}", svrUri, e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        } finally {
            closeHttpResponse(httpResponse, "upload");
        }

        log.info("upload chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        return sendResult;
    }

    private byte[] downloadChunk(String fileId, int chunkIdx) throws BrokerException {
        // this.svrUrl + "/downloadChunk"
        StringBuffer params = new StringBuffer();
        params.append("fileId=").append(fileId);
        params.append("&chunkIdx=").append(chunkIdx);
        HttpGet httpGet = new HttpGet(this.svrUrl + "/downloadChunk" + "?" + params.toString());

        CloseableHttpResponse httpResponse = null;
        byte[] chunkData;
        try {
            httpResponse = this.httpClient.execute(httpGet);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            httpResponse.getEntity().writeTo(bos);
            chunkData = bos.toByteArray();
        } catch (IOException e) {
            log.error("execute http request :{} error, e:{}", httpGet.getURI(), e);
            throw new BrokerException(ErrorCode.HTTP_REQUEST_EXECUTE_ERROR);
        } finally {
            closeHttpResponse(httpResponse, "download");
        }

        // byte[] chunkData = new byte[1];
        log.info("download chunk success, {}@{} {}", fileId, chunkIdx, chunkData.length);
        return chunkData;
    }

    private void closeHttpResponse(CloseableHttpResponse httpResponse, String func) throws BrokerException {
        try {
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (IOException e) {
            log.error("{} httpPost close error, e:{}", func, e);
            throw new BrokerException(ErrorCode.HTTPRESPONSE_CLOSE_ERROR);
        }
    }
}
