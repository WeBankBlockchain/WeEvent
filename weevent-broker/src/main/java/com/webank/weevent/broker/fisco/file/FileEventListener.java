package com.webank.weevent.broker.fisco.file;


import java.util.ArrayList;
import java.util.List;

import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.JsonHelper;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Event listener for file via WeEvent.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public abstract class FileEventListener implements IConsumer.ConsumerListener, NotifyWeEvent {
    private final FileTransportService fileTransportService;
    private List<String> files;

    public FileEventListener(FileTransportService fileTransportService) {
        this.fileTransportService = fileTransportService;
        this.files = new ArrayList<>();
    }

    @Override
    public void onEvent(String subscriptionId, WeEvent event) {
        log.info("received file event via WeEvent, subscriptionId: {} {}", subscriptionId, event);

        if (!event.getExtensions().containsKey(WeEvent.WeEvent_FILE)
                || !event.getExtensions().containsKey(WeEvent.WeEvent_FORMAT)
                || !"json".equals(event.getExtensions().get(WeEvent.WeEvent_FORMAT))) {
            log.error("unknown file event via WeEvent, skip it");
            return;
        }

        FileEvent fileEvent;
        try {
            fileEvent = JsonHelper.json2Object(event.getContent(), FileEvent.class);
        } catch (BrokerException e) {
            log.error("invalid file event content via WeEvent", e);
            return;
        }

        switch (fileEvent.getEventType()) {
            case FileTransportStart:
                log.info("get {}, try to initialize context for receiving file", fileEvent.getEventType());

                // send amop event to sender to begin upload
                this.fileTransportService.prepareReceiveFile(fileEvent.getFileChunksMeta());
                this.files.add(fileEvent.getFileChunksMeta().getFileId());
                break;

            case FileTransportEnd:
                log.info("get {}, try to finalize context for receiving file", fileEvent.getEventType());

                this.fileTransportService.cleanUpReceivedFile(fileEvent.getFileChunksMeta().getFileId());
                this.files.remove(fileEvent.getFileChunksMeta().getFileId());

                // set host in WeEvent, then downloadChunk invoke will be routed to this host
                event.getExtensions().put("host", "127.0.0.1");
                log.info("try to send file received event to remote, {}", event);
                this.send(subscriptionId, event);
                break;

            default:
                log.error("unknown file event type via WeEvent");
        }
    }

    @Override
    public void onException(Throwable e) {
        log.error("file event via WeEvent onException", e);
    }

    @Override
    public void onClose(String subscriptionId) {
        log.info("subscription: {} closed, try to finalize binding file context one by one", subscriptionId);
        for (String fileId : this.files) {
            this.fileTransportService.cleanUpReceivedFile(fileId);
        }
    }
}
