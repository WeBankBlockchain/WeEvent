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
import com.webank.weevent.file.dto.FileChunksMetaPlus;
import com.webank.weevent.file.dto.FileChunksMetaStatus;
import com.webank.weevent.file.dto.FileTransportStats;
import com.webank.weevent.file.ftpclient.FtpClientService;
import com.webank.weevent.file.inner.AMOPChannel;
import com.webank.weevent.file.inner.DiskFiles;
import com.webank.weevent.file.inner.FileTransportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.AMOPVerifyKeyInfo;
import org.fisco.bcos.channel.handler.AMOPVerifyTopicToKeyInfo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WeEventFileClient implements IWeEventFileClient {

    private static final String PATH_SEPARATOR = "/";
    private final String groupId;
    private String localReceivePath = "";
    private FiscoConfig config;
    private FileTransportService fileTransportService;
    private int fileChunkSize;
    private FtpInfo ftpInfo;

    public class FtpInfo {
        private String host;
        private int port;
        private String userName;
        private String passWord;
        private String ftpReceivePath;

        public FtpInfo(String host, int port, String userName, String passWord, String ftpReceivePath) {
            this.host = host;
            this.port = port;
            this.userName = userName;
            this.passWord = passWord;
            this.ftpReceivePath = ftpReceivePath;
        }
    }

    public WeEventFileClient(String groupId, String localReceivePath, int fileChunkSize, FiscoConfig fiscoConfig) {
        this.groupId = groupId;
        this.localReceivePath = localReceivePath;
        this.fileChunkSize = fileChunkSize;
        this.config = fiscoConfig;
        init();
    }

    public WeEventFileClient(String groupId, String localReceivePath, String host, int port, String userName, String passWord, String ftpReceivePath, int fileChunkSize, FiscoConfig fiscoConfig) {
        this.groupId = groupId;
        this.localReceivePath = localReceivePath;
        this.ftpInfo = new FtpInfo(host, port, userName, passWord, ftpReceivePath);
        this.fileChunkSize = fileChunkSize;
        this.config = fiscoConfig;
        init();
    }

    public void init() {
        try {
            // create fisco instance
            FiscoBcosInstance fiscoBcosInstance = new FiscoBcosInstance(this.config);

            // create producer
            IProducer iProducer = fiscoBcosInstance.buildProducer();
            iProducer.startProducer();

            // create consumer
            IConsumer iConsumer = fiscoBcosInstance.buildConsumer();
            iConsumer.startConsumer();

            // create FileTransportService instance
            FileTransportService fileTransportService = new FileTransportService(this.config, iProducer, "", this.localReceivePath, this.fileChunkSize, this.groupId);
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
        File file = new File(publicPemPath);
        if (!file.isFile()) {
            log.error("public key file path string isn't a file.");
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST);
        }
        InputStream inputStream = new FileInputStream(file);

        openTransport4Sender(topic, inputStream);
    }

    /**
     * Publish a file to topic.
     * The file's data DO NOT stored in block chain. Yes, it's not persist, may be deleted sometime after subscribe notify.
     *
     * @param topic binding topic
     * @param filePath local file to be send
     * @return send result, SendResult.SUCCESS if success, and return SendResult.eventId
     * @throws BrokerException broker exception
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    @Override
    public FileChunksMeta publishFile(String topic, String filePath, boolean overwrite) throws BrokerException, IOException, InterruptedException {

        if (this.ftpInfo == null) {
            // publish local file
            validateLocalFile(filePath);

            FileChunksTransport fileChunksTransport = new FileChunksTransport(this.fileTransportService);
            FileChunksMeta fileChunksMeta = fileChunksTransport.upload(filePath, topic, this.groupId, overwrite);
            return fileChunksMeta;
        } else {
            // publish ftp file
            FtpClientService ftpClientService = new FtpClientService();
            ftpClientService.connect(this.ftpInfo.host, this.ftpInfo.port, this.ftpInfo.userName, this.ftpInfo.passWord);
            ftpClientService.downLoadFile(filePath, this.localReceivePath);

            FileChunksTransport fileChunksTransport = new FileChunksTransport(this.fileTransportService);
            FileChunksMeta fileChunksMeta = fileChunksTransport.upload(this.localReceivePath + filePath.substring(filePath.indexOf('/')), topic, this.groupId, overwrite);
            return fileChunksMeta;
        }
    }

    /**
     * @param topic topic name
     * @param fileListener notify interface
     * @throws BrokerException broker exception
     */
    public void openTransport4Receiver(String topic, FileListener fileListener) throws BrokerException {
        AMOPChannel amopChannel = this.fileTransportService.getChannel();

        FileEventListener fileEventListener = new FileEventListener(this.localReceivePath, this.ftpInfo, fileListener);

        amopChannel.subTopic(topic, fileEventListener);
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

        FileEventListener fileEventListener = new FileEventListener(this.localReceivePath, this.ftpInfo, fileListener);

        amopChannel.subTopic(topic, groupId, privatePem, fileEventListener);
    }

    /**
     * @param topic topic name
     * @param fileListener notify interface
     * @param privatePemPath private key pem path string
     * @throws IOException IOException
     * @throws BrokerException InterruptedException
     */
    public void openTransport4Receiver(String topic, FileListener fileListener, String privatePemPath) throws IOException, BrokerException {
        File file = new File(privatePemPath);
        if (!file.isFile()) {
            log.error("private key file path string isn't a file.");
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST);
        }
        InputStream inputStream = new FileInputStream(file);

        openTransport4Receiver(topic, fileListener, inputStream);
    }

    /**
     * @param topic topic name
     */
    public void closeTransport(String topic) {
        AMOPChannel channel = this.fileTransportService.getChannel();
        // unSubscribe topic
        channel.unSubTopic(topic);

        // delete transport
        channel.deleteTransport(topic);
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
            return null;
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
        receiverFileChunksMetaStatusList = fileTransportStats.getReceiver().get(groupId).get(topicName);
        receiverTopicStatusMap.put(topicName, receiverFileChunksMetaStatusList);
        Map<String, Map<String, List<FileChunksMetaStatus>>> receiver = new HashMap<>();
        receiver.put(groupId, receiverTopicStatusMap);

        FileTransportStats retFileTransportStats = new FileTransportStats();
        retFileTransportStats.setSender(sender);
        retFileTransportStats.setReceiver(receiver);

        return retFileTransportStats;
    }

    /**
     * @param topic topic name
     * @return filechunksmeta  filechunksmeta
     * @throws BrokerException broker exception
     */
    public List<FileChunksMeta> listFiles(String topic) throws BrokerException {
        // get json from disk
        List<File> fileList = new ArrayList<>();
        File file = new File(this.localReceivePath);
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isFile() && f.getName().endsWith(".json")) {
                fileList.add(f);
            }
        }

        List<FileChunksMeta> fileChunksMetaList = new ArrayList<>();
        DiskFiles diskFiles = new DiskFiles(this.localReceivePath);
        for (File f : fileList) {
            FileChunksMeta fileChunksMeta = diskFiles.loadFileMeta(f);
            if (fileChunksMeta.getTopic().equals(topic)) {
                fileChunksMetaList.add(fileChunksMeta);
            }
        }

        return fileChunksMetaList;
    }

    /**
     * @param fileChunksMeta fileChunksMeta
     * @return send result and eventId
     * @throws BrokerException broker exception
     */
    public SendResult sign(FileChunksMeta fileChunksMeta) throws BrokerException {
        return this.fileTransportService.sendSign(fileChunksMeta);
    }

    /**
     * @param eventId eventId return by sign
     * @param groupId group id
     * @return file and block information
     * @throws BrokerException broker exception
     */
    public FileChunksMetaPlus verify(String eventId, String groupId) throws BrokerException {
        return this.fileTransportService.verify(eventId, groupId);

    }

    private static void validateLocalFile(String filePath) throws BrokerException {
        if (StringUtils.isBlank(filePath)) {
            throw new BrokerException(ErrorCode.LOCAL_FILE_IS_EMPTY);
        }
        if (!(new File(filePath)).exists()) {
            throw new BrokerException(ErrorCode.LOCAL_FILE_NOT_EXIST);
        }
    }

    static class FileEventListener implements EventListener{
        private String receivePath;
        private final FtpInfo ftpInfo;
        private final FileListener fileListener;

        public FileEventListener(String receivePath, FtpInfo ftpInfo, FileListener fileListener) {
            this.receivePath = receivePath;
            this.ftpInfo = ftpInfo;
            this.fileListener = fileListener;
        }

        @Override
        public void onEvent(String topic, String fileName) {
            // upload file to ftp
            if (this.ftpInfo != null) {
                try {
                    FtpClientService ftpClientService = new FtpClientService();
                    ftpClientService.connect(this.ftpInfo.host, this.ftpInfo.port, this.ftpInfo.userName, this.ftpInfo.passWord);
                    if (StringUtils.isBlank(this.ftpInfo.ftpReceivePath)) {
                        log.info("upload file to ftp server, file：{}", fileName);
                        ftpClientService.upLoadFile(this.receivePath + PATH_SEPARATOR + fileName);
                    } else {
                        // specify upload directory
                        log.info("upload file to ftp server, to path: {}, file：{}", this.ftpInfo.ftpReceivePath, fileName);
                        ftpClientService.upLoadFile(this.ftpInfo.ftpReceivePath, this.receivePath + PATH_SEPARATOR + fileName);
                    }

                } catch (BrokerException e) {
                    e.printStackTrace();
                }
            }
            fileListener.onFile(topic, fileName);
        }

        @Override
        public void onException(Throwable e) {
            this.fileListener.onException(e);
        }
    }
}
