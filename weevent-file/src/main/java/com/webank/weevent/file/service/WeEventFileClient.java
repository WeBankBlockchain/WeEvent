package com.webank.weevent.file.service;

import com.webank.weevent.client.*;
import com.webank.weevent.core.FiscoBcosInstance;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.file.IWeEventFileClient;
import com.webank.weevent.file.dto.FileChunksMetaStatus;
import com.webank.weevent.file.dto.FileTransportStats;
import com.webank.weevent.file.inner.AMOPChannel;
import com.webank.weevent.file.inner.FileEventListener;
import com.webank.weevent.file.inner.FileTransportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class WeEventFileClient implements IWeEventFileClient {

    private final String groupId;
    private String filePath ="";
    private final FiscoConfig config;
    private IProducer iProducer;
    private IConsumer iConsumer;
    private FileTransportService fileTransportService;

    public WeEventFileClient(String groupId, String filePath ,FiscoConfig config) {
        this.groupId = groupId;
        this.filePath = filePath;
        this.config = config;
        init();
    }

    public WeEventFileClient(String groupId, FiscoConfig config) {
        this.groupId = groupId;
        this.config = config;
        init();
    }

    public void init() {
        try {
            // 获取FISCO实例
            FiscoBcosInstance fiscoBcosInstance = new FiscoBcosInstance(this.config);

            // 创建生产者
            IProducer iProducer = fiscoBcosInstance.buildProducer();
            iProducer.startProducer();
            this.iProducer = iProducer;

            // 创建消费者
            IConsumer iConsumer = fiscoBcosInstance.buildConsumer();
            iConsumer.startConsumer();
            this.iConsumer = iConsumer;

            // 创建FileTransportService实例
            FileTransportService fileTransportService = new FileTransportService(this.config, this.iProducer, "", this.filePath, 1048576);
            this.fileTransportService = fileTransportService;
        } catch (BrokerException e) {
            log.error("init WeEventFileClient failed", e);
        }
    }


    public void openTransport4Sender(String topic) throws BrokerException {
        AMOPChannel channel = this.fileTransportService.getChannel(this.groupId);
        if (channel.getAMOPTopicNames().contains(topic)) {
            log.error("this is already sender side for topic: {}", topic);
        }
    }

    public void openTransport4Sender(String topic, InputStream publicPem) throws BrokerException {
        //AMOPChannel channel = this.fileTransportService.getChannel(this.groupId);
        //channel.initVerifyTopic();
    }
    public void openTransport4Sender(String topic, String publicPem) { }

    public void openTransport4Receiver(String topic, String filePath, FileListener fileListener) {

    }
    public void openTransport4Receiver(String topic, String filePath, FileListener fileListener, InputStream privatePem) {

    }
    public void openTransport4Receiver(String topic, String filePath, FileListener fileListener, String privatePem) { }

    public void closeTransport(String topic) { }

    public FileChunksMeta listFiles(String topic) {
        return null;
    }


    @Override
    public SendResult publishFile(String topic, String localFile) throws BrokerException, IOException, InterruptedException {
        // upload file
        validateLocalFile(localFile);

        FileChunksTransport fileChunksTransport = new FileChunksTransport(this.fileTransportService);
        SendResult sendResult = fileChunksTransport.upload(localFile, topic, this.groupId);
        return sendResult;
    }

    @Override
    public void subscribeFile(String topic, String filePath, FileListener fileListener) throws BrokerException {
        validateLocalFile(filePath);

        FileEventListener fileEventListener = new FileEventListener(this.fileTransportService, topic, groupId, fileListener);
    }



    @Override
    public FileTransportStats status(String topicName) {
        FileTransportStats fileTransportStats = this.fileTransportService.stats(true);
        if (fileTransportStats == null) { }

        // sender
        Map<String, List<FileChunksMetaStatus>> senderTopicStatusMap = new HashMap<>();
        List<FileChunksMetaStatus> senderFileChunksMetaStatusList = new ArrayList<>();
        senderFileChunksMetaStatusList = fileTransportStats.getSender().get(groupId).get(topicName);
        senderTopicStatusMap.put(topicName, senderFileChunksMetaStatusList);
        Map<String, Map<String, List<FileChunksMetaStatus>>> sender = new HashMap<>();
        sender.put(groupId, senderTopicStatusMap);

        // receiver
        Map<String, List<FileChunksMetaStatus>> receiverTopicStatusMap = new HashMap<>();
        List<FileChunksMetaStatus> receiverFileChunksMetaStatusList = new ArrayList<>();
        receiverFileChunksMetaStatusList = fileTransportStats.getSender().get(groupId).get(topicName);
        senderTopicStatusMap.put(topicName, receiverFileChunksMetaStatusList);
        Map<String, Map<String, List<FileChunksMetaStatus>>> receiver = new HashMap<>();
        receiver.put(groupId, receiverTopicStatusMap);

        FileTransportStats retFileTransportStats = new FileTransportStats();
        retFileTransportStats.setSender(sender);
        retFileTransportStats.setReceiver(receiver);

        return retFileTransportStats;
    }

    private static void validateLocalFile(String filePath) throws BrokerException {
        if (StringUtils.isBlank(filePath)) {
            throw new BrokerException(ErrorCode.LOCAL_FILE_IS_EMPTY);
        }
        if (!(new File(filePath)).exists()) {
            throw new BrokerException(ErrorCode.LOCAL_FILE_NOT_EXIST);
        }
    }

    private static void validateParam(String param) throws BrokerException {
        if (StringUtils.isBlank(param)) {
            throw new BrokerException(ErrorCode.PARAM_ISBLANK);
        }
    }
}
