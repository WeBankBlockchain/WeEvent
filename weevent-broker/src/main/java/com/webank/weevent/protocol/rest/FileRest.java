package com.webank.weevent.protocol.rest;

import java.io.IOException;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.file.FileTransportService;
import com.webank.weevent.broker.fisco.file.ZKChunksMeta;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.FileChunksMeta;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.BaseResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author v_wbhwliu
 * @version 1.2
 * @since 2020/2/12
 */
@Slf4j
@RequestMapping(value = "/file")
@RestController
public class FileRest {
    private ZKChunksMeta zkChunksMeta;
    private FileTransportService fileTransportService;

    @Autowired(required = false)
    public void setZkChunksMeta(ZKChunksMeta zkChunksMeta) {
        this.zkChunksMeta = zkChunksMeta;
    }

    @Autowired(required = false)
    public void setFileTransportService(FileTransportService fileTransportService) {
        this.fileTransportService = fileTransportService;
    }

    private void checkSupport() throws BrokerException {
        if (this.fileTransportService == null) {
            log.error("DO NOT SUPPORT file transport without zookeeper");
            throw new BrokerException(ErrorCode.ZOOKEEPER_NOT_SUPPORT_FILE_SUBSCRIPTION);
        }
    }

    @RequestMapping(path = "/openChunk")
    public BaseResponse<FileChunksMeta> openChunk(@RequestParam(name = "topic") String topic,
                                                  @RequestParam(name = "groupId", required = false) String groupId,
                                                  @RequestParam(name = "fileName") String fileName,
                                                  @RequestParam(name = "fileSize") long fileSize,
                                                  @RequestParam(name = "md5") String md5) {
        log.info("groupId:{} md5:{}", groupId, md5);
        FileChunksMeta fileChunksMeta;

        try {
            checkSupport();
            ParamCheckUtils.validateFileName(fileName);
            ParamCheckUtils.validateFileSize(fileSize);
            ParamCheckUtils.validateFileMd5(md5);

            // create FileChunksMeta
            fileChunksMeta = new FileChunksMeta(WeEventUtils.generateUuid(),
                    fileName,
                    fileSize,
                    md5,
                    topic,
                    groupId);
            fileChunksMeta.setChunkSize(BrokerApplication.weEventConfig.getFileChunkSize());
            fileChunksMeta.setHost(this.fileTransportService.getHost());

            // create AMOP channel with FileTransportSender
            this.fileTransportService.openChannel(fileChunksMeta);

            // update to Zookeeper
            this.zkChunksMeta.addChunks(fileChunksMeta.getFileId(), fileChunksMeta);
        } catch (BrokerException e) {
            return BaseResponse.buildException(e);
        }

        return BaseResponse.buildSuccess(fileChunksMeta);
    }

    @RequestMapping(path = "/uploadChunk")
    public BaseResponse uploadChunk(@RequestParam(name = "fileId") String fileId,
                                    @RequestParam(name = "chunkIdx") int chunkIdx,
                                    @RequestParam(name = "chunkData") MultipartFile chunkFile) {
        log.info("fileId: {}  chunkIdx: {} chunkData: {}", fileId, chunkIdx, chunkFile.getSize());

        try {
            checkSupport();
            byte[] chunkData = chunkFile.getBytes();

            ParamCheckUtils.validateFileId(fileId);
            ParamCheckUtils.validateChunkIdx(chunkIdx);
            ParamCheckUtils.validateChunkData(chunkData);

            // send data to FileTransportSender
            this.fileTransportService.sendChunkData(fileId, chunkIdx, chunkData);
        } catch (BrokerException | IOException e) {
            return BaseResponse.buildException(e);
        }

        return BaseResponse.buildSuccess();
    }

    @RequestMapping(path = "/downloadChunk")
    public BaseResponse<byte[]> downloadChunk(@RequestParam(name = "fileId") String fileId,
                                              @RequestParam(name = "chunkIdx") int chunkIdx) {
        log.info("fileId: {} chunkIdx: {}", fileId, chunkIdx);
        byte[] downloadChunkBytes;

        try {
            checkSupport();
            ParamCheckUtils.validateFileId(fileId);
            ParamCheckUtils.validateChunkIdx(chunkIdx);

            // get file data from FileTransportReceiver
            downloadChunkBytes = this.fileTransportService.downloadChunk(fileId, chunkIdx);
        } catch (BrokerException e) {
            return BaseResponse.buildException(e);
        }
        return BaseResponse.buildSuccess(downloadChunkBytes);
    }

    @RequestMapping(path = "/listChunk")
    public BaseResponse<FileChunksMeta> listChunk(@RequestParam(name = "fileId") String fileId) {
        log.info("fileId: {}", fileId);
        FileChunksMeta fileChunksMeta;

        try {
            checkSupport();
            ParamCheckUtils.validateFileId(fileId);

            // get file chunks info from Zookeeper
            fileChunksMeta = this.zkChunksMeta.getChunks(fileId);
        } catch (BrokerException e) {
            return BaseResponse.buildException(e);
        }
        return BaseResponse.buildSuccess(fileChunksMeta);
    }

    @RequestMapping(path = "/closeChunk")
    public BaseResponse<SendResult> closeChunk(@RequestParam(name = "fileId") String fileId) {
        log.info("fileId: {}", fileId);
        SendResult sendResult;
        try {
            checkSupport();
            ParamCheckUtils.validateFileId(fileId);

            // remove chunk meta data in zookeeper
            this.zkChunksMeta.removeChunks(fileId);

            // close channel and send WeEvent
            sendResult = this.fileTransportService.closeChannel(fileId);
        } catch (BrokerException e) {
            return BaseResponse.buildException(e);
        }
        return BaseResponse.buildSuccess(sendResult);
    }
}
