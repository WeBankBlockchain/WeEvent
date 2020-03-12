package com.webank.weevent.broker.fisco.file;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.FileChunksMeta;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.core.fisco.web3sdk.v2.Web3SDKConnector;

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
    private final int timeout;
    private final String host;
    private final int fileChunkSize;
    private final DiskFiles diskFiles;

    // following ONLY used in sender side
    // groupId <-> AMOPChannel
    private Map<String, AMOPChannel> groupChannels = new ConcurrentHashMap<>();
    // fileId <-> FileChunksMeta
    private Map<String, FileChunksMeta> fileTransportContexts = new ConcurrentHashMap<>();

    public FileTransportService(FiscoConfig fiscoConfig,
                                IProducer iProducer,
                                String host,
                                String filePath,
                                int fileChunkSize) throws BrokerException {
        this.fiscoConfig = fiscoConfig;
        this.producer = iProducer;
        this.timeout = fiscoConfig.getWeb3sdkTimeout();

        log.info("host: {}, file path: {}, chunk size: {}", host, filePath, fileChunkSize);
        if (fileChunkSize <= 0 || fileChunkSize > 2 * 1024 * 1024) {
            log.error("invalid file chunk size");
            throw new BrokerException(ErrorCode.FILE_INVALID_FILE_CHUNK_SIZE);
        }
        this.host = host;
        this.fileChunkSize = fileChunkSize;
        this.diskFiles = new DiskFiles(filePath);

        // init default group
        String defaultGroupId = WeEvent.DEFAULT_GROUP_ID;
        Service service = Web3SDKConnector.initService(Long.valueOf(defaultGroupId), this.fiscoConfig);
        AMOPChannel channel = new AMOPChannel(this, service);
        this.groupChannels.put(defaultGroupId, channel);
    }

    public FileTransportStats stats() {
        FileTransportStats fileTransportStats = new FileTransportStats();

        for (Map.Entry<String, AMOPChannel> groupEntry : groupChannels.entrySet()) {
            String groupId = groupEntry.getKey();
            AMOPChannel amopChannel = groupEntry.getValue();

            // sender
            Map<String, List<FileChunksMetaPlus>> senders = new HashMap<>();
            for (String topic : amopChannel.getSenderTopics().keySet()) {
                List<FileChunksMetaPlus> filePlus = fileTransportContexts.values().stream()
                        .filter(item -> item.getTopic().equals(topic))
                        .map(FileChunksMetaPlus::new)
                        .collect(Collectors.toList());
                senders.put(topic, filePlus);
            }
            fileTransportStats.getSender().put(groupId, senders);

            // receiver
            List<FileChunksMeta> localFiles = this.diskFiles.listNotCompleteFiles();
            Map<String, List<FileChunksMetaPlus>> receivers = new HashMap<>();
            for (String topic : amopChannel.getSubTopics().keySet()) {
                List<FileChunksMetaPlus> filePlus = localFiles.stream()
                        .filter(item -> item.getTopic().equals(topic))
                        .map(FileChunksMetaPlus::new)
                        .collect(Collectors.toList());
                receivers.put(topic, filePlus);
            }
            fileTransportStats.getReceiver().put(groupId, receivers);
        }

        return fileTransportStats;
    }

    public AMOPChannel getChannel(String groupId) throws BrokerException {
        if (this.groupChannels.containsKey(groupId)) {
            return this.groupChannels.get(groupId);
        }

        // init if not exist
        log.info("AMOP channel is not exist, init for groupId: {}", groupId);
        Service service = Web3SDKConnector.initService(Long.valueOf(groupId), this.fiscoConfig);
        AMOPChannel channel = new AMOPChannel(this, service);
        this.groupChannels.put(groupId, channel);
        return channel;
    }

    // CGI interface

    // called by sender cgi
    public FileChunksMeta openChannel(FileChunksMeta fileChunksMeta) throws BrokerException {
        if (this.fileTransportContexts.containsKey(fileChunksMeta.getFileId())) {
            log.error("already exist file context, fileId: {}", fileChunksMeta.getFileId());
            throw new BrokerException(ErrorCode.FILE_EXIST_CONTEXT);
        }

        // sender side DO NOT need chunk size, skip to minimize memory use
        // fileChunksMeta.setChunkSize(this.fileChunkSize);
        // sender side can fail over automatic
        // fileChunksMeta.setHost(this.host);

        // create chunk meta in receiver
        AMOPChannel channel = this.getChannel(fileChunksMeta.getGroupId());
        FileChunksMeta remoteFileChunksMeta = channel.createReceiverFileContext(fileChunksMeta);
        // it's not used in sender side
        remoteFileChunksMeta.setHost("");

        // cache chunk meta
        this.fileTransportContexts.put(fileChunksMeta.getFileId(), remoteFileChunksMeta);

        return remoteFileChunksMeta;
    }

    // sender side can fail over automatic
    private FileChunksMeta getSenderFileChunksMeta(String topic, String groupId, String fileId) throws BrokerException {
        if (this.fileTransportContexts.containsKey(fileId)) {
            log.info("get file context in memory");
            return this.fileTransportContexts.get(fileId);
        } else {
            log.info("get file context from remote");
            FileChunksMeta fileChunksMeta = this.getReceiverFileChunksMeta(topic, groupId, fileId);
            this.fileTransportContexts.put(fileId, fileChunksMeta);
            return fileChunksMeta;
        }
    }

    // get remote chunk meta from receiver
    public FileChunksMeta getReceiverFileChunksMeta(String topic, String groupId, String fileId) throws BrokerException {
        // get remote chunk meta from receiver
        AMOPChannel channel = this.getChannel(groupId);
        FileChunksMeta remoteFileChunksMeta = channel.getReceiverFileContext(topic, fileId);
        if (remoteFileChunksMeta == null) {
            log.error("not exist receive file context");
            throw new BrokerException(ErrorCode.FILE_RECEIVE_CONTEXT_NOT_READY);
        }

        // it's not used in sender side
        remoteFileChunksMeta.setHost("");
        return remoteFileChunksMeta;
    }

    public SendResult closeChannel(String topic, String groupId, String fileId) throws BrokerException {
        log.info("close AMOP sender channel for file, fileId: {}", fileId);
        this.fileTransportContexts.remove(fileId);

        // clean up receiver context
        AMOPChannel channel = this.getChannel(groupId);
        FileChunksMeta fileChunksMeta = channel.cleanUpReceiverFileContext(topic, fileId);

        // send sign to WeEvent
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileTransport, fileId);
        // chunk detail is no need at all
        fileChunksMeta.cleanChunkStatus();
        fileEvent.setFileChunksMeta(fileChunksMeta);
        Map<String, String> extensions = new HashMap<>();
        extensions.put(WeEvent.WeEvent_FILE, "1");
        extensions.put(WeEvent.WeEvent_FORMAT, "json");
        byte[] json = JsonHelper.object2JsonBytes(fileEvent);
        WeEvent weEvent = new WeEvent(fileChunksMeta.getTopic(), json, extensions);
        return this.producer.publish(weEvent, fileChunksMeta.getGroupId(), this.timeout);
    }

    public void sendChunkData(String topic, String groupId, String fileId, int chunkIndex, byte[] data) throws BrokerException {
        // check file context exist
        FileChunksMeta fileChunksMeta = this.getSenderFileChunksMeta(topic, groupId, fileId);

        log.info("send chunk data via AMOP channel, {}#{}", fileId, chunkIndex);

        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelData, fileId);
        fileEvent.setChunkIndex(chunkIndex);
        fileEvent.setChunkData(data);

        AMOPChannel channel = this.getChannel(groupId);
        ChannelResponse rsp = channel.sendEvent(topic, fileEvent);
        if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            log.info("sender chunk data to remote success");
            // local cached chunkStatus is not consistency, but show in stats and log only
            fileChunksMeta.getChunkStatus().set(chunkIndex);
        } else {
            BrokerException e = AMOPChannel.toBrokerException(rsp);
            log.error("sender chunk data to remote failed", e);
            throw e;
        }
    }

    // Notice: receiver always believe FileChunksMeta in local file

    public byte[] downloadChunk(String fileId, int chunkIndex) throws BrokerException {
        log.info("download chunk data, {}#{}", fileId, chunkIndex);
        return this.diskFiles.readChunkData(fileId, chunkIndex);
    }

    public FileChunksMeta prepareReceiveFile(FileChunksMeta fileChunksMeta) throws BrokerException {
        String fileId = fileChunksMeta.getFileId();
        log.info("initialize file context for receiving, fileId: {}", fileId);

        // create local file
        this.diskFiles.createFixedLengthFile(fileId, fileChunksMeta.getFileSize());
        // initialize chunk size
        fileChunksMeta.initChunkSize(this.fileChunkSize);
        // set local host
        fileChunksMeta.setHost(this.host);
        this.diskFiles.saveFileMeta(fileChunksMeta);
        return fileChunksMeta;
    }

    public FileChunksMeta loadFileChunksMeta(String fileId) throws BrokerException {
        return this.diskFiles.loadFileMeta(fileId);
    }

    public FileChunksMeta cleanUpReceivedFile(String fileId) throws BrokerException {
        log.info("finalize file context for receiving, fileId: {}", fileId);

        // local file CAN NOT delete, because client will downloadChunk after received this WeEvent
        FileChunksMeta fileChunksMeta = this.diskFiles.loadFileMeta(fileId);
        if (!fileChunksMeta.checkChunkFull()) {
            log.info("try to delete file not complete, {}", fileId);
            // delete not complete file in local disk
            this.diskFiles.cleanUp(fileId);
        }

        return fileChunksMeta;
    }

    public void writeChunkData(FileEvent fileEvent) throws BrokerException {
        this.diskFiles.writeChunkData(fileEvent.getFileId(), fileEvent.getChunkIndex(), fileEvent.getChunkData());
    }
}
