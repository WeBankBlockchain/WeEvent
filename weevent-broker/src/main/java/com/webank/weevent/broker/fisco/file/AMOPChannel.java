package com.webank.weevent.broker.fisco.file;


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
import org.fisco.bcos.channel.dto.ChannelResponse;

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

    private final FileTransportService fileTransportService;
    private final String subTopic;
    private Service service;
    private volatile boolean already = false;

    public static String genPublishEndianTopic(String weEventTopic, String fileId) {
        return weEventTopic + "/" + fileId + "/" + publishEndian;
    }

    public static String genSubscribeEndianTopic(String weEventTopic, String fileId) {
        return weEventTopic + "/" + fileId + "/" + subscribeEndian;
    }

    public boolean isAlready() {
        return this.already;
    }

    /**
     * Create a new amop channel(new connection to block chain) for subscribe topic
     *
     * @param fileTransportService component class
     * @param topic binding amop topic
     * @param service initialized service, have not run
     * @throws BrokerException BrokerException
     */
    public AMOPChannel(FileTransportService fileTransportService, String topic, Service service) throws BrokerException {
        try {
            this.fileTransportService = fileTransportService;
            this.subTopic = topic;
            this.service = service;

            // init amop subscription on this service(tcp connection)
            Set<String> topics = new HashSet<>();
            topics.add(this.subTopic);
            this.service.setPushCallback(this);
            this.service.setTopics(topics);
            this.service.run();
        } catch (Exception e) {
            log.error("exception in init amop channel", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
    }

    public void close() {
        // TODO need unSubscribe and then close binding service
        this.service = null;
    }

    public ChannelResponse sendEvent(String topic, FileEvent fileEvent) throws BrokerException {
        byte[] json = JsonHelper.object2JsonBytes(fileEvent);

        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setToTopic(topic);
        channelRequest.setMessageID(this.service.newSeq());
        channelRequest.setTimeout(5000);
        channelRequest.setContent(json);

        log.info("send amop channel message, topic: {} id: {}", channelRequest.getToTopic(), channelRequest.getMessageID());
        return this.service.sendChannelMessage2(channelRequest);
    }

    @Override
    public void onPush(ChannelPush push) {
        if (!this.subTopic.equals(push.getTopic())) {
            log.error("miss match amop topic, {} <-> {}", this.subTopic, push.getTopic());
            push.sendResponse(AMOPChannel.toChannelResponse(ErrorCode.UNKNOWN_ERROR));
            return;
        }

        FileEvent fileEvent;
        try {
            fileEvent = JsonHelper.json2Object(push.getContent2(), FileEvent.class);
        } catch (BrokerException e) {
            log.error("invalid file event via amop", e);
            push.sendResponse(AMOPChannel.toChannelResponse(e));
            return;
        }

        log.info("received file event via amop, {}", fileEvent);
        switch (fileEvent.getEventType()) {
            // event from receiver
            case FileChannelAlready:
                log.info("get {}, can send chunk data now", fileEvent.getEventType());

                this.already = true;
                push.sendResponse(AMOPChannel.toChannelResponse(ErrorCode.SUCCESS));
                break;

            case FileChannelException:
                log.error("get {}, Warning: remote exception in received file", fileEvent.getEventType());
                break;

            // event from sender
            case FileChannelData:
                log.info("get {}, try to write chunk data in local file", fileEvent.getEventType());

                try {
                    FileChunksMeta updatedFileChunksMeta = this.fileTransportService.writeChunkData(fileEvent);
                    // send local file status to sender
                    ChannelResponse rsp = AMOPChannel.toChannelResponse(ErrorCode.SUCCESS);
                    byte[] json = JsonHelper.object2JsonBytes(updatedFileChunksMeta);
                    rsp.setContent(json);
                    push.sendResponse(rsp);
                } catch (BrokerException e) {
                    log.error("write chunk data in local file exception", e);
                    push.sendResponse(AMOPChannel.toChannelResponse(e));
                }
                break;

            default:
                log.error("unknown file event type via amop");
        }
    }

    private static ChannelResponse toChannelResponse(ErrorCode errorCode) {
        ChannelResponse reply = new ChannelResponse();
        reply.setErrorCode(errorCode.getCode());
        reply.setErrorMessage(errorCode.getCodeDesc());
        return reply;
    }

    private static ChannelResponse toChannelResponse(BrokerException e) {
        ChannelResponse reply = new ChannelResponse();
        reply.setErrorCode(e.getCode());
        reply.setErrorMessage(e.getMessage());
        return reply;
    }

    public static BrokerException toBrokerException(ChannelResponse reply) {
        return new BrokerException(reply.getErrorCode(), reply.getErrorMessage());
    }
}
