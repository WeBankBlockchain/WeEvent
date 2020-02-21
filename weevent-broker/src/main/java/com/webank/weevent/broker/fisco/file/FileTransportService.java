package com.webank.weevent.broker.fisco.file;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.web3sdk.v2.Web3SDKConnector;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.FileChunksMeta;
import com.webank.weevent.sdk.JsonHelper;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.dto.ChannelResponse;

/**
 * File transport service base on AMOP.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public class FileTransportService {
    private final FiscoConfig fiscoConfig;
    private final IProducer producer;
    private final ZKChunksMeta zkChunksMeta;
    private final DiskFiles diskFiles;

    // groupId <-> AMOPChannel
    private Map<String, AMOPChannel> groupChannels = new ConcurrentHashMap<>();
    // fileId <-> FileChunksMeta
    private Map<String, FileChunksMeta> fileTransportContexts = new ConcurrentHashMap<>();

    public FileTransportService(FiscoConfig fiscoConfig, IProducer iProducer, ZKChunksMeta zkChunksMeta) {
        this.fiscoConfig = fiscoConfig;
        this.producer = iProducer;
        this.zkChunksMeta = zkChunksMeta;
        this.diskFiles = new DiskFiles(BrokerApplication.weEventConfig.getFilePath());
    }

    public void init() throws BrokerException {
        // init default group
        String defaultGroupId = WeEvent.DEFAULT_GROUP_ID;
        Service service = Web3SDKConnector.initService(Long.valueOf(defaultGroupId), this.fiscoConfig);
        AMOPChannel channel = new AMOPChannel(this, service);
        this.groupChannels.put(defaultGroupId, channel);
    }

    private AMOPChannel getChannel(String groupId) throws BrokerException {
        if (this.groupChannels.containsKey(groupId)) {
            return this.groupChannels.get(groupId);
        }

        // init if not exist
        log.info("amop channel is not exist, init for groupId: {}", groupId);
        Service service = Web3SDKConnector.initService(Long.valueOf(groupId), this.fiscoConfig);
        AMOPChannel channel = new AMOPChannel(this, service);
        this.groupChannels.put(groupId, channel);
        return channel;
    }

    // CGI interface

    // called by sender cgi
    public void openChannel(FileChunksMeta fileChunksMeta) throws BrokerException {
        if (this.fileTransportContexts.containsKey(fileChunksMeta.getFileId())) {
            log.error("already exist file context, fileId: {}", fileChunksMeta.getFileId());
            return;
        }

        // check channel is exist
        this.getChannel(fileChunksMeta.getGroupId());

        // send WeEvent to start
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileTransportStart);
        fileEvent.setFileChunksMeta(fileChunksMeta);
        Map<String, String> extensions = new HashMap<>();
        extensions.put(WeEvent.WeEvent_FILE, "1");
        extensions.put(WeEvent.WeEvent_FORMAT, "json");
        WeEvent startTransport = new WeEvent(fileChunksMeta.getTopic(), JsonHelper.object2JsonBytes(fileEvent), extensions);

        SendResult sendResult = this.producer.publishSync(startTransport, fileChunksMeta.getGroupId());
        log.info("send start WeEvent to receiver result, {}", sendResult);

        this.fileTransportContexts.put(fileChunksMeta.getFileId(), fileChunksMeta);
    }

    public SendResult closeChannel(String fileId) throws BrokerException {
        log.info("close amop sender channel for file, fileId: {}", fileId);

        SendResult sendResult = new SendResult(SendResult.SendResultStatus.ERROR);
        if (!this.fileTransportContexts.containsKey(fileId)) {
            log.error("not exist file context, fileId: {}", fileId);
            return sendResult;
        }

        FileChunksMeta fileChunksMeta = this.fileTransportContexts.get(fileId);
        this.fileTransportContexts.remove(fileId);

        // send WeEvent to close receiver
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileTransportEnd);
        fileEvent.setFileChunksMeta(fileChunksMeta);
        Map<String, String> extensions = new HashMap<>();
        extensions.put(WeEvent.WeEvent_FILE, "1");
        extensions.put(WeEvent.WeEvent_FORMAT, "json");
        byte[] json = JsonHelper.object2JsonBytes(fileEvent);
        WeEvent weEvent = new WeEvent(fileChunksMeta.getTopic(), json, extensions);

        return this.producer.publishSync(weEvent, fileChunksMeta.getGroupId());
    }

    public void sendChunkData(String fileId, int chunkIndex, byte[] data) throws BrokerException {
        if (!this.fileTransportContexts.containsKey(fileId)) {
            log.error("not exist file context, fileId: {}", fileId);
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST_CONTEXT);
        }

        FileChunksMeta fileChunksMeta = this.fileTransportContexts.get(fileId);
        if (chunkIndex >= fileChunksMeta.getChunkNum()
                || data.length > fileChunksMeta.getChunkSize()) {
            log.error("invalid chunk data, skip");
            throw new BrokerException(ErrorCode.FILE_INVALID_CHUNK);
        }

        // check receiver ready
        String amopTopic = AMOPChannel.genTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId());
        AMOPChannel channel = this.getChannel(fileChunksMeta.getGroupId());
        if (!channel.checkReceiverAlready(amopTopic)) {
            log.error("amop channel haven't ready");
            throw new BrokerException(ErrorCode.FILE_CHANNEL_NOT_READY);
        }

        log.info("send chunk data via amop channel, {}@{}", fileId, chunkIndex);

        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelData);
        fileEvent.setFileChunksMeta(fileChunksMeta);
        fileEvent.setChunkIndex(chunkIndex);
        fileEvent.setChunkData(data);
        ChannelResponse rsp = channel.sendEvent(amopTopic, fileEvent);
        if (rsp.getErrorCode() != 0) {
            BrokerException e = AMOPChannel.toBrokerException(rsp);
            log.error("sender chunk data to remote failed", e);
            throw e;
        } else {
            log.info("sender chunk data to remote success, try to update FileChunksMeta in zookeeper");
            this.flushZKFileChunksMeta(fileEvent.getFileChunksMeta());
        }
    }

    public byte[] downloadChunk(String fileId, int chunkIndex) throws BrokerException {
        log.info("download chunk data, {}@{}", fileId, chunkIndex);
        return this.diskFiles.readChunkData(fileId, chunkIndex);
    }

    // WeEvent interface

    /**
     * call by FileEvent.EventType.FileTransportStart
     * open amop channel to received file data, and create local file(include data and meta).
     *
     * @param fileChunksMeta file meta
     */
    public void prepareReceiveFile(FileChunksMeta fileChunksMeta) {
        String fileId = fileChunksMeta.getFileId();
        if (this.fileTransportContexts.containsKey(fileId)) {
            log.error("already exist file receiving context, fileId: {}", fileId);
            return;
        }

        log.info("initialize file context for receiving, fileId: {}", fileId);

        // check channel exist
        AMOPChannel channel = this.groupChannels.get(fileChunksMeta.getGroupId());
        try {
            String amopTopic = AMOPChannel.genTopic(fileChunksMeta.getTopic(), fileId);
            channel.subTopic(amopTopic);

            // create local file
            this.diskFiles.createFixedLengthFile(fileId, fileChunksMeta.getFileSize());
            this.diskFiles.saveFileMeta(fileChunksMeta);

            this.fileTransportContexts.put(fileId, fileChunksMeta);
        } catch (BrokerException e) {
            log.error("initialize file receiving context failed", e);
        }
    }

    public void cleanUpReceivedFile(String fileId) {
        // close receiver amop channel
        if (!this.fileTransportContexts.containsKey(fileId)) {
            log.error("not exist file receiving context, fileId: {}", fileId);
            return;
        }

        log.info("finalize file context for receiving, fileId: {}", fileId);
        try {
            FileChunksMeta localFileChunksMeta = this.fileTransportContexts.get(fileId);
            AMOPChannel amopChannel = this.getChannel(localFileChunksMeta.getGroupId());
            String amopTopic = AMOPChannel.genTopic(localFileChunksMeta.getTopic(), fileId);
            amopChannel.unSubTopic(amopTopic);
            this.fileTransportContexts.remove(fileId);

            // local file CAN NOT delete, because client will downloadChunk after received this WeEvent
            FileChunksMeta fileChunksMeta = this.diskFiles.loadFileMeta(fileId);
            if (!fileChunksMeta.checkChunkFull()) {
                log.info("try to delete file not complete, {}", fileId);
                // delete not complete file in local disk
                this.diskFiles.cleanUp(fileId);
            }
        } catch (BrokerException e) {
            log.error("clean up not complete file failed", e);
        }
    }

    // Notice: always believe FileChunksMeta in local file
    public FileChunksMeta writeChunkData(FileEvent fileEvent) throws BrokerException {
        return this.diskFiles.writeChunkData(fileEvent.getFileChunksMeta().getFileId(), fileEvent.getChunkIndex(), fileEvent.getChunkData());
    }

    public void flushZKFileChunksMeta(FileChunksMeta fileChunksMeta) throws BrokerException {
        try {
            boolean full = this.zkChunksMeta.updateChunks(fileChunksMeta.getFileId(), fileChunksMeta);
            if (full) {
                log.info("all chunk bit is set on, file complete {}", fileChunksMeta.getFileId());
            }
        } catch (BrokerException e) {
            log.error("update FileChunksMeta in zookeeper failed", e);
            throw e;
        }
    }
}
