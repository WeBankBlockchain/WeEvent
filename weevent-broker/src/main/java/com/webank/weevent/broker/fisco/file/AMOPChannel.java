package com.webank.weevent.broker.fisco.file;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Service service;

    // topic <-> ready status, used by sender
    private Map<String, Boolean> topicStatus = new ConcurrentHashMap<>();

    // topic <-> ready status, used by receiver
    private Map<String, Boolean> subTopics = new ConcurrentHashMap<>();

    /**
     * Create a amop channel on service for subscribe topic
     *
     * @param fileTransportService component class
     * @param service initialized service
     */
    public AMOPChannel(FileTransportService fileTransportService, Service service) {
        this.fileTransportService = fileTransportService;
        this.service = service;
    }

    public static String genTopic(String weEventTopic, String fileId) {
        return weEventTopic + "/" + fileId;
    }

    public void subTopic(String topic) {
        if (!this.subTopics.containsKey(topic)) {
            log.info("subscribe topic on channel, {}", topic);

            this.subTopics.put(topic, false);
            this.service.addTopics(this.subTopics.keySet());
            this.service.updateTopicsToNode();
        }
    }

    public void unSubTopic(String topic) {
        if (this.subTopics.containsKey(topic)) {
            log.info("unSubscribe topic on channel, {}", topic);

            this.subTopics.remove(topic);
            this.service.addTopics(this.subTopics.keySet());
            this.service.updateTopicsToNode();
        }
    }

    public boolean checkReceiverAlready(String topic) throws BrokerException {
        if (this.topicStatus.containsKey(topic) &&
                this.topicStatus.get(topic)) {
            return true;
        }

        int times = 0;
        while (times < 5) {
            ChannelResponse rsp = this.sendEvent(topic, new FileEvent(FileEvent.EventType.FileChannelAlready));
            if (rsp.getErrorCode() == 0) {
                log.info("topic is subscribe, go");
                this.topicStatus.put(topic, true);
                return true;
            }

            log.error("topic is not subscribe");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("sleep Interrupted");
            }
            times++;
        }

        this.topicStatus.put(topic, false);
        return false;
    }

    public ChannelResponse sendEvent(String topic, FileEvent fileEvent) throws BrokerException {
        byte[] json = JsonHelper.object2JsonBytes(fileEvent);

        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setToTopic(topic);
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
        if (!this.subTopics.containsKey(push.getTopic())) {
            log.error("unknown topic in amop channel, {} <-> {}", push.getTopic(), this.subTopics.keySet());
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
        reply.setContent("".getBytes());
        return reply;
    }

    private static ChannelResponse toChannelResponse(BrokerException e) {
        ChannelResponse reply = new ChannelResponse();
        reply.setErrorCode(e.getCode());
        reply.setErrorMessage(e.getMessage());
        reply.setContent("".getBytes());
        return reply;
    }

    public static BrokerException toBrokerException(ChannelResponse reply) {
        return new BrokerException(reply.getErrorCode(), reply.getErrorMessage());
    }
}
