package com.webank.weevent.file.service;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.SendResult;
import com.webank.weevent.core.FiscoBcosInstance;
import com.webank.weevent.core.IConsumer;
import com.webank.weevent.core.IProducer;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.core.fisco.web3sdk.v2.Web3SDKConnector;
import com.webank.weevent.file.IWeEventFileClient;
import com.webank.weevent.file.dto.FileChunksMetaStatus;
import com.webank.weevent.file.dto.FileTransportStats;
import com.webank.weevent.file.inner.AMOPChannel;
import com.webank.weevent.file.inner.FileTransportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.AMOPVerifyKeyInfo;
import org.fisco.bcos.channel.handler.AMOPVerifyTopicToKeyInfo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WeEventFileClient implements IWeEventFileClient {

    private final String groupId;
    private String filePath ="";
    private FiscoConfig config;
    private FileTransportService fileTransportService;
    private int fileChunkSize;

    public WeEventFileClient(String groupId, String receiveFilePath, int fileChunkSize, FiscoConfig config) {
        this.groupId = groupId;
        this.filePath = receiveFilePath;
        this.fileChunkSize = fileChunkSize;
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

            // 创建消费者
            IConsumer iConsumer = fiscoBcosInstance.buildConsumer();
            iConsumer.startConsumer();

            // 创建FileTransportService实例
            FileTransportService fileTransportService = new FileTransportService(this.config, iProducer, "", this.filePath, this.fileChunkSize, this.groupId);
            this.fileTransportService = fileTransportService;

        } catch (BrokerException e) {
            log.error("init WeEventFileClient failed", e);
        }
    }

    /**
     * @param topic topic name
     */
    public void openTransport4Sender(String topic) {
        if (!this.fileTransportService.getChannel().senderTopics.contains(topic)) {
            this.fileTransportService.getChannel().senderTopics.add(topic);
        }
    }

    /**
     * @param topic topic name
     * @param publicPem public pem inputstream
     * @throws BrokerException exception
     */
    public void openTransport4Sender(String topic, InputStream publicPem) throws BrokerException {

        // get AMOPChannel, fileTransportService and amopChannel is One-to-one correspondence
        AMOPChannel amopChannel = this.fileTransportService.getChannel();

        // service is exist
        if (amopChannel.getSenderTopics().contains(topic) || amopChannel.senderVerifyTopics.containsKey(topic)) {
            log.error("this is already sender side for topic: {}", topic);
            throw new BrokerException(ErrorCode.FILE_SENDER_RECEIVER_CONFLICT);
        }

        // service not exist, new service
        Service service = Web3SDKConnector.initService(Long.valueOf(this.groupId), this.fileTransportService.getFiscoConfig());

        // construct attribute for service
        AMOPVerifyTopicToKeyInfo verifyTopicToKeyInfo = new AMOPVerifyTopicToKeyInfo();
        ConcurrentHashMap<String, AMOPVerifyKeyInfo> topicToKeyInfo = new ConcurrentHashMap<>();
        AMOPVerifyKeyInfo verifyKeyInfo = new AMOPVerifyKeyInfo();

        // set private pem for service
        InputStreamResource inputStreamResource = new InputStreamResource(publicPem);
        List<Resource> publicPemList = new ArrayList<>();
        publicPemList.add(inputStreamResource);

        verifyKeyInfo.setPublicKey(publicPemList);
        topicToKeyInfo.put(topic, verifyKeyInfo);
        verifyTopicToKeyInfo.setTopicToKeyInfo(topicToKeyInfo);

        // set service attribute
        service.setNeedVerifyTopics(topic);
        service.setTopic2KeyInfo(verifyTopicToKeyInfo);

        // run service
        try {
            service.run();
        } catch (Exception e) {
            log.error("service run failed", e);
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_SERVICE_ERROR);
        }

        // put <topic-service> to map in AMOPChannel
        amopChannel.senderVerifyTopics.put(topic, service);
    }


    /**
     * @param topic topic name
     * @param publicPemPath public pem path string
     * @throws BrokerException exception
     * @throws IOException exception
     */
    public void openTransport4Sender(String topic, String publicPemPath) throws BrokerException, IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:" + publicPemPath);

        openTransport4Sender(topic, resource.getInputStream());
    }




    /**
     * @param topic topic name
     * @param fileListener notify interface
     * @throws BrokerException broker exception
     */
    public void openTransport4Receiver(String topic, FileListener fileListener) throws BrokerException {
        AMOPChannel amopChannel = this.fileTransportService.getChannel();

        amopChannel.subTopic(topic, fileListener);
    }

    /**
     * @param topic topic name
     * @param fileListener notify interface
     * @param privatePem private key pem inputstream
     * @throws BrokerException broker exception
     */
    public void openTransport4Receiver(String topic, FileListener fileListener, InputStream privatePem) throws BrokerException {
        // get AMOPChannel, fileTransportService and amopChannel is One-to-one correspondence
        AMOPChannel amopChannel = this.fileTransportService.getChannel();


        amopChannel.subTopic(topic, groupId, privatePem, fileListener);
    }

    /**
     * @param topic topic name
     * @param fileListener notify interface
     * @param privatePemPath private key pem path string
     * @throws IOException IOException
     * @throws BrokerException InterruptedException
     */
    public void openTransport4Receiver(String topic, FileListener fileListener, String privatePemPath) throws IOException, BrokerException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:" + privatePemPath);

        openTransport4Receiver(topic, fileListener, resource.getInputStream());
    }

    /**
     * @param topic topic name
     */
    public void closeTransport(String topic) {
        AMOPChannel channel = this.fileTransportService.getChannel();
        channel.unSubTopic(topic);
    }

    /**
     * @param topic topic name
     * @return filechunksmeta  filechunksmeta
     */
    public List<FileChunksMeta> listFiles(String topic) {
        List<FileChunksMeta> fileChunksMetas = this.fileTransportService.getFileChunksMeta(topic);
        return fileChunksMetas;
    }


    /**
     * Publish a file to topic.
     * The file's data DO NOT stored in block chain. Yes, it's not persist, may be deleted sometime after subscribe notify.
     *
     * @param topic binding topic
     * @param localFile local file to be send
     * @return send result, SendResult.SUCCESS if success, and return SendResult.eventId
     * @throws BrokerException broker exception
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    @Override
    public SendResult publishFile(String topic, String localFile) throws BrokerException, IOException, InterruptedException {
        // upload file
        validateLocalFile(localFile);

        FileChunksTransport fileChunksTransport = new FileChunksTransport(this.fileTransportService);
        SendResult sendResult = fileChunksTransport.upload(localFile, topic, this.groupId);
        return sendResult;
    }

    /**
     * @param topicName topic name
     * @return transport status
     */
    @Override
    public FileTransportStats status(String topicName) {
        FileTransportStats fileTransportStats = this.fileTransportService.stats(true, this.groupId);
        if (fileTransportStats == null) {
            log.error("get status error");
        }

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
}
