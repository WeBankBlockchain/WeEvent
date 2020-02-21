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
 * sender and receiver can not be in one process,
 * because one file's sender and receiver MUST access in different block node.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public class AMOPChannel extends ChannelPushCallback {
    private final FileTransportService fileTransportService;
    private final String topic;
    private Service service;
    private boolean sender;
    private volatile boolean already = false;

    /**
     * Create a new amop channel(new connection to block chain) for subscribe topic
     *
     * @param fileTransportService component class
     * @param topic binding amop topic
     * @param service initialized service, have not run
     * @param sender is this used by sender or receiver
     * @throws BrokerException BrokerException
     */
    public AMOPChannel(FileTransportService fileTransportService, String topic, Service service, boolean sender) throws BrokerException {
        try {
            this.fileTransportService = fileTransportService;
            this.topic = topic;
            this.service = service;
            this.sender = sender;

            if (!this.sender) {
                // init amop subscription on this service(tcp connection)
                Set<String> topics = new HashSet<>();
                topics.add(this.topic);
                this.service.setPushCallback(this);
                this.service.setTopics(topics);
                this.already = true;
            }
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

    public static String genTopic(String weEventTopic, String fileId) {
        return weEventTopic + "/" + fileId;
    }

    public boolean checkReceiverAlready() throws BrokerException {
        if (!this.sender) {
            log.error("only call by sender");
            throw new BrokerException(ErrorCode.UNKNOWN_ERROR);
        }

        if (!this.already) {
            ChannelResponse rsp = this.sendEvent(new FileEvent(FileEvent.EventType.FileChannelAlready));
            if (rsp.getErrorCode() == 0) {
                log.info("amop channel is ready, can send chunk data");
                this.already = true;
            } else {
                log.error("amop channel is not ready");
            }
        }

        return this.already;
    }

    public ChannelResponse sendEvent(FileEvent fileEvent) throws BrokerException {
        byte[] json = JsonHelper.object2JsonBytes(fileEvent);

        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setToTopic(this.topic);
        channelRequest.setMessageID(this.service.newSeq());
        channelRequest.setTimeout(5000);
        channelRequest.setContent(json);

        log.info("send amop channel request, topic: {} id: {}", channelRequest.getToTopic(), channelRequest.getMessageID());
        ChannelResponse rsp = this.service.sendChannelMessage2(channelRequest);
        log.info("receive amop channel response, id: {} result: {}-{}", rsp.getMessageID(), rsp.getErrorCode(), rsp.getErrorMessage());
        return rsp;
    }

    // event from sender
    @Override
    public void onPush(ChannelPush push) {
        if (!this.topic.equals(push.getTopic())) {
            log.error("miss match amop topic, {} <-> {}", this.topic, push.getTopic());
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
            case FileChannelAlready:
                log.info("get {}, can send chunk data now", fileEvent.getEventType());

                push.sendResponse(AMOPChannel.toChannelResponse(ErrorCode.SUCCESS));
                break;

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
