package com.webank.weevent.file.inner;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.core.fisco.web3sdk.v2.Web3SDKConnector;
import com.webank.weevent.file.IWeEventFileClient;
import com.webank.weevent.file.dto.FileEvent;
import com.webank.weevent.file.service.FileChunksMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.fisco.bcos.channel.client.ChannelPushCallback;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.dto.ChannelPush;
import org.fisco.bcos.channel.dto.ChannelRequest;
import org.fisco.bcos.channel.dto.ChannelResponse;
import org.fisco.bcos.channel.handler.AMOPVerifyKeyInfo;
import org.fisco.bcos.channel.handler.AMOPVerifyTopicToKeyInfo;
import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    public Service service;

    // verify topic in AMOP(WeEvent's topic hash) <-> service correspond to topic
    public Map<String, Service> senderVerifyTopics = new ConcurrentHashMap<>();
    // topic not verify
    public List<String> senderTopics = new ArrayList<>();

    //public Map<String, Service> subVerifyTopics = new ConcurrentHashMap<>();
    //  verify topic in AMOP(WeEvent's topic hash) <-> service correspond to topic
    public Map<String, Service> subVerifyTopics = new ConcurrentHashMap<>();
    // topic not verify
    public List<String> subTopics = new ArrayList<>();

    public Map<String, IWeEventFileClient.EventListener> topicListenerMap = new ConcurrentHashMap<>();

    /**
     * Create a AMOP channel on service for subscribe topic
     *
     * @param fileTransportService component class
     * @param groupId group id
     * @throws BrokerException exception
     */
    public AMOPChannel(FileTransportService fileTransportService, String groupId) throws BrokerException {
        this.fileTransportService = fileTransportService;

        // new service
        Service service = Web3SDKConnector.initService(Long.valueOf(groupId), this.fileTransportService.getFiscoConfig());
        this.service = service;
        this.service.setPushCallback(this);
        try {
            this.service.run();
        } catch (Exception e) {
            log.error("service run failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_SERVICE_ERROR);
        }
    }


    public Set<String> getSenderTopics() {
        Set<String> topicMap = new HashSet<>();
        topicMap.addAll(senderTopics);
        topicMap.addAll(senderVerifyTopics.keySet());
        return topicMap;
    }

    public Set<String> getSubTopics() {
        Set<String> topicMap = new HashSet<>();
        topicMap.addAll(subTopics);
        topicMap.addAll(subVerifyTopics.keySet());

        return topicMap;
    }

    public Set<String> getVerifyTopics() {
        Set<String> topics = new HashSet<>();
        for (String topicVerify : subVerifyTopics.keySet()) {
            topics.add(this.subVerifyTopics.get(topicVerify).getNeedVerifyTopics(topicVerify));
        }

        return topics;
    }


    // Receiver call subscribe topic
    public void subTopic(String topic, IWeEventFileClient.EventListener eventListener) throws BrokerException {
        if (this.senderTopics.contains(topic) || senderVerifyTopics.containsKey(topic)) {
            log.error("this is already sender side for topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        if (!this.subTopics.contains(topic)) {
            log.info("subscribe topic on AMOP channel, {}", topic);
            this.topicListenerMap.put(topic, eventListener);
            this.subTopics.add(topic);
            Set<String> topicSet = new HashSet<>(this.subTopics);
            this.service.setTopics(topicSet);
            this.service.updateTopicsToNode();
        }
    }

    // Receiver call subscribe verify topic
    public void subTopic(String topic, String groupId, InputStream privatePem, IWeEventFileClient.EventListener eventListener) throws BrokerException {
        if (this.senderTopics.contains(topic) || senderVerifyTopics.containsKey(topic)) {
            log.error("this is already sender side for topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        Service service = Web3SDKConnector.initService(Long.valueOf(groupId), this.fileTransportService.getFiscoConfig());

        // construct attribute for service
        AMOPVerifyTopicToKeyInfo verifyTopicToKeyInfo = new AMOPVerifyTopicToKeyInfo();
        ConcurrentHashMap<String, AMOPVerifyKeyInfo> topicToKeyInfo = new ConcurrentHashMap<>();
        AMOPVerifyKeyInfo verifyKeyInfo = new AMOPVerifyKeyInfo();

        // set private pem for service
        InputStreamResource inputStreamResource = new InputStreamResource(privatePem);

        verifyKeyInfo.setPrivateKey(inputStreamResource);
        topicToKeyInfo.put(topic, verifyKeyInfo);
        verifyTopicToKeyInfo.setTopicToKeyInfo(topicToKeyInfo);

        // set service attribute
        service.setTopic2KeyInfo(verifyTopicToKeyInfo);

        service.setNeedVerifyTopics(topic);
        service.setPushCallback(this);
        // run service
        try {
            service.run();
        } catch (Exception e) {
            log.error("service run failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_SERVICE_ERROR);
        }
        service.updateTopicsToNode();

        log.info("subscribe verify topic on AMOP channel, {}", topic);
        this.topicListenerMap.put(topic, eventListener);

        // put <topic-service> to map in AMOPChannel
        this.subVerifyTopics.put(topic, service);

    }

    public void unSubTopic(String topic) {
        if (subVerifyTopics.containsKey(topic)) {
            log.info("unSubscribe verify topic on AMOP channel, {}", topic);
            service = this.subVerifyTopics.remove(topic);
            service = null;
            this.topicListenerMap.remove(topic);
        } else {
            if (this.subTopics.contains(topic)) {
                log.info("unSubscribe topic on AMOP channel, {}", topic);
                this.subTopics.remove(topic);
                this.topicListenerMap.remove(topic);
                Set<String> topicSet = new HashSet<>(this.subTopics);
                this.service.setTopics(topicSet);
                this.service.updateTopicsToNode();
            }
        }
    }

    public void deleteTransport(String topic) {
        if (senderVerifyTopics.containsKey(topic)) {
            log.info("delete verify topic on AMOP channel, {}", topic);
            service = this.senderVerifyTopics.remove(topic);
            service = null;
        } else {
            if (this.senderTopics.contains(topic)) {
                log.info("delete topic on AMOP channel, {}", topic);
                this.senderTopics.remove(topic);
                Set<String> topicSet = new HashSet<>(this.senderTopics);
                this.service.setTopics(topicSet);
                this.service.updateTopicsToNode();
            }
        }
    }

    public FileChunksMeta createReceiverFileContext(FileChunksMeta fileChunksMeta) throws BrokerException {
        log.info("send AMOP message to create receiver file context");
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelStart, fileChunksMeta.getFileId());
        fileEvent.setFileChunksMeta(fileChunksMeta);
        ChannelResponse rsp = this.sendEvent(fileChunksMeta.getTopic(), fileEvent);
        if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
            log.info("create remote file context success");
            if (!this.senderTopics.contains(fileChunksMeta.getTopic())) {
                this.senderTopics.add(fileChunksMeta.getTopic());
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
        if (this.subTopics.contains(topic) || this.subVerifyTopics.containsKey(topic)) {
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
        if (this.senderVerifyTopics.containsKey(topic)) {
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

        if (!(this.getVerifyTopics().contains(push.getTopic()) || this.subTopics.contains(push.getTopic()))) {
            log.error("unknown topic on channel, {} -> {}", push.getTopic(), this.subTopics.addAll(this.subVerifyTopics.keySet()));
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
                    log.info("create file context success, fileName: {}", fileEvent.getFileChunksMeta().getFileName());

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

                    //
                    IWeEventFileClient.EventListener eventListener = this.topicListenerMap.get(fileChunksMeta.getTopic());
                    new Thread(new uploadFile2Ftp(fileChunksMeta.getTopic(), fileChunksMeta.getFileName(), eventListener),"thread upload").start();
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
        reply.setContent(e.getMessage());
        return reply;
    }

    public static BrokerException toBrokerException(ChannelResponse reply) {
        if (reply.getErrorCode() < 100000) {
            return new BrokerException(reply.getErrorCode(), reply.getErrorMessage());
        } else {
            return new BrokerException(reply.getErrorCode(), ErrorCode.getDescByCode(reply.getErrorCode()));
        }

    }

    // new class for upload file to ftp server
    static class uploadFile2Ftp implements Runnable {
        private final String topic;
        private final String fileName;
        private final IWeEventFileClient.EventListener eventListener;


        public uploadFile2Ftp(String topic, String fileName, IWeEventFileClient.EventListener eventListener){
            this.topic = topic;
            this.fileName = fileName;
            this.eventListener = eventListener;
        }
        @Override
        public void run() {
            eventListener.onEvent(this.topic, this.fileName);
        }
    }
}
