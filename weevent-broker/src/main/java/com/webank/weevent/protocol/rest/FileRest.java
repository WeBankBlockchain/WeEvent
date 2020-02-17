package com.webank.weevent.protocol.rest;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.fisco.file.FileTransportService;
import com.webank.weevent.broker.fisco.file.ZKChunksMeta;
import com.webank.weevent.broker.fisco.util.ParamCheckUtils;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.FileChunksMeta;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

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
@Slf4j
@RequestMapping(value = "/file")
@RestController
public class FileRest {
    private ZKChunksMeta zkChunksMeta;
    private FileTransportService fileTransportService;
    private IProducer producer;

    @Autowired(required = false)
    public void setZkChunksMeta(ZKChunksMeta zkChunksMeta) {
        this.zkChunksMeta = zkChunksMeta;
    }

    @Autowired(required = false)
    public void setFileTransportService(FileTransportService fileTransportService) {
        this.fileTransportService = fileTransportService;
    }

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    private void checkSupport() throws BrokerException {
        if (this.fileTransportService == null) {
            log.error("DO NOT SUPPORT file transport without zookeeper");
            throw new BrokerException(ErrorCode.ZOOKEEPER_NOT_SUPPORT_FILE_SUBSCRIPTION);
        }
    }

    @RequestMapping(path = "/createChunk")
    public FileChunksMeta createChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                      @RequestParam(name = "fileName") String fileName,
                                      @RequestParam(name = "fileSize") long fileSize,
                                      @RequestParam(name = "md5") String md5) throws BrokerException {
        log.info("groupId:{} md5:{}", groupId, md5);

        checkSupport();

        ParamCheckUtils.validateFileName(fileName);
        ParamCheckUtils.validateFileSize(fileSize);
        ParamCheckUtils.validateFileMd5(md5);

        String fileId = WeEventUtils.generateUuid();
        // create FileChunksMeta
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        fileChunksMeta.setFileId(fileId);
        fileChunksMeta.setFileName(fileName);
        fileChunksMeta.setFileSize(fileSize);
        fileChunksMeta.setFileMd5(md5);
        fileChunksMeta.setChunkSize(BrokerApplication.weEventConfig.getChunkSize());

        // create AMOP channel with FileTransportSender
        this.fileTransportService.openChannel(fileChunksMeta);

        // update to Zookeeper
        this.zkChunksMeta.addChunks(fileId, fileChunksMeta);

        return fileChunksMeta;
    }

    @RequestMapping(path = "/uploadChunk")
    public SendResult uploadChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                  @RequestParam(name = "topic") String topic,
                                  @RequestParam(name = "fileId") String fileId,
                                  @RequestParam(name = "chunkIdx") String chunkIdx,
                                  @RequestParam(name = "chunkData") byte[] chunkData) throws BrokerException, InterruptedException, ExecutionException, TimeoutException {
        log.info("groupId: {}  fileId: {}  chunkIdx: {}", groupId, fileId, chunkIdx);
        checkSupport();

        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(Integer.parseInt(chunkIdx));
        ParamCheckUtils.validateChunkData(chunkData);

        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        fileChunksMeta.setTopic(topic);
        fileChunksMeta.setChunkNum(Integer.parseInt(chunkIdx));
        fileChunksMeta.setFileId(fileId);

        SendResult sendResult = new SendResult();
        sendResult.setTopic(topic);
        sendResult.setFinish(false);
        sendResult.setStatus(SendResult.SendResultStatus.FILE_UPLOAD_NOT_YET);

        // send data to FileTransportSender
        this.fileTransportService.sendChunkData(fileId, Integer.parseInt(chunkIdx), chunkData);

        // update bitmap in Zookeeper
        boolean finish = this.zkChunksMeta.setChunksBit(fileId, Integer.parseInt(chunkIdx));
        // publish file and close AMOP channel if finish
        if (finish) {
            sendResult.setFinish(true);
            Map<String, String> extensions = new HashMap<>();
            extensions.put(WeEvent.WeEvent_FILE, "1");
            WeEvent weEvent = new WeEvent(topic, fileId.getBytes(StandardCharsets.UTF_8), extensions);

            try {
                sendResult = this.producer.publish(weEvent, groupId).get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
                sendResult.setFileChunksMeta(this.zkChunksMeta.getChunks(fileId));
            } catch (InterruptedException | ExecutionException e) {
                log.error("publishWeEvent failed due to transaction execution error.", e);
                sendResult.setFileChunksMeta(fileChunksMeta);
                sendResult.setStatus(SendResult.SendResultStatus.ERROR);
            } catch (TimeoutException e) {
                log.error("publishWeEvent failed due to transaction execution timeout.", e);
                sendResult.setFileChunksMeta(fileChunksMeta);
                sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
            }

            this.fileTransportService.closeChannel(fileId);
        }

        return sendResult;
        // return this.zkChunksMeta.getChunks(fileId);
    }

    @RequestMapping(path = "/downloadChunk")
    public byte[] downloadChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                @RequestParam(name = "fileId") String fileId,
                                @RequestParam(name = "chunkIdx") int chunkIdx) throws BrokerException {
        log.info("groupId: {}  fileId: {}  chunkIdx: {}", groupId, fileId, chunkIdx);

        checkSupport();

        ParamCheckUtils.validateFileId(fileId);
        ParamCheckUtils.validateChunkIdx(chunkIdx);

        // get file data from FileTransportReceiver
        return this.fileTransportService.downloadChunk(fileId, chunkIdx);
    }

    @RequestMapping(path = "/listChunk")
    public FileChunksMeta listChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                    @RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("groupId: {} fileId: {}", groupId, fileId);

        checkSupport();

        ParamCheckUtils.validateFileId(fileId);

        // get file chunks info from Zookeeper
        return this.zkChunksMeta.getChunks(fileId);
    }
}
