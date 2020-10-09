package com.webank.weevent.file.inner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.JsonHelper;
import com.webank.weevent.core.dto.AmopMsgResponse;
import com.webank.weevent.core.fisco.util.DataTypeUtils;
import com.webank.weevent.core.fisco.web3sdk.v2.Web3SDKConnector;
import com.webank.weevent.file.dto.FileEvent;
import com.webank.weevent.file.service.FileChunksMeta;
import com.webank.weevent.file.service.WeEventFileClient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.BcosSDKException;
import org.fisco.bcos.sdk.amop.Amop;
import org.fisco.bcos.sdk.amop.AmopCallback;
import org.fisco.bcos.sdk.amop.AmopMsgOut;
import org.fisco.bcos.sdk.amop.topic.AmopMsgIn;
import org.fisco.bcos.sdk.amop.topic.TopicType;
import org.fisco.bcos.sdk.crypto.keystore.KeyTool;
import org.fisco.bcos.sdk.crypto.keystore.PEMKeyStore;
import org.fisco.bcos.sdk.model.Response;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
public class AMOPChannel extends AmopCallback {
    private final FileTransportService fileTransportService;
    //    public Service service;
    public Amop amop;
    public ThreadPoolTaskExecutor threadPool;

    // verify topic in AMOP(WeEvent's topic hash) <-> service correspond to topic
    public Map<String, Amop> senderVerifyTopics = new ConcurrentHashMap<>();
    // topic not verify
    public List<String> senderTopics = new ArrayList<>();

    //public Map<String, Service> subVerifyTopics = new ConcurrentHashMap<>();
    //  verify topic in AMOP(WeEvent's topic hash) <-> service correspond to topic
    public Map<String, Amop> subVerifyTopics = new ConcurrentHashMap<>();
    // topic not verify
    public List<String> subTopics = new ArrayList<>();

    public Map<String, WeEventFileClient.EventListener> topicListenerMap = new ConcurrentHashMap<>();

    /**
     * Create a AMOP channel on service for subscribe topic
     *
     * @param fileTransportService component class
     * @param groupId group id
     * @throws BrokerException exception
     */
    public AMOPChannel(FileTransportService fileTransportService, String groupId) throws BrokerException {
        this.fileTransportService = fileTransportService;
        this.threadPool = this.initThreadPool(1, 10);

        // new service
        try {
            this.amop = Web3SDKConnector.buidBcosSDK(this.fileTransportService.getFiscoConfig()).getAmop();
        } catch (BcosSDKException e) {
            log.error("build BcosSDK failed.", e);
            throw new BrokerException(ErrorCode.BCOS_SDK_BUILD_ERROR);
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
            topics.addAll(this.subVerifyTopics.get(topicVerify).getSubTopics());
        }

        return topics;
    }


    // Receiver call subscribe topic
    public void subTopic(String topic, WeEventFileClient.EventListener eventListener) throws BrokerException {
        if (this.senderTopics.contains(topic) || senderVerifyTopics.containsKey(topic)) {
            log.error("this is already sender side for topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        if (!this.subTopics.contains(topic)) {
            log.info("subscribe topic on AMOP channel, {}", topic);
            this.topicListenerMap.put(topic, eventListener);
            this.subTopics.add(topic);
            this.amop.subscribeTopic(topic, this);
        }
    }

    // Receiver call subscribe verify topic
    public void subTopic(String topic, String groupId, InputStream privatePem, WeEventFileClient.EventListener eventListener) throws BrokerException {
        if (this.senderTopics.contains(topic) || senderVerifyTopics.containsKey(topic)) {
            log.error("this is already sender side for topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        Amop amop = Web3SDKConnector.buidBcosSDK(this.fileTransportService.getFiscoConfig()).getAmop();

        KeyTool kt;
        try {
            kt = new PEMKeyStore(privatePem);
        } catch (Exception e) {
            log.error("load private key in pem format failed.", e);
            throw new BrokerException(ErrorCode.FILE_PEM_KEY_INVALID);
        }

        amop.subscribePrivateTopics(topic, kt, this);
        log.info("subscribe verify topic on AMOP channel, {}", topic);
        this.topicListenerMap.put(topic, eventListener);

        // put <topic-service> to map in AMOPChannel
        this.subVerifyTopics.put(topic, amop);

    }

    public void unSubTopic(String topic) {
        if (subVerifyTopics.containsKey(topic)) {
            log.info("unSubscribe verify topic on AMOP channel, {}", topic);
            this.subVerifyTopics.get(topic).unsubscribeTopic(topic);
            this.subVerifyTopics.remove(topic);
            this.topicListenerMap.remove(topic);
        } else {
            if (this.subTopics.contains(topic)) {
                log.info("unSubscribe topic on AMOP channel, {}", topic);
                this.subTopics.remove(topic);
                this.topicListenerMap.remove(topic);
                this.amop.unsubscribeTopic(topic);
            }
        }
    }

    public void deleteTransport(String topic) {
        if (senderVerifyTopics.containsKey(topic)) {
            log.info("delete verify topic on AMOP channel, {}", topic);
            this.subVerifyTopics.get(topic).unsubscribeTopic(topic);
            this.subVerifyTopics.remove(topic);
            this.topicListenerMap.remove(topic);
        } else {
            if (this.senderTopics.contains(topic)) {
                log.info("delete topic on AMOP channel, {}", topic);
                this.senderTopics.remove(topic);
                this.amop.unsubscribeTopic(topic);
            }
        }
    }

    public FileChunksMeta createReceiverFileContext(FileChunksMeta fileChunksMeta) throws BrokerException {
        log.info("send AMOP message to create receiver file context");
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelStart, fileChunksMeta.getFileId());
        fileEvent.setFileChunksMeta(fileChunksMeta);
        try {
            Response rsp = this.sendEvent(fileChunksMeta.getTopic(), fileEvent);
            if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
                log.info("create remote file context success");
                if (!this.senderTopics.contains(fileChunksMeta.getTopic())) {
                    this.senderTopics.add(fileChunksMeta.getTopic());
                }
                return JsonHelper.json2Object(rsp.getContentBytes(), FileChunksMeta.class);
            } else {
                log.error("create remote file context failed");
                throw toBrokerException(rsp);
            }
        } catch (InterruptedException | TimeoutException e) {
            log.error("InterruptedException | TimeoutException while send amop request");
            throw new BrokerException(ErrorCode.SEND_AMOP_MESSAGE_FAILED);
        }
    }

    public FileChunksMeta cleanUpReceiverFileContext(String topic, String fileId) throws BrokerException {
        log.info("send AMOP message to clean up receiver file context");

        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelEnd, fileId);
        try {
            Response rsp = this.sendEvent(topic, fileEvent);
            if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
                log.info("clean up receiver file context success");
                return JsonHelper.json2Object(rsp.getContentBytes(), FileChunksMeta.class);
            } else {
                log.error("clean up remote file context failed");
                throw toBrokerException(rsp);
            }
        } catch (InterruptedException | TimeoutException e) {
            log.error("InterruptedException | TimeoutException while send amop request");
            throw new BrokerException(ErrorCode.SEND_AMOP_MESSAGE_FAILED);
        }
    }

    public FileChunksMeta getReceiverFileContext(String topic, String fileId) throws BrokerException {
        log.info("send AMOP message to get receiver file context");
        try {
            Response rsp = this.sendEvent(topic, new FileEvent(FileEvent.EventType.FileChannelStatus, fileId));
            if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
                log.info("receive file context is ready, go");
                return JsonHelper.json2Object(rsp.getContentBytes(), FileChunksMeta.class);
            } else {
                log.error("receive file context is not exist");
                throw toBrokerException(rsp);
            }
        } catch (InterruptedException | TimeoutException e) {
            log.error("InterruptedException | TimeoutException while send amop request");
            throw new BrokerException(ErrorCode.SEND_AMOP_MESSAGE_FAILED);
        }
    }

    public boolean isFileExist(FileChunksMeta fileChunksMeta) throws BrokerException {
        log.info("send AMOP message to Check file existence");
        FileEvent fileEvent = new FileEvent(FileEvent.EventType.FileChannelExist, fileChunksMeta.getFileId());
        fileEvent.setFileChunksMeta(fileChunksMeta);

        try {
            Response rsp = this.sendEvent(fileChunksMeta.getTopic(), fileEvent);
            if (rsp.getErrorCode() == ErrorCode.SUCCESS.getCode()) {
                log.info("Check file existence success");
                return JsonHelper.json2Object(rsp.getContentBytes(), Boolean.class);
            } else {
                log.error("Check file existence failed");
                throw toBrokerException(rsp);
            }
        } catch (InterruptedException | TimeoutException e) {
            log.error("InterruptedException | TimeoutException while send amop request");
            throw new BrokerException(ErrorCode.SEND_AMOP_MESSAGE_FAILED);
        }
    }

    public Response sendEvent(String topic, FileEvent fileEvent) throws BrokerException, InterruptedException, TimeoutException {
        if (this.subTopics.contains(topic) || this.subVerifyTopics.containsKey(topic)) {
            log.error("this is already receiver side for topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        byte[] json = JsonHelper.object2JsonBytes(fileEvent);
        AmopMsgOut msgOut = new AmopMsgOut();
        msgOut.setContent(json);
        msgOut.setTopic(topic);
        msgOut.setTimeout(6000L);
        if (this.senderVerifyTopics.containsKey(topic)) {
            log.info("over verified AMOP channel");
            msgOut.setType(TopicType.PRIVATE_TOPIC);
        } else {
            msgOut.setType(TopicType.NORMAL_TOPIC);
        }

        log.info("send channel request, topic: {} {}", topic, fileEvent.getEventType());
        StopWatch sw = StopWatch.createStarted();
        AmopResponseCallback callback = new AmopResponseCallback();
        this.amop.sendAmopMsg(msgOut, callback);
        Response response = callback.get(msgOut.getTimeout(), TimeUnit.MINUTES);
        sw.stop();
        log.info("receive channel response, id: {} result: {}-{} cost: {}", response.getMessageID(), response.getErrorCode(), response.getErrorMessage(), sw.getTime());
        return response;

    }

    public static BrokerException toBrokerException(Response reply) {
        if (reply.getErrorCode() < 100000) {
            return new BrokerException(reply.getErrorCode(), reply.getErrorMessage());
        } else {
            return new BrokerException(reply.getErrorCode(), ErrorCode.getDescByCode(reply.getErrorCode()));
        }

    }

    private ThreadPoolTaskExecutor initThreadPool(int core, int keepalive) {
        // init thread pool
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setThreadNamePrefix("ftp service-");
        pool.setCorePoolSize(core);
        // queue conflict with thread pool scale up, forbid it
        pool.setQueueCapacity(0);
        pool.setKeepAliveSeconds(keepalive);
        // abort policy
        pool.setRejectedExecutionHandler(null);
        pool.setDaemon(true);
        pool.initialize();

        log.info("init ThreadPoolTaskExecutor");
        return pool;
    }

    @Override
    public byte[] receiveAmopMsg(AmopMsgIn msg) {
        if (!(this.getVerifyTopics().contains(msg.getTopic()) || this.subTopics.contains(msg.getTopic()))) {
            log.error("unknown topic on channel, {} -> {}", msg.getTopic(), this.subTopics.addAll(this.subVerifyTopics.keySet()));
            return DataTypeUtils.toChannelResponse(ErrorCode.UNKNOWN_AMOP_SUB_TOPIC);
        }

        FileEvent fileEvent;
        try {
            fileEvent = JsonHelper.json2Object(msg.getContent(), FileEvent.class);
        } catch (BrokerException e) {
            log.error("invalid file event on channel", e);
            return DataTypeUtils.toChannelResponse(e);
        }

        byte[] channelResponseDate = new byte[0];
        log.info("received file event on channel, {}", fileEvent);
        switch (fileEvent.getEventType()) {
            case FileChannelStart: {
                log.info("get {}, try to initialize context for receiving file", fileEvent.getEventType());
                try {
                    FileChunksMeta fileChunksMeta = this.fileTransportService.prepareReceiveFile(fileEvent.getFileChunksMeta());
                    log.info("create file context success, fileName: {}", fileEvent.getFileChunksMeta().getFileName());

                    channelResponseDate = DataTypeUtils.toChannelResponse(ErrorCode.SUCCESS, fileChunksMeta);
                } catch (BrokerException e) {
                    log.error("create file context failed, fileId: {}", fileEvent.getFileId());
                    channelResponseDate = DataTypeUtils.toChannelResponse(e);
                }
            }
            break;

            case FileChannelStatus: {
                log.info("get {}", fileEvent.getEventType());
                try {
                    FileChunksMeta fileChunksMeta = this.fileTransportService.loadFileChunksMeta(fileEvent.getFileId());
                    log.info("exist file context, fileId: {}", fileEvent.getFileId());

                    channelResponseDate = DataTypeUtils.toChannelResponse(ErrorCode.SUCCESS, fileChunksMeta);
                } catch (BrokerException e) {
                    log.error("load file context failed", e);
                    channelResponseDate = DataTypeUtils.toChannelResponse(e);
                }
            }
            break;

            case FileChannelData: {
                log.info("get {}, try to write chunk data in local file", fileEvent.getEventType());
                try {
                    this.fileTransportService.writeChunkData(fileEvent);

                    return DataTypeUtils.toChannelResponse(ErrorCode.SUCCESS);
                } catch (BrokerException e) {
                    log.error("write chunk data in local file failed", e);
                    channelResponseDate = DataTypeUtils.toChannelResponse(e);
                }
            }
            break;

            case FileChannelEnd: {
                log.info("get {}, try to clean up file context", fileEvent.getEventType());
                try {
                    FileChunksMeta fileChunksMeta = this.fileTransportService.cleanUpReceivedFile(fileEvent.getFileId());
                    channelResponseDate = DataTypeUtils.toChannelResponse(ErrorCode.SUCCESS, fileChunksMeta);

                    // new thread upload file to ftp server
                    WeEventFileClient.EventListener eventListener = this.topicListenerMap.get(fileChunksMeta.getTopic());
                    threadPool.execute(() -> eventListener.onEvent(fileChunksMeta.getTopic(), fileChunksMeta.getFileName()));
                } catch (BrokerException e) {
                    log.error("clean up not complete file failed", e);
                    channelResponseDate = DataTypeUtils.toChannelResponse(e);
                }
            }
            break;

            case FileChannelExist: {
                log.info("get {}, check if the file exists", fileEvent.getEventType());
                try {
                    FileChunksMeta fileChunksMeta = fileEvent.getFileChunksMeta();
                    boolean fileExistLocal = this.fileTransportService.checkFileExist(fileChunksMeta);
                    log.info("check if the file exists success, fileName: {}, local file existence: {}", fileChunksMeta.getFileName(), fileExistLocal);

                    WeEventFileClient.EventListener eventListener = this.topicListenerMap.get(fileChunksMeta.getTopic());
                    boolean fileExistFtp = eventListener.checkFile(fileChunksMeta.getFileName());
                    log.info("check if the file exists success, fileName: {}, ftp file existence: {}", fileChunksMeta.getFileName(), fileExistFtp);

                    channelResponseDate = DataTypeUtils.toChannelResponse(ErrorCode.SUCCESS, (fileExistLocal || fileExistFtp));
                } catch (BrokerException e) {
                    log.error("check if the file exists failed", e);
                    channelResponseDate = DataTypeUtils.toChannelResponse(e);
                }
            }
            break;

            default:
                log.error("unknown file event type on channel");
                channelResponseDate = DataTypeUtils.toChannelResponse(ErrorCode.UNKNOWN_ERROR);
        }

        return channelResponseDate;
    }
}
