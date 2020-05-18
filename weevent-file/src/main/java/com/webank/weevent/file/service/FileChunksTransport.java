package com.webank.weevent.file.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.core.fisco.util.ParamCheckUtils;
import com.webank.weevent.core.fisco.util.WeEventUtils;
import com.webank.weevent.file.inner.FileTransportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

/**
 * Upload and Download File in chunks.
 *
 * @author matthewliu
 * @since 2020/02/12
 */
@Slf4j
public class FileChunksTransport {
    private FileTransportService fileTransportService;
    //private String downloadFilePath = "";

    // retry upload/download chunk times
    private static final int CHUNK_RETRY_COUNT = 5;
    private static final int INVOKE_CGI_FAIL_RETRY_COUNT = 20;
    private static final long INVOKE_CGI_FAIL_SLEEP_MILLISECOND = 3000;

    public FileChunksTransport(FileTransportService fileTransportService) {
        this.fileTransportService = fileTransportService;
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

        /*
        // if chunk upload failed, sleep and retry again
        for (int i = 1; i <= INVOKE_CGI_FAIL_RETRY_COUNT; i++) {
            Thread.sleep(INVOKE_CGI_FAIL_SLEEP_MILLISECOND * i);
            if (this.uploadChunk(fileChunksMeta, chunkIdx, chunkData)) {
                log.info("upload file chunk data success, {}@{}", fileChunksMeta.getFileId(), chunkIdx);
                return;
            }
        }
         */

        log.error("retry {}th upload chunk failed, {}@{}", INVOKE_CGI_FAIL_RETRY_COUNT, fileChunksMeta.getFileId(), chunkIdx);
        throw new BrokerException(ErrorCode.FILE_UPLOAD_FAILED);
    }

    /*
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
     */

    private FileChunksMeta openFileChunksInfo(FileChunksMeta fileChunksMeta) throws BrokerException {
        // 1. 验证参数合法性
        ParamCheckUtils.validateFileName(fileChunksMeta.getFileName());
        ParamCheckUtils.validateFileSize(fileChunksMeta.getFileSize());
        ParamCheckUtils.validateFileMd5(fileChunksMeta.getFileMd5());


        // 2. 构造参数
        FileChunksMeta newFileChunksMeta;
        try {
            newFileChunksMeta = new FileChunksMeta(WeEventUtils.generateUuid(),
                URLDecoder.decode(fileChunksMeta.getFileName(), StandardCharsets.UTF_8.toString()),
                fileChunksMeta.getFileSize(),
                fileChunksMeta.getFileMd5(),
                fileChunksMeta.getTopic(),
                fileChunksMeta.getGroupId());
        } catch (UnsupportedEncodingException e) {
            log.error("decode fileName error", e);
            throw new BrokerException(ErrorCode.DECODE_FILE_NAME_ERROR);
        }

        // 2. create AMOP channel with FileTransportSender
        FileChunksMeta remoteFileChunksMeta = this.fileTransportService.openChannel(newFileChunksMeta);
        return remoteFileChunksMeta;
    }


    private FileChunksMeta getFileChunksInfo(String topic, String groupId, String fileId) throws BrokerException {
        // 1. 验证参数有效性
        ParamCheckUtils.validateFileId(fileId);

        // 2. 调用服务



        FileChunksMeta fileChunksMeta = fileTransportService.getReceiverFileChunksMeta(topic, groupId, fileId);

        return fileChunksMeta;
    }

    private boolean uploadChunk(FileChunksMeta local, int chunkIdx, byte[] chunkData) throws BrokerException {
        // 1. 验证参数有效性
        ParamCheckUtils.validateFileId(local.getFileId());
        ParamCheckUtils.validateChunkIdx(chunkIdx);
        ParamCheckUtils.validateChunkData(chunkData);

        // 2. 调用服务
        fileTransportService.sendChunkData(local.getTopic(), local.getGroupId(), local.getFileId(), chunkIdx, chunkData);

        return true;
    }

    private byte[] downloadChunk(String host, String fileId, int chunkIdx) throws BrokerException {
        // 1. 验证参数有效性
        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(chunkIdx);

        // 2. 调用服务
        byte[] downloadChunkBytes = this.fileTransportService.downloadChunk(fileId, chunkIdx);
        if (downloadChunkBytes.length == 0) {
            throw new BrokerException(ErrorCode.FILE_DOWNLOAD_ERROR);
        }

        return downloadChunkBytes;
    }

    private SendResult closeChunk(FileChunksMeta local) throws BrokerException {
        // 1. 验证参数有效性
        ParamCheckUtils.validateFileId(local.getFileId());

        // 2. 调用服务
        // close channel and send WeEvent
        SendResult sendResult = this.fileTransportService.closeChannel(local.getTopic(), local.getGroupId(), local.getFileId());

        return sendResult;
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
