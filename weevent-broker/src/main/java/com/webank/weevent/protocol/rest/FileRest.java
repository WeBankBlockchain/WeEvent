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
    public FileChunksMeta openChunk(@RequestParam(name = "topic") String topic,
                                    @RequestParam(name = "groupId", required = false) String groupId,
                                    @RequestParam(name = "fileName") String fileName,
                                    @RequestParam(name = "fileSize") long fileSize,
                                    @RequestParam(name = "md5") String md5) throws BrokerException {
        log.info("groupId:{} md5:{}", groupId, md5);

        checkSupport();

        ParamCheckUtils.validateFileName(fileName);
        ParamCheckUtils.validateFileSize(fileSize);
        ParamCheckUtils.validateFileMd5(md5);

        // create FileChunksMeta
        FileChunksMeta fileChunksMeta = new FileChunksMeta(WeEventUtils.generateUuid(),
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

        return fileChunksMeta;
    }

    @RequestMapping(path = "/uploadChunk")
    public SendResult uploadChunk(@RequestParam(name = "host") String host,
                                  @RequestParam(name = "fileId") String fileId,
                                  @RequestParam(name = "chunkIdx") int chunkIdx,
                                  @RequestParam(name = "chunkData") MultipartFile chunkFile) throws BrokerException, IOException {
        log.info("host:{} fileId: {}  chunkIdx: {} chunkData: {}", host, fileId, chunkIdx, chunkFile.getSize());
        checkSupport();

        byte[] chunkData = chunkFile.getBytes();

        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(chunkIdx);
        ParamCheckUtils.validateChunkData(chunkData);

        FileChunksMeta fileChunksMeta = this.zkChunksMeta.getChunks(fileId);
        // send data to FileTransportSender
        this.fileTransportService.sendChunkData(fileId, chunkIdx, chunkData);

        SendResult sendResult = new SendResult(SendResult.SendResultStatus.SUCCESS);
        sendResult.setTopic(fileChunksMeta.getTopic());
        return sendResult;
    }

    @RequestMapping(path = "/downloadChunk")
    public byte[] downloadChunk(@RequestParam(name = "host") String host,
                                @RequestParam(name = "fileId") String fileId,
                                @RequestParam(name = "chunkIdx") int chunkIdx) throws BrokerException {
        log.info("host:{} fileId: {} chunkIdx: {}", host, fileId, chunkIdx);

        checkSupport();

        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(chunkIdx);

        // get file data from FileTransportReceiver
        return this.fileTransportService.downloadChunk(fileId, chunkIdx);
    }

    @RequestMapping(path = "/listChunk")
    public FileChunksMeta listChunk(@RequestParam(name = "host") String host,
                                    @RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("host:{} fileId: {}", host, fileId);

        checkSupport();

        ParamCheckUtils.validateFileId(fileId);

        // get file chunks info from Zookeeper
        return this.zkChunksMeta.getChunks(fileId);
    }

    @RequestMapping(path = "/closeChunk")
    public SendResult closeChunk(@RequestParam(name = "host") String host,
                                 @RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("host:{} fileId: {}", host, fileId);

        checkSupport();

        ParamCheckUtils.validateFileId(fileId);

        // remove chunk meta data in zookeeper
        this.zkChunksMeta.removeChunks(fileId);

        // close channel and send WeEvent
        return this.fileTransportService.closeChannel(fileId);
    }
}
