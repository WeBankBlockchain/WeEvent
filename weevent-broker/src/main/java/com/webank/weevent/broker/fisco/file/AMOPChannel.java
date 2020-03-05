package com.webank.weevent.broker.fisco.file;


import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.FileChunksMeta;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.core.fisco.util.DataTypeUtils;

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
 * throws ErrorCode.FILE_SENDER_RECEIVER_CONFLICT if found
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Slf4j
public class AMOPChannel extends ChannelPushCallback {
    private final FileTransportService fileTransportService;
    private final Service service;

    // topic <-> ready status, used by sender
    private Map<String, Boolean> senderTopics = new ConcurrentHashMap<>();
    // topic <-> ready status, used by receiver
    private Map<String, Boolean> subTopics = new ConcurrentHashMap<>();

    /**
     * Create a AMOP channel on service for subscribe topic
     *
     * @param fileTransportService component class
     * @param service initialized service
     */
    public AMOPChannel(FileTransportService fileTransportService, Service service) {
        this.fileTransportService = fileTransportService;
        this.service = service;

        this.service.setPushCallback(this);
    }

    public static String genTopic(String weEventTopic) {
        // the total length of all topic on AMOP channel is limited
        return DataTypeUtils.genTopicNameHash(weEventTopic);
    }

    public void subTopic(String topic) throws BrokerException {
        if (this.senderTopics.containsKey(topic)) {
            log.error("this is already sender side for topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        if (!this.subTopics.containsKey(topic)) {
            log.info("subscribe topic on AMOP channel, {}", topic);

            this.subTopics.put(topic, true);
            this.service.setTopics(new HashSet<>(this.subTopics.keySet()));
            this.service.updateTopicsToNode();
        }
    }

    public void unSubTopic(String topic) {
        if (this.subTopics.containsKey(topic)) {
            log.info("unSubscribe topic on AMOP channel, {}", topic);

            this.subTopics.remove(topic);
            this.service.setTopics(new HashSet<>(this.subTopics.keySet()));
            this.service.updateTopicsToNode();
        }
    }

    public FileChunksMeta createReceiverFileContext(FileChunksMeta fileChunksMeta) throws BrokerException {
        if (this.subTopics.containsKey(fileChunksMeta.getTopic())) {
            log.error("this side is already for receiving, topic: {}", fileChunksMeta.getTopic());
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        log.info("send AMOP message to create receiver file context");
        String amopTopic = AMOPChannel.genTopic(fileChunksMeta.getTopic());
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelStart, fileChunksMeta.getFileId());
        fileEvent.setFileChunksMeta(fileChunksMeta);
        ChannelResponse rsp = this.sendEvent(amopTopic, fileEvent);
        if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            log.info("create remote file context success");
            FileChunksMeta remoteFileChunksMeta = JsonHelper.json2Object(rsp.getContentByteArray(), FileChunksMeta.class);
            this.senderTopics.put(fileChunksMeta.getTopic(), true);
            return remoteFileChunksMeta;
        }

        log.error("create remote file context failed");
        throw toBrokerException(rsp);
    }

    public void cleanUpReceiverFileContext(FileChunksMeta fileChunksMeta) throws BrokerException {
        log.info("send AMOP message to clean up receiver file context");

        String amopTopic = AMOPChannel.genTopic(fileChunksMeta.getTopic());
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelEnd, fileChunksMeta.getFileId());
        ChannelResponse rsp = this.sendEvent(amopTopic, fileEvent);
        if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            log.info("clean up receiver file context success");
        } else {
            log.error("clean up remote file context failed");
            throw toBrokerException(rsp);
        }
    }

    public FileChunksMeta getReceiverFileContext(String topic, String fileId) throws BrokerException {
        if (this.subTopics.containsKey(topic)) {
            log.error("this side is already for receiving, topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        log.info("send AMOP message to get receiver file context");
        ChannelResponse rsp = this.sendEvent(topic, new FileEvent(FileEvent.EventType.FileChannelStatus, fileId));
        if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            log.info("receive file context is ready, go");
            FileChunksMeta fileChunksMeta = JsonHelper.json2Object(rsp.getContentByteArray(), FileChunksMeta.class);
            this.senderTopics.put(topic, true);
            return fileChunksMeta;
        }

        log.error("receive file context is not exist");
        throw toBrokerException(rsp);
    }

    public ChannelResponse sendEvent(String topic, FileEvent fileEvent) throws BrokerException {
        if (this.subTopics.containsKey(topic)) {
            log.error("this is already receiver side for topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        byte[] json = JsonHelper.object2JsonBytes(fileEvent);

        ChannelRequest channelRequest = new ChannelRequest();
        channelRequest.setToTopic(topic);
        channelRequest.setMessageID(this.service.newSeq());
        channelRequest.setTimeout(5000);
        channelRequest.setContent(json);

        log.info("send channel request, topic: {} id: {}", channelRequest.getToTopic(), channelRequest.getMessageID());
        ChannelResponse rsp = this.service.sendChannelMessage2(channelRequest);
        log.info("receive channel response, id: {} result: {}-{}", rsp.getMessageID(), rsp.getErrorCode(), rsp.getErrorMessage());
        return rsp;
    }

    // event from sender
    @Override
    public void onPush(ChannelPush push) {
        if (!this.subTopics.containsKey(push.getTopic())) {
            log.error("unknown topic on channel, {} -> {}", push.getTopic(), this.subTopics.keySet());
            push.sendResponse(AMOPChannel.toChannelResponse(ErrorCode.UNKNOWN_ERROR));
            return;
        }

        FileEvent fileEvent;
        try {
            fileEvent = JsonHelper.json2Object(push.getContent2(), FileEvent.class);
        } catch (BrokerException e) {
            log.error("invalid file event on channel", e);
            push.sendResponse(AMOPChannel.toChannelResponse(e));
            return;
        }

        log.info("received file event on channel, {}", fileEvent);
        ChannelResponse channelResponse;
        switch (fileEvent.getEventType()) {
            case FileChannelStart: {
                log.info("get {}, try to initialize context for receiving file", fileEvent.getEventType());
                try {
                    FileChunksMeta fileChunksMeta = this.fileTransportService.prepareReceiveFile(fileEvent.getFileChunksMeta());
                    log.info("create file context success, fileId: {}", fileEvent.getFileId());

                    byte[] json = JsonHelper.object2JsonBytes(fileChunksMeta);
                    channelResponse = AMOPChannel.toChannelResponse(ErrorCode.SUCCESS, json);
                } catch (BrokerException e) {
                    log.error("create file context failed, fileId: {}", fileEvent.getFileId());
                    channelResponse = AMOPChannel.toChannelResponse(e);
                }
            }
            break;

            case FileChannelStatus: {
                log.info("get {}", fileEvent.getEventType());
                try {
                    FileChunksMeta fileChunksMeta = this.fileTransportService.loadFileChunksMeta(fileEvent.getFileId());
                    log.info("exist file context, fileId: {}", fileEvent.getFileId());

                    byte[] json = JsonHelper.object2JsonBytes(fileChunksMeta);
                    channelResponse = AMOPChannel.toChannelResponse(ErrorCode.SUCCESS, json);
                } catch (BrokerException e) {
                    log.error("load file context failed", e);
                    channelResponse = AMOPChannel.toChannelResponse(e);
                }
            }
            break;

            case FileChannelData: {
                log.info("get {}, try to write chunk data in local file", fileEvent.getEventType());
                try {
                    this.fileTransportService.writeChunkData(fileEvent);
                    channelResponse = AMOPChannel.toChannelResponse(ErrorCode.SUCCESS);
                } catch (BrokerException e) {
                    log.error("write chunk data in local file failed", e);
                    channelResponse = AMOPChannel.toChannelResponse(e);
                }
            }
            break;

            case FileChannelEnd: {
                log.info("get {}, try to clean up file context", fileEvent.getEventType());
                try {
                    this.fileTransportService.cleanUpReceivedFile(fileEvent.getFileId());
                    channelResponse = AMOPChannel.toChannelResponse(ErrorCode.SUCCESS);
                } catch (BrokerException e) {
                    log.error("clean up not complete file failed", e);
                    channelResponse = AMOPChannel.toChannelResponse(e);
                }
            }
            break;

            default:
                log.error("unknown file event type on channel");
                channelResponse = AMOPChannel.toChannelResponse(ErrorCode.UNKNOWN_ERROR);
                break;
        }

        push.sendResponse(channelResponse);
    }

    private static ChannelResponse toChannelResponse(ErrorCode errorCode) {
        return toChannelResponse(errorCode, "".getBytes());
    }

    private static ChannelResponse toChannelResponse(ErrorCode errorCode, byte[] content) {
        ChannelResponse reply = new ChannelResponse();
        reply.setErrorCode(errorCode.getCode());
        reply.setErrorMessage(errorCode.getCodeDesc());
        reply.setContent(content);
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
