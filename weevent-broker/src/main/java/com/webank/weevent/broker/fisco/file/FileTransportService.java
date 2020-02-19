package com.webank.weevent.broker.fisco.file;


import java.util.HashMap;
import java.util.Map;

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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.Service;

/**
 * File transport service base on AMOP.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public class FileTransportService {
    @Data
    static class FileTransportContext {
        private String fileId;
        private FileChunksMeta fileChunksMeta;
        private AMOPChannel sender;
        private AMOPChannel receiver;
    }

    private final DiskFiles diskFiles;
    private IProducer producer;
    private FiscoConfig fiscoConfig;
    private ZKChunksMeta zkChunksMeta;

    // fileId -> (FileChunksMeta, sender AMOPChannel, receiver AMOPChannel)
    private Map<String, FileTransportContext> fileTransportContexts = new HashMap<>();

    public FileTransportService() {
        this.diskFiles = new DiskFiles(BrokerApplication.weEventConfig.getFilePath());
    }

    public void setProducer(IProducer iProducer) {
        this.producer = iProducer;
    }

    public void setFiscoConfig(FiscoConfig fiscoConfig) {
        this.fiscoConfig = fiscoConfig;
    }

    public void setZkChunksMeta(ZKChunksMeta zkChunksMeta) {
        this.zkChunksMeta = zkChunksMeta;
    }

    private Service initService(String groupId) throws BrokerException {
        return Web3SDKConnector.initService(Long.valueOf(groupId), this.fiscoConfig);
    }

    // CGI interface

    // called by sender cgi
    public void openChannel(FileChunksMeta fileChunksMeta) throws BrokerException {
        FileTransportContext fileTransportContext;
        if (this.fileTransportContexts.containsKey(fileChunksMeta.getFileId())) {
            if (this.fileTransportContexts.get(fileChunksMeta.getFileId()).sender == null) {
                fileTransportContext = this.fileTransportContexts.get(fileChunksMeta.getFileId());
            } else {
                log.error("already exist file context, fileId: {}", fileChunksMeta.getFileId());
                return;
            }
        } else {
            fileTransportContext = new FileTransportContext();
            fileTransportContext.setFileId(fileChunksMeta.getFileId());
            fileTransportContext.setFileChunksMeta(fileChunksMeta);

            this.fileTransportContexts.put(fileChunksMeta.getFileId(), fileTransportContext);
        }

        // listen amop sender topic
        String amopTopic = AMOPChannel.genPublishEndianTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId());
        AMOPChannel amopChannel = new AMOPChannel(this, amopTopic, this.initService(fileChunksMeta.getGroupId()));
        fileTransportContext.setSender(amopChannel);

        // send WeEvent to start
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileTransportStart);
        fileEvent.setFileChunksMeta(fileChunksMeta);
        Map<String, String> extensions = new HashMap<>();
        extensions.put(WeEvent.WeEvent_FILE, "1");
        extensions.put(WeEvent.WeEvent_FORMAT, "json");
        WeEvent startTransport = new WeEvent(fileChunksMeta.getTopic(), JsonHelper.object2JsonBytes(fileEvent), extensions);

        SendResult sendResult = this.producer.publishSync(startTransport, fileChunksMeta.getGroupId());
        log.info("send start event result, {}", sendResult);
    }

    public SendResult closeChannel(String fileId) throws BrokerException {
        SendResult sendResult = new SendResult(SendResult.SendResultStatus.ERROR);
        if (!this.fileTransportContexts.containsKey(fileId)) {
            log.error("not exist file context, fileId: {}", fileId);
            return sendResult;
        }

        FileTransportContext fileTransportContext = this.fileTransportContexts.get(fileId);
        this.fileTransportContexts.remove(fileId);

        if (fileTransportContext.getReceiver() != null) {
            fileTransportContext.getReceiver().close();
            fileTransportContext.setReceiver(null);
        }

        if (fileTransportContext.getSender() != null) {
            fileTransportContext.getSender().close();
            fileTransportContext.setSender(null);

            FileChunksMeta fileChunksMeta = fileTransportContext.getFileChunksMeta();
            // send WeEvent to close
            FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileTransportEnd);
            fileEvent.setFileChunksMeta(fileChunksMeta);

            Map<String, String> extensions = new HashMap<>();
            extensions.put(WeEvent.WeEvent_FILE, "1");
            extensions.put(WeEvent.WeEvent_FORMAT, "json");
            byte[] json = JsonHelper.object2JsonBytes(fileEvent);
            WeEvent weEvent = new WeEvent(fileTransportContext.getFileChunksMeta().getTopic(), json, extensions);

            sendResult = this.producer.publishSync(weEvent, fileChunksMeta.getGroupId());
        }

        return sendResult;
    }

    public void sendChunkData(String fileId, int chunkIndex, byte[] data) throws BrokerException {
        if (!this.fileTransportContexts.containsKey(fileId)) {
            log.error("not exist file context, fileId: {}", fileId);
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST_CONTEXT);
        }

        FileTransportContext fileTransportContext = this.fileTransportContexts.get(fileId);
        FileChunksMeta fileChunksMeta = fileTransportContext.getFileChunksMeta();
        if (chunkIndex >= fileChunksMeta.getChunkNum()
                || data.length > fileChunksMeta.getChunkSize()) {
            log.error("invalid chunk data, skip");
            throw new BrokerException(ErrorCode.FILE_INVALID_CHUNK);
        }

        if (fileTransportContext.getSender() != null) {
            String amopTopic = AMOPChannel.genSubscribeEndianTopic(fileTransportContext.getFileChunksMeta().getTopic(), fileId);

            // wait 10s until receiver ready
            int times = 0;
            while (!fileTransportContext.getSender().isAlready() && times < 10) {
                log.info("idle to wait receiver ready");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("idle wait exception", e);
                }

                times++;
            }

            FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelData);
            fileEvent.setFileChunksMeta(fileTransportContext.getFileChunksMeta());
            fileEvent.setChunkIndex(chunkIndex);
            fileEvent.setChunkData(data);
            fileTransportContext.getSender().sendEvent(amopTopic, fileEvent);
        }
    }

    public byte[] downloadChunk(String fileId, int chunkIndex) throws BrokerException {
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
        FileTransportContext fileTransportContext;
        if (this.fileTransportContexts.containsKey(fileChunksMeta.getFileId())) {
            if (this.fileTransportContexts.get(fileChunksMeta.getFileId()).receiver == null) {
                fileTransportContext = this.fileTransportContexts.get(fileChunksMeta.getFileId());
            } else {
                log.error("already exist file receiving context, fileId: {}", fileChunksMeta.getFileId());
                return;
            }
        } else {
            fileTransportContext = new FileTransportContext();
            fileTransportContext.setFileId(fileChunksMeta.getFileId());
            fileTransportContext.setFileChunksMeta(fileChunksMeta);
            this.fileTransportContexts.put(fileChunksMeta.getFileId(), fileTransportContext);
        }

        log.info("initialize file context for receiving fileId, {}", fileChunksMeta.getFileId());

        String amopTopic = AMOPChannel.genSubscribeEndianTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId());
        String senderTopic = AMOPChannel.genPublishEndianTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId());
        try {
            // open amop channel with receiver topic
            AMOPChannel amopChannel = new AMOPChannel(this, amopTopic, this.initService(fileChunksMeta.getGroupId()));
            fileTransportContext.setReceiver(amopChannel);

            // create local file
            this.diskFiles.createFixedLengthFile(fileChunksMeta.getFileId(), fileChunksMeta.getFileSize());
            this.diskFiles.saveFileMeta(fileChunksMeta);

            // send amop event to sender to begin upload
            amopChannel.sendEvent(senderTopic, new FileEvent(FileEvent.EventType.FileChannelAlready));
        } catch (BrokerException e) {
            log.error("exception in new FileTransportContext", e);

            if (fileTransportContext.getReceiver() != null) {
                fileTransportContext.getReceiver().sendEvent(senderTopic, new FileEvent(FileEvent.EventType.FileChannelException));
            }
        }
    }

    public WeEvent cleanUpReceivedFile(FileChunksMeta fileChunksMeta, WeEvent event) {
        // close receiver amop channel
        if (this.fileTransportContexts.containsKey(fileChunksMeta.getFileId())) {
            FileTransportContext fileTransportContext = this.fileTransportContexts.get(fileChunksMeta.getFileId());
            if (fileTransportContext.getReceiver() != null) {
                log.error("close amop channel for file receiving, fileId: {}", fileChunksMeta.getFileId());
                fileTransportContext.getReceiver().close();
                fileTransportContext.setSender(null);
            }

            // remove FileTransportContext if sender is null
            if (fileTransportContext.getSender() == null) {
                this.fileTransportContexts.remove(fileChunksMeta.getFileId());
            }
        }

        // local file CAN NOT delete, because client will downloadChunk after received this WeEvent

        // set host in WeEvent, then downloadChunk invoke will be routed to this host
        event.getExtensions().put("host", "127.0.0.1");
        return event;
    }

    // Notice: always believe FileChunksMeta in local file
    public FileChunksMeta writeChunkData(FileEvent fileEvent) throws BrokerException {
        return this.diskFiles.writeChunkData(fileEvent.getFileChunksMeta().getFileId(), fileEvent.getChunkIndex(), fileEvent.getChunkData());
    }

    public void flushZKFileChunksMeta(FileChunksMeta fileChunksMeta) {
        try {
            boolean full = this.zkChunksMeta.updateChunks(fileChunksMeta.getFileId(), fileChunksMeta);
            if (full) {
                log.info("all chunk bit is set on, file complete {}", fileChunksMeta.getFileId());
            }
        } catch (BrokerException e) {
            log.error("update FileChunksMeta in zookeeper failed", e);
        }
    }
}
