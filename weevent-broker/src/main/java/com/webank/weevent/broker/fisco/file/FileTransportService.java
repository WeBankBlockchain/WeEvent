package com.webank.weevent.broker.fisco.file;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
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

    private IProducer producer;

    // fileId -> (FileChunksMeta, sender AMOPChannel, receiver AMOPChannel)
    private Map<String, FileTransportContext> fileTransportContexts = new HashMap<>();

    public void setProducer(IProducer iProducer) {
        this.producer = iProducer;
    }

    private Service initService(String groupId) {
        return new Service();
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
        AMOPChannel amopChannel = new AMOPChannel(amopTopic, this.initService(fileChunksMeta.getGroupId()));
        fileTransportContext.setSender(amopChannel);

        // send WeEvent to start
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileTransportStart);
        fileEvent.setFileChunksMeta(fileChunksMeta);
        Map<String, String> extensions = new HashMap<>();
        extensions.put(WeEvent.WeEvent_FILE, "1");
        extensions.put(WeEvent.WeEvent_FORMAT, "json");
        WeEvent startTransport = new WeEvent(fileChunksMeta.getTopic(), JsonHelper.object2JsonBytes(fileEvent), extensions);

        SendResult sendResult = new SendResult(SendResult.SendResultStatus.ERROR);
        try {
            sendResult = this.producer.publish(startTransport, fileChunksMeta.getGroupId()).get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            log.error("publishWeEvent failed due to transaction execution error.", e);
            sendResult.setStatus(SendResult.SendResultStatus.ERROR);
        } catch (TimeoutException e) {
            log.error("publishWeEvent failed due to transaction execution timeout.", e);
            sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
        }

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

        if (fileTransportContext.getSender() != null) {
            fileTransportContext.getSender().close();

            FileChunksMeta fileChunksMeta = fileTransportContext.getFileChunksMeta();
            // send WeEvent to close
            FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileTransportEnd);
            fileEvent.setFileChunksMeta(fileChunksMeta);

            Map<String, String> extensions = new HashMap<>();
            extensions.put(WeEvent.WeEvent_FILE, "1");
            extensions.put(WeEvent.WeEvent_FORMAT, "json");
            byte[] json = JsonHelper.object2JsonBytes(fileEvent);
            WeEvent weEvent = new WeEvent(fileTransportContext.getFileChunksMeta().getTopic(), json, extensions);

            try {
                sendResult = this.producer.publish(weEvent, fileChunksMeta.getGroupId()).get(FiscoBcosDelegate.timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException e) {
                log.error("publishWeEvent failed due to transaction execution error.", e);
                sendResult.setFileChunksMeta(fileChunksMeta);
                sendResult.setStatus(SendResult.SendResultStatus.ERROR);
            } catch (TimeoutException e) {
                log.error("publishWeEvent failed due to transaction execution timeout.", e);
                sendResult.setFileChunksMeta(fileChunksMeta);
                sendResult.setStatus(SendResult.SendResultStatus.TIMEOUT);
            }
        }

        if (fileTransportContext.getReceiver() != null) {
            fileTransportContext.getReceiver().close();
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

            FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelData);
            fileEvent.setFileChunksMeta(fileTransportContext.getFileChunksMeta());
            fileEvent.setChunkIndex(chunkIndex);
            fileEvent.setChunkData(data);
            fileTransportContext.getSender().sendEvent(amopTopic, fileEvent);
        }
    }

    public byte[] downloadChunk(String fileId, int chunkIndex) throws BrokerException {
        if (!this.fileTransportContexts.containsKey(fileId)) {
            log.error("not exist file context, fileId: {}", fileId);
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST_CONTEXT);
        }

        FileTransportContext fileTransportContext = this.fileTransportContexts.get(fileId);
        FileChunksMeta fileChunksMeta = fileTransportContext.getFileChunksMeta();
        if (chunkIndex >= fileChunksMeta.getChunkNum()) {
            log.error("invalid chunk data, skip");
            throw new BrokerException(ErrorCode.FILE_INVALID_CHUNK);
        }

        // read data from local file
        try {
            FileInputStream fileInputStream = new FileInputStream("./fileCache/" + fileChunksMeta.getFileId());
            int size = fileChunksMeta.getChunkSize();
            if (chunkIndex == fileChunksMeta.getChunkNum() - 1) {
                size = (int) (fileChunksMeta.getFileSize() % fileChunksMeta.getChunkSize());
            }
            byte[] data = new byte[size];
            fileInputStream.read(data, chunkIndex * fileChunksMeta.getChunkSize(), fileChunksMeta.getChunkSize());
            return data;
        } catch (FileNotFoundException e) {
            log.error("not exist file, skip");
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST);
        } catch (IOException e) {
            log.error("read data from file exception", e);
            throw new BrokerException(ErrorCode.FILE_READ_EXCEPTION);
        }
    }

    // inner interface

    // callback by FileEvent.EventType.FileTransportStart
    public void prepareReceiveFile(FileChunksMeta fileChunksMeta) {
        log.info("init file context, fileId: {}", fileChunksMeta.getFileId());

        FileTransportContext fileTransportContext;
        if (this.fileTransportContexts.containsKey(fileChunksMeta.getFileId())) {
            if (this.fileTransportContexts.get(fileChunksMeta.getFileId()).receiver == null) {
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

        String amopTopic = AMOPChannel.genSubscribeEndianTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId());
        AMOPChannel amopChannel = null;
        try {
            // listen amop receiver topic
            amopChannel = new AMOPChannel(amopTopic, this.initService(fileChunksMeta.getGroupId()));
            fileTransportContext.setReceiver(amopChannel);

            // create local file buff
            File file = new File("./fileCache/" + fileChunksMeta.getFileId());
            file.createNewFile();
        } catch (IOException | BrokerException e) {
            log.error("exception in new FileTransportContext", e);
            if (amopChannel != null) {
                amopChannel.sendEvent(AMOPChannel.genPublishEndianTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId()),
                        new FileEvent(FileEvent.EventType.FileChannelException));
            }
            return;
        }

        // send amop event to begin upload
        amopChannel.sendEvent(AMOPChannel.genPublishEndianTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId()),
                new FileEvent(FileEvent.EventType.FileChannelAlready));
    }

    public WeEvent genWeEventForReceivedFile(WeEvent event) {
        // publish file event
        event.getExtensions().put("host", "127.0.0.1");
        return event;
    }
}
