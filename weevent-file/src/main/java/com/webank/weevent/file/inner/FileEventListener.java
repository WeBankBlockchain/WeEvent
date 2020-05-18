package com.webank.weevent.file.inner;


import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.client.WeEvent;
import com.webank.weevent.file.IWeEventFileClient;
import com.webank.weevent.file.dto.FileEvent;
import com.webank.weevent.file.service.FileChunksMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Event listener for file via WeEvent.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public class FileEventListener {
    private final FileTransportService fileTransportService;
    private final String topic;
    private final String groupId;
    private final IWeEventFileClient.FileListener fileListener;

    public FileEventListener(FileTransportService fileTransportService, String topic, String groupId, IWeEventFileClient.FileListener fileListener) throws BrokerException {
        this.fileTransportService = fileTransportService;
        this.topic = topic;
        this.groupId = groupId;
        this.fileListener = fileListener;

        // subscribe topic on AMOP channel
        AMOPChannel channel = this.fileTransportService.getChannel(this.groupId);
        channel.subTopic(this.topic, this.groupId, fileListener);
    }

    public static boolean isFileEvent(WeEvent event) {
        return event.getExtensions().containsKey(WeEvent.WeEvent_FILE)
                && event.getExtensions().containsKey(WeEvent.WeEvent_FORMAT)
                && "json".equals(event.getExtensions().get(WeEvent.WeEvent_FORMAT));
    }

    public void onEvent(String subscriptionId, WeEvent event) {
        log.info("received file event on WeEvent, subscriptionId: {} {}", subscriptionId, event);

        if (!isFileEvent(event)) {
            log.error("unknown FileEvent in WeEvent, send original");
            this.send(subscriptionId, event);
            return;
        }

        FileEvent fileEvent;
        try {
            fileEvent = JsonHelper.json2Object(event.getContent(), FileEvent.class);
        } catch (BrokerException e) {
            log.error("invalid FileEvent in WeEvent's content, send original", e);
            this.send(subscriptionId, event);
            return;
        }

        if (FileEvent.EventType.FileTransport == fileEvent.getEventType()) {
            log.info("get {}", fileEvent.getEventType());

            // set host in WeEvent, then downloadChunk invoke will be routed to this host
            try {
                FileChunksMeta fileChunksMeta = this.fileTransportService.loadFileChunksMeta(fileEvent.getFileId());
                if (StringUtils.isEmpty(fileChunksMeta.getHost())) {
                    log.error("FATAL: unknown host in FileChunksMeta");
                }
                if (!fileChunksMeta.checkChunkFull()) {
                    log.error("FATAL: FileChunksMeta is not full");
                }
            } catch (BrokerException e) {
                log.error("change received WeEvent failed, send original", e);
                this.send(subscriptionId, event);
            }
        }
    }

    public void onException(Throwable e) {
        log.error("file event on WeEvent onException", e);
    }

    public void onClose(String subscriptionId) {
        log.info("subscription: {} closed, try to unsub topic on AMOP", subscriptionId);

        // unSubscribe topic on AMOP channel
        try {
            AMOPChannel channel = this.fileTransportService.getChannel(this.groupId);
            channel.unSubTopic(this.topic);
        } catch (BrokerException e) {
            log.error("AMOPChannel.unSubTopic failed", e);
        }
    }

    public void send(String subscriptionId, WeEvent event) {

    }
}
