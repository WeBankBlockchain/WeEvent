package com.webank.weevent.protocol.rest;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.file.FileTransportReceiver;
import com.webank.weevent.broker.fisco.file.FileTransportSender;
import com.webank.weevent.broker.fisco.file.ZKChunksMeta;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.FileChunksMeta;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author v_wbhwliu
 * @version 1.2
 * @since 2020/2/12
 */
@RequestMapping(value = "/file")
@RestController
@Slf4j
public class FileRest {
    private IProducer producer;
    private ZKChunksMeta zkChunksMeta;
    private FileTransportSender fileTransportSender;
    private FileTransportReceiver fileTransportReceiver;

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    @Autowired
    public void setZkChunksMeta(ZKChunksMeta zkChunksMeta) {
        this.zkChunksMeta = zkChunksMeta;
    }

    @Autowired
    public void setFileTransportSender(FileTransportSender fileTransportSender) {
        this.fileTransportSender = fileTransportSender;
    }

    @Autowired
    public void fileTransportReceiver(FileTransportReceiver fileTransportReceiver) {
        this.fileTransportReceiver = fileTransportReceiver;
    }

    @RequestMapping(path = "/createChunk")
    public FileChunksMeta createChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                      @RequestParam(name = "fileName") String fileName,
                                      @RequestParam(name = "fileSize") long fileSize,
                                      @RequestParam(name = "md5") String md5) throws BrokerException {
        log.info("createChunk, groupId:{} md5:{}", groupId, md5);

        ParamCheckUtils.validateFileName(fileName);
        ParamCheckUtils.validateFileSize(fileSize);
        ParamCheckUtils.validateFileMd5(md5);

        String fileId = WeEventUtils.generateUuid();

        // create AMOP channel with FileTransportSender
        this.fileTransportSender.openChannel(fileId);

        // create FileChunksMeta
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        fileChunksMeta.setFileId(fileId);
        fileChunksMeta.setFileName(fileName);
        fileChunksMeta.setFileSize(fileSize);
        fileChunksMeta.setFileMd5(md5);
        fileChunksMeta.setChunkSize(BrokerApplication.weEventConfig.getChunkSize());

        // update to Zookeeper
        this.zkChunksMeta.addChunks(fileId, fileChunksMeta);

        return fileChunksMeta;
    }

    @RequestMapping(path = "/uploadChunk")
    public FileChunksMeta uploadChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                      @RequestParam(name = "fileId") String fileId,
                                      @RequestParam(name = "chunkIdx") int chunkIdx,
                                      @RequestParam(name = "chunkData") byte[] chunkData) throws BrokerException {
        log.info("uploadChunk, groupId:{}. fileId:{}. chunkIdx:{}", groupId, fileId, chunkIdx);

        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(chunkIdx);
        ParamCheckUtils.validateChunkData(chunkData);

        // send data to FileTransportSender
        this.fileTransportSender.send(fileId, chunkIdx, chunkData);

        // update bitmap in Zookeeper
        boolean finish = this.zkChunksMeta.setChunksBit(fileId, chunkIdx);
        // close AMOP channel if finish
        if (finish) {
            this.fileTransportReceiver.closeChannel(fileId);
            this.fileTransportSender.closeChannel(fileId);
        }

        return this.zkChunksMeta.getChunks(fileId);
    }

    @RequestMapping(path = "/downloadChunk")
    public byte[] downloadChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                @RequestParam(name = "fileId") String fileId,
                                @RequestParam(name = "chunkIdx") int chunkIdx) throws BrokerException {
        log.info("downloadChunk, groupId:{}. fileId:{}. chunkIdx:{}", groupId, fileId, chunkIdx);

        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(chunkIdx);

        // get file data from FileTransportReceiver
        return this.fileTransportReceiver.downloadChunk(fileId, chunkIdx);
    }

    @RequestMapping(path = "/listChunk")
    public FileChunksMeta listChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                    @RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("listChunk, groupId:{}. fileId:{}", groupId, fileId);
        ParamCheckUtils.validateFileId(fileId);

        // get file chunks info from Zookeeper
        return this.zkChunksMeta.getChunks(fileId);
    }
}
