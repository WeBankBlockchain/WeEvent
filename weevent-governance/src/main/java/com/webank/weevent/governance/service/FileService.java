package com.webank.weevent.governance.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.core.config.FiscoConfig;
import com.webank.weevent.file.IWeEventFileClient;
import com.webank.weevent.file.dto.FileChunksMetaStatus;
import com.webank.weevent.file.dto.FileTransportStats;
import com.webank.weevent.file.inner.DiskFiles;
import com.webank.weevent.file.service.FileChunksMeta;
import com.webank.weevent.governance.GovernanceApplication;
import com.webank.weevent.governance.common.ConstantProperties;
import com.webank.weevent.governance.common.ErrorCode;
import com.webank.weevent.governance.common.GovernanceException;
import com.webank.weevent.governance.common.GovernanceResult;
import com.webank.weevent.governance.entity.FileTransportEntity;
import com.webank.weevent.governance.entity.UploadChunkParam;
import com.webank.weevent.governance.repository.TransportRepository;
import com.webank.weevent.governance.utils.ParamCheckUtils;
import com.webank.weevent.governance.utils.Utils;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * file upload/download Service.
 *
 * @author v_wbhwliu
 * @version 1.3
 * @since 2020/5/20
 */
@Service
@Slf4j
public class FileService {

    private TransportRepository transportRepository;
    // file upload root path
    private String uploadPath;
    // file download root path
    private String downloadPath;
    // <brokerId, <groupId, <IWeEventFileClient, DiskFiles>>>
    private Map<Integer, Map<String, Pair<IWeEventFileClient, DiskFiles>>> fileClientMap = new ConcurrentHashMap<>();
    // <brokerId, <groupId, <topic, overwrite>>>
    private Map<Integer, Map<String, Map<String, Boolean>>> transportMap = new ConcurrentHashMap<>();
    // upload local file to governance server, <fileId, FileChunksMeta>
    private Map<String, Pair<FileChunksMeta, DiskFiles>> fileChunksMap = new ConcurrentHashMap<>();


    @Autowired
    public void setTransportRepository(TransportRepository transportRepository) throws GovernanceException {
        this.transportRepository = transportRepository;
        this.uploadPath = GovernanceApplication.governanceConfig.getFileTransportPath() + File.separator + ConstantProperties.UPLOAD;
        this.downloadPath = GovernanceApplication.governanceConfig.getFileTransportPath() + File.separator + ConstantProperties.DOWNLOAD;
        this.initFileTransportBasePath();
        this.syncTransportToCache(this.transportRepository.findAll());
    }

    public GovernanceResult openTransport(FileTransportEntity fileTransport) throws GovernanceException {
        ParamCheckUtils.validateTransportName(fileTransport.getTopicName());
        ParamCheckUtils.validateTransportRole(fileTransport.getRole());
        ParamCheckUtils.validateOverWrite(fileTransport.getOverWrite());
        this.checkTransportExist(fileTransport.getBrokerId(), fileTransport.getGroupId(), fileTransport.getTopicName());

        if (ConstantProperties.TRANSPORT_RECEIVER.equals(fileTransport.getRole())) {
            this.openTransport4Receiver(fileTransport);
        } else {
            this.openTransport4Sender(fileTransport);
        }

        this.transportRepository.save(fileTransport);
        log.info("open transport success, topic:{}, role:{}", fileTransport.getTopicName(), fileTransport.getRole());
        return GovernanceResult.ok(true);
    }

    private void openTransport4Sender(FileTransportEntity fileTransport) throws GovernanceException {
        IWeEventFileClient fileClient;
        try {
            fileClient = this.buildIWeEventFileClient(fileTransport.getGroupId(), fileTransport.getBrokerId());

            if (StringUtils.isBlank(fileTransport.getPublicKey())) {
                fileClient.openTransport4Sender(fileTransport.getTopicName());
            } else {
                fileClient.openTransport4Sender(fileTransport.getTopicName(),
                        new ByteArrayInputStream(fileTransport.getPublicKey().getBytes(StandardCharsets.UTF_8)));
            }
        } catch (BrokerException e) {
            log.error("open sender transport failed.", e);
            this.fileClientMap.remove(fileTransport.getBrokerId());
            throw new GovernanceException(e.getMessage());
        }

        this.addIWeEventClientToCache(fileTransport.getBrokerId(), fileTransport.getGroupId(), fileClient);
        this.addTransportToCache(fileTransport.getBrokerId(), fileTransport.getGroupId(), fileTransport.getTopicName(),
                fileTransport.getOverWrite());
        log.info("open sender transport success, groupId:{}, topic:{}", fileTransport.getGroupId(), fileTransport.getTopicName());
    }

    private void openTransport4Receiver(FileTransportEntity fileTransport) throws GovernanceException {
        IWeEventFileClient fileClient;
        try {
            fileClient = buildIWeEventFileClient(fileTransport.getGroupId(), fileTransport.getBrokerId());
            if (StringUtils.isBlank(fileTransport.getPrivateKey())) {
                fileClient.openTransport4Receiver(fileTransport.getTopicName(), new IWeEventFileClient.FileListener() {
                    @Override
                    public void onFile(String topicName, String fileName) {
                        log.info("receive file：{} from topic: {}.", fileName, topicName);
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("onException:", e);
                    }
                });
            } else {
                fileClient.openTransport4Receiver(fileTransport.getTopicName(), new IWeEventFileClient.FileListener() {
                    @Override
                    public void onFile(String topicName, String fileName) {
                        log.info("receive file：{} from topic: {}.", fileName, topicName);
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("onException:", e);
                    }
                }, new ByteArrayInputStream(fileTransport.getPrivateKey().getBytes(StandardCharsets.UTF_8)));
            }
        } catch (BrokerException e) {
            log.error("open receive transport failed.", e);
            this.fileClientMap.remove(fileTransport.getBrokerId());
            throw new GovernanceException(e.getMessage());
        }

        this.addIWeEventClientToCache(fileTransport.getBrokerId(), fileTransport.getGroupId(), fileClient);
        this.addTransportToCache(fileTransport.getBrokerId(), fileTransport.getGroupId(), fileTransport.getTopicName(),
                fileTransport.getOverWrite());
        log.info("open receiver transport success, groupId:{}, topic:{}", fileTransport.getGroupId(), fileTransport.getTopicName());
    }

    public GovernanceResult uploadFile(HttpServletRequest request) throws GovernanceException {
        UploadChunkParam chunkParam = parseUploadChunkRequest(request);
        log.info("upload chunk:{}", chunkParam);

        IWeEventFileClient fileClient = this.getIWeEventFileClient(chunkParam.getFileChunksMeta().getGroupId(), chunkParam.getBrokerId());
        boolean isSuccess = this.uploadChunks(chunkParam.getFileChunksMeta(), chunkParam.getChunkNumber(), chunkParam.getChunkData());

        try {
            // if chunk upload failed, sleep and retry again
            for (int i = 1; i <= ConstantProperties.UPLOAD_CHUNK_FAIL_RETRY_COUNT; i++) {
                if (isSuccess) {
                    break;
                }
                Thread.sleep(ConstantProperties.WAIT1S);
                isSuccess = this.uploadChunks(chunkParam.getFileChunksMeta(), chunkParam.getChunkNumber(), chunkParam.getChunkData());
            }
        } catch (InterruptedException e) {
            log.error("upload file failed, topic:{}, fileId:{}.", chunkParam.getFileChunksMeta().getTopic(), chunkParam.getFileId());
            throw new GovernanceException(e.getMessage());
        }
        if (!isSuccess) {
            throw new GovernanceException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        if (chunkParam.getFileChunksMeta().checkChunkFull()) {
            CompletableFuture.runAsync(() -> {
                String fileId = chunkParam.getFileChunksMeta().getFileId();
                String filePath = this.uploadPath.concat(File.separator).concat(fileId).concat(File.separator).concat(chunkParam
                        .getFileChunksMeta().getTopic()).concat(File.separator).concat(chunkParam.getFileChunksMeta().getFileName());
                boolean overWrite = this.transportMap.get(chunkParam.getBrokerId()).get(chunkParam.getFileChunksMeta().getGroupId())
                        .get(chunkParam.getFileChunksMeta().getTopic());

                log.info("all chunks has uploaded success, start publish file, filePath:{}", filePath);
                try {
                    fileClient.publishFile(chunkParam.getFileChunksMeta().getTopic(), filePath, overWrite);
                    log.info("publish file success, topic:{}, fileName:{}.", chunkParam.getFileChunksMeta().getTopic(),
                            chunkParam.getFileChunksMeta().getFileName());
                } catch (BrokerException | IOException | InterruptedException e) {
                    log.error("publish file error, fileName:{}.", chunkParam.getFileChunksMeta().getFileName(), e);
                } finally {
                    // remove local file after publish
                    Utils.removeLocalFile(this.uploadPath.concat(File.separator).concat(fileId));
                    this.fileChunksMap.remove(fileId);
                    log.info("remove local file after publish, fileName:topic:{}, fileName:{}.",
                            chunkParam.getFileChunksMeta().getTopic(), chunkParam.getFileChunksMeta().getFileName());
                }
            });
        }

        return GovernanceResult.ok(true);
    }

    private UploadChunkParam parseUploadChunkRequest(HttpServletRequest request) throws GovernanceException {
        UploadChunkParam chunkParam = new UploadChunkParam();
        String fileId = request.getParameter("identifier");
        FileChunksMeta fileChunksMeta = this.fileChunksMap.get(fileId).getKey();

        chunkParam.setFileChunksMeta(fileChunksMeta);
        chunkParam.setBrokerId(Integer.parseInt(request.getParameter("brokerId")));
        chunkParam.setChunkNumber(Integer.parseInt(request.getParameter("chunkNumber")) - 1);
        chunkParam.setFileId(fileId);

        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        if (multipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                MultipartFile file = multiRequest.getFile(iter.next());
                if (!Objects.isNull(file)) {
                    try {
                        chunkParam.setChunkData(file.getBytes());
                    } catch (IOException e) {
                        log.error("parse upload chunk data error.", e);
                        throw new GovernanceException(ErrorCode.PARSE_CHUNK_REQUEST_ERROR);
                    }
                }
            }
        }
        return chunkParam;
    }

    public String downloadFile(String groupId, Integer brokerId, String fileId) {
        DiskFiles diskFiles = this.getDiskFiles(groupId, brokerId);
        return diskFiles.genLocalFileName(fileId);
    }

    public GovernanceResult listFile(String groupId, Integer brokerId, String topic) throws GovernanceException {
        IWeEventFileClient fileClient = getIWeEventFileClient(groupId, brokerId);
        try {
            List<FileChunksMeta> fileChunksMetas = fileClient.listFiles(topic);
            return GovernanceResult.ok(fileChunksMetas);
        } catch (BrokerException e) {
            log.error("list file error, topic:{}", topic);
            throw new GovernanceException(e.getMessage());
        }
    }

    public GovernanceResult status(String groupId, Integer brokerId, String topic, String role) throws GovernanceException {
        ParamCheckUtils.validateTransportRole(role);
        List<FileChunksMetaStatus> fileChunksMetaStatusList = null;
        IWeEventFileClient fileClient = this.getIWeEventFileClient(groupId, brokerId);
        FileTransportStats status = fileClient.status(topic);
        if (ConstantProperties.TRANSPORT_RECEIVER.equals(role)) {
            if (status.getReceiver().containsKey(groupId)) {
                fileChunksMetaStatusList = status.getReceiver().get(groupId).get(topic);
            }
        } else {
            if (status.getSender().containsKey(groupId)) {
                fileChunksMetaStatusList = status.getSender().get(groupId).get(topic);
            }
        }
        return GovernanceResult.ok(fileChunksMetaStatusList);
    }

    public GovernanceResult listTransport(String groupId, Integer brokerId) {
        List<FileTransportEntity> fileTransportList = this.transportRepository.queryByBrokerIdAndGroupId(brokerId, groupId);
        fileTransportList.forEach(fileTransport -> {
            fileTransport.setCreateTime(Utils.dateToStr(fileTransport.getCreateDate()));
            if (StringUtils.isNotBlank(fileTransport.getPublicKey()) || StringUtils.isNotBlank(fileTransport.getPrivateKey())) {
                fileTransport.setVerified(true);
            }

        });
        log.info("get transport list success, transport.size:{}", fileTransportList.size());
        return GovernanceResult.ok(fileTransportList);
    }

    public GovernanceResult closeTransport(FileTransportEntity fileTransport) throws GovernanceException {
        IWeEventFileClient fileClient = this.getIWeEventFileClient(fileTransport.getGroupId(), fileTransport.getBrokerId());
        fileClient.closeTransport(fileTransport.getTopicName());

        this.transportRepository.delete(fileTransport);
        this.removeTransportCache(fileTransport.getBrokerId(), fileTransport.getGroupId(), fileTransport.getTopicName());
        return GovernanceResult.ok(true);
    }

    public GovernanceResult prepareUploadFile(String fileId, String filename, String topic, String groupId, long totalSize,
                                              Integer chunkSize) throws GovernanceException {

        if (this.fileChunksMap.containsKey(fileId)) {
            FileChunksMeta fileChunksMeta = this.fileChunksMap.get(fileId).getKey();
            return GovernanceResult.ok(this.chunkUploadedList(fileChunksMeta));
        }

        FileChunksMeta fileChunksMeta = new FileChunksMeta(fileId, filename, totalSize, "", topic, groupId, true);
        fileChunksMeta.initChunkSize(chunkSize);
        DiskFiles diskFiles = new DiskFiles(this.uploadPath + File.separator + fileId);
        try {
            diskFiles.createFixedLengthFile(fileChunksMeta);
            diskFiles.saveFileMeta(fileChunksMeta);
        } catch (BrokerException e) {
            log.error("create fileChunksMeta error.fileName:{}, fileId:{}.", filename, fileId, e);
            throw new GovernanceException(e.getMessage());
        }
        this.fileChunksMap.put(fileId, new Pair<>(fileChunksMeta, diskFiles));

        return GovernanceResult.ok(chunkUploadedList(fileChunksMeta));
    }

    private IWeEventFileClient buildIWeEventFileClient(String groupId, Integer brokerId) {
        if (!this.fileClientMap.containsKey(brokerId) || !this.fileClientMap.get(brokerId).containsKey(groupId)) {
            FiscoConfig fiscoConfig = new FiscoConfig();
            fiscoConfig.load("");

            IWeEventFileClient fileClient = IWeEventFileClient.build(groupId, this.downloadPath, ConstantProperties.FILE_CHUNK_SIZE, fiscoConfig);
            Map<String, Pair<IWeEventFileClient, DiskFiles>> fileClientOfEachGroupMap = new ConcurrentHashMap<>();
            fileClientOfEachGroupMap.put(groupId, new Pair<>(fileClient, fileClient.getDiskFiles()));
            this.fileClientMap.put(brokerId, fileClientOfEachGroupMap);
        }

        return this.fileClientMap.get(brokerId).get(groupId).getKey();
    }

    private IWeEventFileClient getIWeEventFileClient(String groupId, Integer brokerId) throws GovernanceException {
        if (!this.fileClientMap.containsKey(brokerId) || !this.fileClientMap.get(brokerId).containsKey(groupId)) {
            throw new GovernanceException(ErrorCode.TRANSPORT_NOT_EXISTS);
        }
        return this.fileClientMap.get(brokerId).get(groupId).getKey();
    }

    private DiskFiles getDiskFiles(String groupId, Integer brokerId) {
        return this.fileClientMap.get(brokerId).get(groupId).getValue();
    }

    private boolean uploadChunks(FileChunksMeta fileChunksMeta, Integer chunkIdx, byte[] chunkData) {
        DiskFiles diskFiles = this.fileChunksMap.get(fileChunksMeta.getFileId()).getValue();
        try {
            diskFiles.writeChunkData(fileChunksMeta.getFileId(), chunkIdx, chunkData);
            this.fileChunksMap.get(fileChunksMeta.getFileId()).getKey().getChunkStatus().set(chunkIdx);
            log.info("upload file chunk data success, {}@{}.", fileChunksMeta.getFileId(), chunkIdx);
        } catch (BrokerException e) {
            log.error("write chunk data error, topic:{}, {}@{}", fileChunksMeta.getTopic(), fileChunksMeta.getFileId(), chunkIdx, e);
            return false;
        }
        return true;
    }

    private void checkTransportExist(Integer brokerId, String groupId, String topic) throws GovernanceException {
        if (this.transportMap.containsKey(brokerId)
                && this.transportMap.get(brokerId).containsKey(groupId)
                && this.transportMap.get(brokerId).get(groupId).containsKey(topic)) {
            throw new GovernanceException(ErrorCode.TRANSPORT_ALREADY_EXISTS);
        }
    }

    private void syncTransportToCache(List<FileTransportEntity> transportList) throws GovernanceException {
        for (FileTransportEntity fileTransport : transportList) {
            try {
                checkTransportExist(fileTransport.getBrokerId(), fileTransport.getGroupId(), fileTransport.getTopicName());
                if (ConstantProperties.TRANSPORT_RECEIVER.equals(fileTransport.getRole())) {
                    this.openTransport4Receiver(fileTransport);
                } else {
                    this.openTransport4Sender(fileTransport);
                }
            } catch (GovernanceException e) {
                if (e.getCode() != ErrorCode.TRANSPORT_ALREADY_EXISTS.getCode()) {
                    throw e;
                }
            }
        }
        log.info("synchronize transport from db to local cache success.");
    }

    private void initFileTransportBasePath() {
        File uploadFile = new File(this.uploadPath);
        if (!uploadFile.exists()) {
            uploadFile.mkdir();
        }
        File downloadFile = new File(this.downloadPath);
        if (!downloadFile.exists()) {
            downloadFile.mkdir();
        }
    }

    private void addIWeEventClientToCache(Integer brokerId, String groupId, IWeEventFileClient fileClient) {
        Map<String, Pair<IWeEventFileClient, DiskFiles>> clientMap = this.fileClientMap.get(brokerId);
        if (Objects.isNull(clientMap)) {
            clientMap = new ConcurrentHashMap<>();
        }
        clientMap.put(groupId, new Pair<>(fileClient, fileClient.getDiskFiles()));
        this.fileClientMap.put(brokerId, clientMap);
    }

    private void addTransportToCache(Integer brokerId, String groupId, String topic, String overwrite) {
        // 0 means false, 1 means true;
        boolean isOverWrite = "1".equals(overwrite);
        if (this.transportMap.isEmpty()) {
            Map<String, Map<String, Boolean>> group2TransportMap = new ConcurrentHashMap<>();
            Map<String, Boolean> transport2OverWriteMap = new HashMap<>();
            transport2OverWriteMap.put(topic, isOverWrite);
            group2TransportMap.put(groupId, transport2OverWriteMap);
            this.transportMap.put(brokerId, group2TransportMap);
            return;
        }
        if (!this.transportMap.containsKey(brokerId)) {
            Map<String, Map<String, Boolean>> group2TransportMap = new ConcurrentHashMap<>();
            Map<String, Boolean> transport2OverWriteMap = new HashMap<>();
            transport2OverWriteMap.put(topic, isOverWrite);
            group2TransportMap.put(groupId, transport2OverWriteMap);
            this.transportMap.put(brokerId, group2TransportMap);
            return;
        }
        if (!this.transportMap.get(brokerId).containsKey(groupId)) {
            Map<String, Boolean> transport2OverWriteMap = new HashMap<>();
            transport2OverWriteMap.put(topic, isOverWrite);
            this.transportMap.get(brokerId).put(groupId, transport2OverWriteMap);
        } else {
            this.transportMap.get(brokerId).get(groupId).put(topic, isOverWrite);
        }
    }

    private void removeTransportCache(Integer brokerId, String groupId, String topic) {
        if (Objects.isNull(this.transportMap.get(brokerId)) || Objects.isNull(this.transportMap.get(brokerId).get(groupId))) {
            log.error("transport:{} not exists in local cache.", topic);
        } else {
            this.transportMap.get(brokerId).get(groupId).remove(topic);
        }
    }

    private List<Integer> chunkUploadedList(FileChunksMeta fileChunksMeta) {
        List<Integer> uploadedChunks = new ArrayList<>();
        for (int i = 0; i < fileChunksMeta.getChunkNum(); i++) {
            if (fileChunksMeta.getChunkStatus().get(i)) {
                uploadedChunks.add(i + 1);
            }
        }
        return uploadedChunks;
    }

}
