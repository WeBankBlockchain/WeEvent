package com.webank.weevent.broker.fisco.file;


import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.core.IConsumer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.FileChunksMeta;
import com.webank.weevent.sdk.JsonHelper;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Event listener for file via WeEvent.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public abstract class FileEventListener implements IConsumer.ConsumerListener, NotifyWeEvent {
    private final FileTransportService fileTransportService;
    private final String topic;
    private final String groupId;

    private List<String> files = new ArrayList<>();

    public FileEventListener(FileTransportService fileTransportService, String topic, String groupId) throws BrokerException {
        this.fileTransportService = fileTransportService;
        this.topic = topic;
        this.groupId = groupId;

        // subscribe topic on AMOP channel
        AMOPChannel channel = this.fileTransportService.getChannel(this.groupId);
        String amopTopic = AMOPChannel.genTopic(this.topic);
        channel.subTopic(amopTopic);
    }

    @Override
    public void onEvent(String subscriptionId, WeEvent event) {
        log.info("received file event on WeEvent, subscriptionId: {} {}", subscriptionId, event);

        if (!event.getExtensions().containsKey(WeEvent.WeEvent_FILE)
                || !event.getExtensions().containsKey(WeEvent.WeEvent_FORMAT)
                || !"json".equals(event.getExtensions().get(WeEvent.WeEvent_FORMAT))) {
            log.error("unknown file event on WeEvent, skip it");
            return;
        }

        FileEvent fileEvent;
        try {
            fileEvent = JsonHelper.json2Object(event.getContent(), FileEvent.class);
        } catch (BrokerException e) {
            log.error("invalid file event content on WeEvent", e);
            return;
        }

        switch (fileEvent.getEventType()) {
            case FileTransportStart:
                log.info("get {}, try to initialize context for receiving file", fileEvent.getEventType());

                this.fileTransportService.prepareReceiveFile(fileEvent.getFileChunksMeta());
                this.files.add(fileEvent.getFileId());
                break;

            case FileTransportEnd:
                log.info("get {}, try to finalize context for receiving file", fileEvent.getEventType());

                FileChunksMeta fileChunksMeta = this.fileTransportService.cleanUpReceivedFile(fileEvent.getFileId());
                if (fileChunksMeta == null) {
                    log.error("clean up file context return null");
                    return;
                }
                this.files.remove(fileEvent.getFileId());

                // set host in WeEvent, then downloadChunk invoke will be routed to this host
                try {
                    if (StringUtils.isEmpty(fileChunksMeta.getHost())) {
                        log.error("FATAL: unknown host in FileChunksMeta");
                    }
                    if (!fileChunksMeta.checkChunkFull()) {
                        log.error("FATAL: FileChunksMeta is not full");
                    }

                    byte[] json = JsonHelper.object2JsonBytes(fileChunksMeta);
                    WeEvent weEvent = new WeEvent(event.getTopic(), json, event.getExtensions());
                    log.info("try to send file received WeEvent to client, {}", weEvent);
                    this.send(subscriptionId, weEvent);
                } catch (BrokerException e) {
                    log.error("send file received WeEvent to client failed", e);
                }
                break;

            default:
                log.error("unknown file event type on WeEvent");
        }
    }

    @Override
    public void onException(Throwable e) {
        log.error("file event on WeEvent onException", e);
    }

    @Override
    public void onClose(String subscriptionId) {
        log.info("subscription: {} closed, try to finalize binding file context one by one", subscriptionId);

        // unSubscribe topic on AMOP channel
        try {
            AMOPChannel channel = this.fileTransportService.getChannel(this.groupId);
            String amopTopic = AMOPChannel.genTopic(this.topic);
            channel.unSubTopic(amopTopic);
        } catch (BrokerException e) {
            log.error("AMOPChannel.unSubTopic failed", e);
        }

        for (String fileId : this.files) {
            this.fileTransportService.cleanUpReceivedFile(fileId);
        }
    }
}
