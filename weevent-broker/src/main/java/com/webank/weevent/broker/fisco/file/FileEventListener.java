package com.webank.weevent.broker.fisco.file;


import com.webank.weevent.broker.plugin.IConsumer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.JsonHelper;
import com.webank.weevent.sdk.WeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Event listener for file.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public abstract class FileEventListener implements IConsumer.ConsumerListener, NotifyWeEvent {
    private final FileTransportService fileTransportService;

    public FileEventListener(FileTransportService fileTransportService) {
        this.fileTransportService = fileTransportService;
    }

    @Override
    public void onEvent(String subscriptionId, WeEvent event) {
        log.info("received file event, subscriptionId: {} {}", subscriptionId, event);

        if (!event.getExtensions().containsKey(WeEvent.WeEvent_FILE)
                || !event.getExtensions().containsKey(WeEvent.WeEvent_FORMAT)
                || !"json".equals(event.getExtensions().get(WeEvent.WeEvent_FORMAT))) {
            log.error("unknown FileEvent, skip it");
            return;
        }

        FileEvent fileEvent;
        try {
            fileEvent = JsonHelper.json2Object(event.getContent(), FileEvent.class);
        } catch (BrokerException e) {
            log.error("invalid file event content", e);
            return;
        }

        switch (fileEvent.getEventType()) {
            case FileTransportStart:
                log.info("try to initialize file context for receiving file");
                this.fileTransportService.prepareReceiveFile(fileEvent.getFileChunksMeta());
                break;

            case FileTransportEnd:
                // TODO close amop channel

                WeEvent notifyWeEvent = this.fileTransportService.genWeEventForReceivedFile(event);
                log.info("try to send file received event to remote, {}", notifyWeEvent);
                this.send(subscriptionId, notifyWeEvent);
                break;

            default:
                log.error("unknown file event type");
        }
    }

    @Override
    public void onException(Throwable e) {
        log.error("file event onException", e);
    }
}
