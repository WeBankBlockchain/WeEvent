package com.webank.weevent.broker.fisco.file;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.broker.fisco.file.dto.FileEvent;
import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.FileChunksMeta;
import com.webank.weevent.client.JsonHelper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.fisco.bcos.channel.client.ChannelPushCallback;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.dto.ChannelPush;
import org.fisco.bcos.channel.dto.ChannelRequest;
import org.fisco.bcos.channel.dto.ChannelResponse;
import org.fisco.bcos.channel.handler.AMOPVerifyKeyInfo;
import org.fisco.bcos.channel.handler.AMOPVerifyTopicToKeyInfo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

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

    // topic in AMOP(WeEvent's topic hash) <-> true if verified topic, used by sender
    private Map<String, Boolean> senderTopics = new ConcurrentHashMap<>();
    // topic in AMOP(WeEvent's topic hash) <-> true if verified topic, used by receiver
    private Map<String, Boolean> subTopics = new ConcurrentHashMap<>();

    /**
     * Create a AMOP channel on service for subscribe topic
     *
     * @param fileTransportService component class
     * @param service initialized service
     */
    public AMOPChannel(FileTransportService fileTransportService, Service service) throws BrokerException {
        this.fileTransportService = fileTransportService;
        this.service = service;
        this.service.setPushCallback(this);

        // init verify topic information, MUST be call before service.run
        this.initVerifyTopic(this.service.getGroupId());

        try {
            this.service.run();
        } catch (Exception e) {
            log.error("service run failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_SERVICE_ERROR);
        }
    }

    public Map<String, Boolean> getSenderTopics() {
        return this.senderTopics;
    }

    public Map<String, Boolean> getSubTopics() {
        return this.subTopics;
    }

    // path in resources like "file-transport\sender\1"
    private Map<String, List<Resource>> loadResources(String path) throws BrokerException {
        Map<String, List<Resource>> resources = new HashMap<>();

        Resource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.info("not exist verified topic path: {}", path);
            return resources;
        }

        try {
            File[] topics = resource.getFile().listFiles((dir, name) -> name.endsWith(".pem"));
            if (topics != null) {
                for (File topic : topics) {
                    List<Resource> pemResources = new ArrayList<>();
                    File[] pems = topic.listFiles();
                    if (pems != null) {
                        for (File pem : pems) {
                            pemResources.add(new FileSystemResource(pem.getPath()));
                        }
                        if (!pemResources.isEmpty()) {
                            resources.put(topic.getName(), pemResources);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("load verified topic PEM resources failed", e);
            throw new BrokerException(ErrorCode.FILE_INIT_VERIFY_FAILED);
        }

        if (!resources.isEmpty()) {
            log.info("load verified topic PEM resources in path: {}, {}", path, resources);
        }
        return resources;
    }

    private void initVerifyTopic(int groupId) throws BrokerException {
        AMOPVerifyTopicToKeyInfo verifyTopicToKeyInfo = new AMOPVerifyTopicToKeyInfo();
        ConcurrentHashMap<String, AMOPVerifyKeyInfo> topicToKeyInfo = new ConcurrentHashMap<>();
        AMOPVerifyKeyInfo verifyKeyInfo = new AMOPVerifyKeyInfo();

        // load PEM in sender
        Map<String, List<Resource>> senderResources = this.loadResources("file-transport/sender/" + groupId + "/");
        for (Map.Entry<String, List<Resource>> resource : senderResources.entrySet()) {
            verifyKeyInfo.setPublicKey(resource.getValue());

            this.senderTopics.put(resource.getKey(), true);
            topicToKeyInfo.put(resource.getKey(), verifyKeyInfo);
        }

        // load PEM in receiver
        Map<String, List<Resource>> receiverResources = this.loadResources("file-transport/receiver/" + groupId + "/");
        for (Map.Entry<String, List<Resource>> resource : receiverResources.entrySet()) {
            if (resource.getValue().size() > 1) {
                log.error("more than one private key");
                throw new BrokerException(ErrorCode.FILE_INIT_VERIFY_FAILED);
            }
            verifyKeyInfo.setPrivateKey(resource.getValue().get(0));

            this.subTopics.put(resource.getKey(), true);
            topicToKeyInfo.put(resource.getKey(), verifyKeyInfo);
            this.service.setNeedVerifyTopics(resource.getKey());
        }

        verifyTopicToKeyInfo.setTopicToKeyInfo(topicToKeyInfo);
        this.service.setTopic2KeyInfo(verifyTopicToKeyInfo);
    }

    private Set<String> getAMOPTopicNames() {
        Set<String> topics = new HashSet<>();
        for (Map.Entry<String, Boolean> topic : this.subTopics.entrySet()) {
            if (topic.getValue()) {
                topics.add(this.service.getNeedVerifyTopics(topic.getKey()));
            } else {
                topics.add(topic.getKey());
            }
        }
        return topics;
    }

    public void subTopic(String topic) throws BrokerException {
        if (this.senderTopics.containsKey(topic)) {
            log.error("this is already sender side for topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        // verified topic DO NOT need/support dynamic subscribe
        if (this.subTopics.containsKey(topic) && this.subTopics.get(topic)) {
            return;
        }

        if (!this.subTopics.containsKey(topic)) {
            log.info("subscribe topic on AMOP channel, {}", topic);
            this.subTopics.put(topic, false);

            this.service.setTopics(this.getAMOPTopicNames());
            this.service.updateTopicsToNode();
        }
    }

    public void unSubTopic(String topic) {
        if (this.subTopics.containsKey(topic)) {
            // verified topic DO NOT need/support dynamic subscribe
            if (this.subTopics.get(topic)) {
                return;
            }

            log.info("unSubscribe topic on AMOP channel, {}", topic);
            this.subTopics.remove(topic);

            this.service.setTopics(this.getAMOPTopicNames());
            this.service.updateTopicsToNode();
        }
    }

    public FileChunksMeta createReceiverFileContext(FileChunksMeta fileChunksMeta) throws BrokerException {
        log.info("send AMOP message to create receiver file context");
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelStart, fileChunksMeta.getFileId());
        fileEvent.setFileChunksMeta(fileChunksMeta);
        ChannelResponse rsp = this.sendEvent(fileChunksMeta.getTopic(), fileEvent);
        if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            log.info("create remote file context success");
            if (!this.senderTopics.containsKey(fileChunksMeta.getTopic())) {
                this.senderTopics.put(fileChunksMeta.getTopic(), false);
            }
            return JsonHelper.json2Object(rsp.getContentByteArray(), FileChunksMeta.class);
        }

        log.error("create remote file context failed");
        throw toBrokerException(rsp);
    }

    public FileChunksMeta cleanUpReceiverFileContext(String topic, String fileId) throws BrokerException {
        log.info("send AMOP message to clean up receiver file context");

        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelEnd, fileId);
        ChannelResponse rsp = this.sendEvent(topic, fileEvent);
        if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            log.info("clean up receiver file context success");
            return JsonHelper.json2Object(rsp.getContentByteArray(), FileChunksMeta.class);
        } else {
            log.error("clean up remote file context failed");
            throw toBrokerException(rsp);
        }
    }

    public FileChunksMeta getReceiverFileContext(String topic, String fileId) throws BrokerException {
        log.info("send AMOP message to get receiver file context");
        ChannelResponse rsp = this.sendEvent(topic, new FileEvent(FileEvent.EventType.FileChannelStatus, fileId));
        if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            log.info("receive file context is ready, go");
            return JsonHelper.json2Object(rsp.getContentByteArray(), FileChunksMeta.class);
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
        channelRequest.setTimeout(this.service.getConnectSeconds() * 1000);
        channelRequest.setContent(json);

        log.info("send channel request, topic: {} {} id: {}", channelRequest.getToTopic(), fileEvent.getEventType(), channelRequest.getMessageID());
        ChannelResponse rsp;
        StopWatch sw = StopWatch.createStarted();
        if (this.senderTopics.containsKey(topic) && this.senderTopics.get(topic)) {
            log.info("over verified AMOP channel");
            rsp = this.service.sendChannelMessageForVerifyTopic(channelRequest);
        } else {
            rsp = this.service.sendChannelMessage2(channelRequest);
        }
        sw.stop();
        log.info("receive channel response, id: {} result: {}-{} cost: {}", rsp.getMessageID(), rsp.getErrorCode(), rsp.getErrorMessage(), sw.getTime());
        return rsp;
    }

    // event from sender
    @Override
    public void onPush(ChannelPush push) {
        if (!this.getAMOPTopicNames().contains(push.getTopic())) {
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
                    FileChunksMeta fileChunksMeta = this.fileTransportService.cleanUpReceivedFile(fileEvent.getFileId());
                    byte[] json = JsonHelper.object2JsonBytes(fileChunksMeta);
                    channelResponse = AMOPChannel.toChannelResponse(ErrorCode.SUCCESS, json);
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
