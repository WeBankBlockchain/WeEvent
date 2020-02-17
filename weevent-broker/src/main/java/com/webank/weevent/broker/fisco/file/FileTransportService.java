package com.webank.weevent.broker.fisco.file;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.broker.fisco.web3sdk.FiscoBcosDelegate;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.FileChunksMeta;
import com.webank.weevent.sdk.JsonHelper;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * File transport service base on AMOP.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public class FileTransportService {
    private IProducer producer;

    // fileid -> FileChunksMeta
    private Map<String, FileChunksMeta> files = new HashMap<>();
    // fileid -> amop channel
    private Map<String, AMOPChannel> channels = new HashMap<>();

    public void setProducer(IProducer iProducer) {
        this.producer = iProducer;
    }

    // CGI interface

    public void openChannel(FileChunksMeta fileChunksMeta) throws BrokerException {
        String amopTopic = AMOPChannel.genPublishEndianTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId());
        if (this.channels.containsKey(amopTopic)) {
            log.info("already exist");
            return;
        }

        // listen amop sender topic
        AMOPChannel amopChannel = new AMOPChannel();
        amopChannel.subscribeSender(amopTopic);
        this.channels.put(fileChunksMeta.getFileId(), amopChannel);

        // send WeEvent to start
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileTransportStart);
        fileEvent.setFileChunksMeta(fileChunksMeta);
        WeEvent startTransport = new WeEvent(fileChunksMeta.getTopic(), JsonHelper.object2JsonBytes(fileEvent));

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

    public void closeChannel(String fileId) {
        if (!this.channels.containsKey(fileId)) {
            log.error("not exist channel, fileId: {}", fileId);
            return;
        }
        AMOPChannel amopChannel = this.channels.get(fileId);

        // close amop sender topic
        amopChannel.unSubscribe(amopChannel.getAmopSenderTopic());

        // send amop event to close
        amopChannel.sendEvent(amopChannel.getAmopReceivedTopic(),
                new FileEvent(FileEvent.EventType.FIleChannelEnd));
    }

    public void sendChunkData(String fileId, int chunkIndex, byte[] data) throws BrokerException {
        if (!this.channels.containsKey(fileId)) {
            log.error("not exist channel, fileId: {}", fileId);
            return;
        }
        AMOPChannel amopChannel = this.channels.get(fileId);

        amopChannel.sendEvent(amopChannel.getAmopSenderTopic(),
                new FileEvent(FileEvent.EventType.FIleChannelData));
    }

    public byte[] downloadChunk(String fileId, int chunkIndex) {
        return null;
    }

    // inner interface

    public void prepareReceiveFile(FileChunksMeta fileChunksMeta) {
        // create file buff

        // listen amop receiver topic
        AMOPChannel amopChannel;
        String amopTopic = AMOPChannel.genSubscribeEndianTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId());
        if (this.channels.containsKey(fileChunksMeta.getFileId())) {
            amopChannel = this.channels.get(fileChunksMeta.getFileId());
        } else {
            amopChannel = new AMOPChannel();
        }
        amopChannel.subscribeReceiver(amopTopic);

        // send amop event to start
        amopChannel.sendEvent(amopChannel.getAmopSenderTopic(),
                new FileEvent(FileEvent.EventType.FIleChannelAlready));
    }

    public WeEvent genWeEventForReceivedFile(FileChunksMeta fileChunksMeta) {
        // publish file event
        Map<String, String> extensions = new HashMap<>();
        extensions.put(WeEvent.WeEvent_FILE, "1");
        return new WeEvent(fileChunksMeta.getTopic(), "".getBytes(), extensions);
    }
}
