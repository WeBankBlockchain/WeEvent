package com.webank.weevent.broker.fisco.file;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.FileChunksMeta;
import com.webank.weevent.sdk.JsonHelper;

import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.channel.client.ChannelPushCallback;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.dto.ChannelPush;
import org.fisco.bcos.channel.dto.ChannelRequest;

/**
 * AMOP channel for file transport.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public class AMOPChannel extends ChannelPushCallback {
    private final static String publishEndian = "pubEndian";
    private final static String subscribeEndian = "subEndian";

    private final String subTopic;
    private Service service;
    private volatile boolean already = false;

    public static String genPublishEndianTopic(String weEventTopic, String fileId) {
        return weEventTopic + "/" + fileId + "/" + publishEndian;
    }

    public static String genSubscribeEndianTopic(String weEventTopic, String fileId) {
        return weEventTopic + "/" + fileId + "/" + subscribeEndian;
    }

    /**
     * Create a new amop channel(new connection to block chain) for subscribe topic
     *
     * @param topic binding amop topic
     * @param service initialized service, have not run
     * @throws BrokerException BrokerException
     */
    public AMOPChannel(String topic, Service service) throws BrokerException {
        try {
            this.subTopic = topic;
            this.service = service;
            Set<String> topics = new HashSet<>();
            topics.add(this.subTopic);
            this.service.setTopics(topics);
            this.service.run();
        } catch (Exception e) {
            log.error("exception in init amop channel", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
    }

    public void close() {
        this.service = null;
    }

    // can be called after received FileEvent.EventType.FIleChannelAlready
    public void sendEvent(String topic, FileEvent fileEvent) {
        byte[] json;
        try {
            json = JsonHelper.object2JsonBytes(fileEvent);
        } catch (BrokerException e) {
            log.error("encode json failed, skip it", e);
            return;
        }

        // wait 10s until receiver ready
        int times = 0;
        while (!this.already && times < 10) {
            log.info("idle to wait receiver ready");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("idle wait exception", e);
            }

            times++;
        }

        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setToTopic(topic);
        channelRequest.setMessageID(this.service.newSeq());
        channelRequest.setTimeout(5000);
        channelRequest.setContent(json);
        log.info("send amop channel message, topic: {} id: {}", channelRequest.getToTopic(), channelRequest.getMessageID());
        this.service.sendChannelMessage2(channelRequest);
    }

    @Override
    public void onPush(ChannelPush push) {
        if (!this.subTopic.equals(push.getTopic())) {
            log.error("miss match topic, {} <-> {}", this.subTopic, push.getTopic());
            return;
        }

        FileEvent fileEvent;
        try {
            fileEvent = JsonHelper.json2Object(push.getContent2(), FileEvent.class);
        } catch (BrokerException e) {
            log.error("invalid FileEvent", e);
            return;
        }
        log.info("received event, {}", fileEvent);

        FileChunksMeta fileChunksMeta = fileEvent.getFileChunksMeta();
        switch (fileEvent.getEventType()) {
            // event from receiver
            case FileChannelAlready:
                this.already = true;
                break;

            case FileChannelStatus:
                // update status to zookeeper
                break;

            case FileChannelException:
                //
                break;

            // event from sender
            case FileChannelData:
                try {
                    // write file into cache
                    FileOutputStream fileOutputStream = new FileOutputStream("./fileCache/" + fileChunksMeta.getFileId());
                    fileOutputStream.write(fileEvent.getChunkData(), fileEvent.getChunkIndex() * fileChunksMeta.getChunkSize(), fileEvent.getChunkData().length);
                    fileOutputStream.flush();

                    // send local file status to sender
                    String amopTopic = genPublishEndianTopic(fileChunksMeta.getTopic(), fileChunksMeta.getFileId());
                    this.sendEvent(amopTopic, new FileEvent(FileEvent.EventType.FileChannelStatus));
                } catch (FileNotFoundException e) {
                    log.error("not exist file", e);
                } catch (IOException e) {
                    log.error("write file failed", e);
                }
                break;

            default:
                log.error("unknown file event type");
        }
    }
}
