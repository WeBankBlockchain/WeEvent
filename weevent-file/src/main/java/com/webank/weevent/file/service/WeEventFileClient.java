package com.webank.weevent.file.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import com.webank.weevent.file.ftpclient.FtpInfo;
import com.webank.weevent.file.inner.AMOPChannel;
import com.webank.weevent.file.inner.DiskFiles;
import com.webank.weevent.file.inner.FileTransportService;
import com.webank.weevent.file.inner.PemFile;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.channel.handler.AMOPVerifyKeyInfo;
import org.fisco.bcos.channel.handler.AMOPVerifyTopicToKeyInfo;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

@Slf4j
public class WeEventFileClient implements IWeEventFileClient {

    private static final String PATH_SEPARATOR = "/";
    private static final String PRIVATE_KEY_SUFFIX = ".pem";
    private static final String PUBLIC_KEY_SUFFIX = ".pub.pem";
    private static final String HEX_HEADER = "0x";
    private static final String PRIVATE_KEY_DESC = "PRIVATE KEY";
    private static final String PUBLIC_KEY_DESC = "PUBLIC KEY";
    private static final String ALGORITHM = "ECDSA";
    private static final String CURVE_TYPE = "SECP256k1";

    private final String groupId;
    private String localReceivePath = "";
    private FiscoConfig config;
    private FileTransportService fileTransportService;
    private int fileChunkSize;
    private FtpInfo ftpInfo;

    public WeEventFileClient(String groupId, String localReceivePath, int fileChunkSize, FiscoConfig fiscoConfig) {
        this.groupId = groupId;
        this.localReceivePath = localReceivePath;
        this.fileChunkSize = fileChunkSize;
        this.config = fiscoConfig;
        init();
    }

    public WeEventFileClient(String groupId, String localReceivePath, FtpInfo ftpInfo, int fileChunkSize, FiscoConfig fiscoConfig) {
        this.groupId = groupId;
        this.localReceivePath = localReceivePath;
        this.ftpInfo = ftpInfo;
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

    public void openTransport4Sender(String topic) {
        if (!this.fileTransportService.getChannel().senderTopics.contains(topic)) {
            this.fileTransportService.getChannel().senderTopics.add(topic);
        }
    }

    public void openTransport4Sender(String topic, InputStream publicPem) throws BrokerException {
        // publicPem is public key
        BufferedInputStream bufferedInputStream = new BufferedInputStream(publicPem);
        try {
            if (publicPem == null) {
                log.error("public key pem inputstream is null.");
                throw new BrokerException(ErrorCode.PARAM_ISNULL);
            }
            bufferedInputStream.mark(bufferedInputStream.available() + 1);
            String publicKey = IOUtils.toString(bufferedInputStream, StandardCharsets.UTF_8);
            if (!publicKey.contains(PUBLIC_KEY_DESC)) {
                log.error("inputStream is not a public key.");
                throw new BrokerException(ErrorCode.FILE_PEM_KEY_INVALID);
            }
            bufferedInputStream.reset();
        } catch (IOException e) {
            log.error("public key inputStream is invalid.");
            throw new BrokerException(ErrorCode.FILE_PEM_KEY_INVALID);
        }

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
        InputStreamResource inputStreamResource = new InputStreamResource(bufferedInputStream);
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

    public void openTransport4Sender(String topic, String publicPemPath) throws BrokerException, IOException {
        if (StringUtils.isBlank(publicPemPath)) {
            log.error("public key pem path is blank.");
            throw new BrokerException(ErrorCode.PARAM_ISNULL);
        }
        File file = new File(publicPemPath);
        if (!file.isFile()) {
            log.error("public key file path string isn't a file.");
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST);
        }
        InputStream inputStream = new FileInputStream(file);

        openTransport4Sender(topic, inputStream);
    }

    @Override
    public FileChunksMeta publishFile(String topic, String filePath, boolean overwrite) throws BrokerException, IOException {

        if (this.ftpInfo == null) {
            // publish local file
            validateLocalFile(filePath);

            // check if topic is exist
            IProducer iProducer = this.fileTransportService.getProducer();
            if (!iProducer.exist(topic, this.groupId)) {
                log.info("topic: " + topic + " not exist in group: " + groupId + ", open topic: " + topic + " groupID: " + groupId);
                iProducer.open(topic, this.groupId);
            }

            FileChunksTransport fileChunksTransport = new FileChunksTransport(this.fileTransportService);
            FileChunksMeta fileChunksMeta = fileChunksTransport.upload(filePath, topic, this.groupId, overwrite);
            return fileChunksMeta;
        } else {
            // publish ftp file
            FtpClientService ftpClientService = new FtpClientService();
            ftpClientService.connect(this.ftpInfo.getHost(), this.ftpInfo.getPort(), this.ftpInfo.getUserName(), this.ftpInfo.getPassWord());
            ftpClientService.downLoadFile(filePath, this.localReceivePath);

            FileChunksTransport fileChunksTransport = new FileChunksTransport(this.fileTransportService);
            FileChunksMeta fileChunksMeta = fileChunksTransport.upload(this.localReceivePath + filePath.substring(filePath.indexOf('/')), topic, this.groupId, overwrite);
            return fileChunksMeta;
        }
    }

    public void openTransport4Receiver(String topic, FileListener fileListener) throws BrokerException {
        AMOPChannel amopChannel = this.fileTransportService.getChannel();

        FileEventListener fileEventListener = new FileEventListener(this.localReceivePath, this.ftpInfo, fileListener);

        amopChannel.subTopic(topic, fileEventListener);
    }

    public void openTransport4Receiver(String topic, FileListener fileListener, InputStream privatePem) throws BrokerException {
        // privatePem is private  key
        BufferedInputStream bufferedInputStream = new BufferedInputStream(privatePem);
        try {
            bufferedInputStream.mark(bufferedInputStream.available() + 1);
            String publicKey = IOUtils.toString(bufferedInputStream, StandardCharsets.UTF_8);
            if (!publicKey.contains(PRIVATE_KEY_DESC)) {
                log.error("inputStream is not a private key.");
                throw new BrokerException(ErrorCode.FILE_PEM_KEY_INVALID);
            }
            bufferedInputStream.reset();
        } catch (IOException e) {
            log.error("private key inputStream is invalid.");
            throw new BrokerException(ErrorCode.FILE_PEM_KEY_INVALID);
        }

        // get AMOPChannel, fileTransportService and amopChannel is One-to-one correspondence
        AMOPChannel amopChannel = this.fileTransportService.getChannel();

        FileEventListener fileEventListener = new FileEventListener(this.localReceivePath, this.ftpInfo, fileListener);

        amopChannel.subTopic(topic, groupId, bufferedInputStream, fileEventListener);
    }

    public void openTransport4Receiver(String topic, FileListener fileListener, String privatePemPath) throws IOException, BrokerException {
        if (StringUtils.isBlank(privatePemPath)) {
            log.error("private key pem path is blank.");
            throw new BrokerException(ErrorCode.PARAM_ISNULL);
        }
        File file = new File(privatePemPath);
        if (!file.isFile()) {
            log.error("private key file path string isn't a file.");
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST);
        }
        InputStream inputStream = new FileInputStream(file);

        openTransport4Receiver(topic, fileListener, inputStream);
    }

    public void closeTransport(String topic) {
        AMOPChannel channel = this.fileTransportService.getChannel();
        // unSubscribe topic
        if (!channel.getSubTopics().isEmpty()) {
            channel.unSubTopic(topic);
        } else {
            if (!channel.getSenderTopics().isEmpty()) {
                // delete transport
                channel.deleteTransport(topic);
            }
        }
    }

    @Override
    public FileTransportStats status(String topicName) {
        FileTransportStats fileTransportStats = this.fileTransportService.stats(true, this.groupId, topicName);
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

    public List<FileChunksMeta> listFiles(String topic) throws BrokerException {
        // get json from disk
        List<File> fileList = new ArrayList<>();
        String filePath = this.localReceivePath + PATH_SEPARATOR + topic;
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".json")) {
                    fileList.add(f);
                }
            }
        } else {
            log.error("the directory is empty, {}.", filePath);
            throw new BrokerException(ErrorCode.FILE_NOT_EXIST);
        }

        List<FileChunksMeta> fileChunksMetaList = new ArrayList<>();
        DiskFiles diskFiles = new DiskFiles(filePath);
        for (File f : fileList) {
            FileChunksMeta fileChunksMeta = diskFiles.loadFileMeta(f);
            if (fileChunksMeta.getTopic().equals(topic) && fileChunksMeta.checkChunkFull()) {
                fileChunksMetaList.add(fileChunksMeta);
            }
        }

        return fileChunksMetaList;
    }

    public SendResult sign(FileChunksMeta fileChunksMeta) throws BrokerException {
        return this.fileTransportService.sendSign(fileChunksMeta);
    }

    public FileChunksMetaPlus verify(String eventId, String groupId) throws BrokerException {
        return this.fileTransportService.verify(eventId, groupId);

    }

    public DiskFiles getDiskFiles() {
        return this.fileTransportService.getDiskFiles();
    }

    public Map<String, String> genPemFile(String filePath) throws BrokerException {
	validateLocalFile(filePath);
	try {
	     BouncyCastleProvider prov = new BouncyCastleProvider();
	     Security.addProvider(prov);
	     ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE_TYPE);
	     KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM, prov.getName());
	     generator.initialize(ecSpec, new SecureRandom());
	     KeyPair pair = generator.generateKeyPair();
	     String pubKey = pair.getPublic().toString();
	     String account = HEX_HEADER + pubKey.substring(pubKey.indexOf("[") + 1, pubKey.indexOf("]")).replace(":", "");

	     PemFile privatePemFile = new PemFile(pair.getPrivate(), PRIVATE_KEY_DESC);
	     PemFile publicPemFile = new PemFile(pair.getPublic(), PUBLIC_KEY_DESC);

	     String privateKeyUrl = filePath + PATH_SEPARATOR + account + PRIVATE_KEY_SUFFIX;
	     String publicKeyUrl = filePath + PATH_SEPARATOR + account + PUBLIC_KEY_SUFFIX;
			
	     privatePemFile.write(privateKeyUrl);
	     publicPemFile.write(publicKeyUrl);
			
	     Map<String, String> ppkUrlMap = new HashMap<>();
	     ppkUrlMap.put("privateKeyUrl", getFileKyeInfo(privateKeyUrl));
	     ppkUrlMap.put("publicKeyUrl", getFileKyeInfo(publicKeyUrl));
	     return ppkUrlMap;
	} catch (IOException | NoSuchProviderException | NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
	     log.error("generate pem file error", e);
	     throw new BrokerException(ErrorCode.FILE_GEN_PEM_BC_FAILED);
	}
    }
    
    private String getFileKyeInfo(String url) {
	StringBuffer sb = new StringBuffer();
	File file = new File(url);
	// 读取文件
	BufferedInputStream bis = null;
	FileInputStream fis = null;
	try {
	     // 第一步 通过文件路径来创建文件实例
	     fis = new FileInputStream(file);
	     bis = new BufferedInputStream(fis);
	     while (bis.available() > 0) {
	     	sb.append((char) bis.read());
	     }
	} catch (FileNotFoundException fnfe) {
	     log.error("file non-existent", fnfe);
	} catch (IOException ioe) {
	     log.error("I/O error", ioe);
	} finally {
	     try {
		 if (bis != null && fis != null) {
		     fis.close();
		     bis.close();
		 }
	     } catch (IOException ioe) {
		 log.error("close InputStream error", ioe);
             }
	}
	     file.delete();
	     return sb.toString();
    }

    public boolean isFileExist(String fileName, String topic, String groupId) throws BrokerException {
        return this.fileTransportService.getFileExistence(fileName, topic, groupId);
    }

    private static void validateLocalFile(String filePath) throws BrokerException {
        if (StringUtils.isBlank(filePath)) {
            throw new BrokerException(ErrorCode.LOCAL_FILE_IS_EMPTY);
        }
        if (!(new File(filePath)).exists()) {
            throw new BrokerException(ErrorCode.LOCAL_FILE_NOT_EXIST);
        }
    }

    /**
     * Interface for event notify callback
     */
    public interface EventListener {
        /**
         * check if file exists at ftp server.
         *
         * @param fileName file name
         * @return true if file exist.
         */
        boolean checkFile(String fileName);

        /**
         * Called while new event arrived.
         *
         * @param topic topic name
         * @param fileName file name
         */
        void onEvent(String topic, String fileName);

        /**
         * Called while raise exception.
         *
         * @param e the e
         */
        void onException(Throwable e);
    }

    static class FileEventListener implements EventListener {
        private final String receivePath;
        private final FtpInfo ftpInfo;
        private final FileListener fileListener;

        public FileEventListener(String receivePath, FtpInfo ftpInfo, FileListener fileListener) {
            this.receivePath = receivePath;
            this.ftpInfo = ftpInfo;
            this.fileListener = fileListener;
        }

        public boolean checkFile(String fileName) {
            boolean ret = false;
            if (this.ftpInfo != null) {
                try {
                    FtpClientService ftpClientService = new FtpClientService();
                    ftpClientService.connect(this.ftpInfo.getHost(), this.ftpInfo.getPort(), this.ftpInfo.getUserName(), this.ftpInfo.getPassWord());

                    // check file exist
                    if (StringUtils.isBlank(this.ftpInfo.getFtpReceivePath())) {
                        List<String> files = ftpClientService.getFileList("./");
                        for (String file : files) {
                            if (file.equals(fileName)) {
                                ret = true;
                                break;
                            }
                        }
                    } else {
                        List<String> files = ftpClientService.getFileList(this.ftpInfo.getFtpReceivePath());
                        for (String file : files) {
                            if (file.equals(fileName)) {
                                ret = true;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ret;
        }

        @Override
        public void onEvent(String topic, String fileName) {
            // upload file to ftp
            if (this.ftpInfo != null) {
                try {
                    FtpClientService ftpClientService = new FtpClientService();
                    ftpClientService.connect(this.ftpInfo.getHost(), this.ftpInfo.getPort(), this.ftpInfo.getUserName(), this.ftpInfo.getPassWord());
                    // upload file
                    if (StringUtils.isBlank(this.ftpInfo.getFtpReceivePath())) {
                        log.info("upload file to ftp server, file：{}", fileName);
                        ftpClientService.upLoadFile(this.receivePath + PATH_SEPARATOR + topic + PATH_SEPARATOR + fileName);
                    } else {
                        // specify upload directory
                        log.info("upload file to ftp server, to path: {}, file：{}", this.ftpInfo.getFtpReceivePath(), fileName);
                        ftpClientService.upLoadFile(this.ftpInfo.getFtpReceivePath(), this.receivePath + PATH_SEPARATOR + topic + PATH_SEPARATOR + fileName);
                    }
                } catch (BrokerException e) {
                    e.printStackTrace();
                }
            }
            fileListener.onFile(topic, fileName);
        }

        public void onException(Throwable e) {
            this.fileListener.onException(e);
        }
    }
}
