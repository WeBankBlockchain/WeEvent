package com.webank.weevent.protocol.rest;

import com.webank.weevent.broker.fisco.file.FileTransportReceiver;
import com.webank.weevent.broker.fisco.file.FileTransportSender;
import com.webank.weevent.broker.fisco.file.ZKChunksMeta;
import com.webank.weevent.broker.fisco.util.WeEventUtils;
import com.webank.weevent.broker.plugin.IProducer;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.FileChunksMeta;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author v_wbhwliu
 * @version 1.2
 * @since 2020/2/12
 */
@RequestMapping(value = "/file")
@RestController
@Slf4j
public class FileRest {
    private IProducer producer;
    private ZKChunksMeta zkChunksMeta;
    private FileTransportSender fileTransportSender;
    private FileTransportReceiver fileTransportReceiver;

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    @Autowired
    public void setZkChunksMeta(ZKChunksMeta zkChunksMeta) {
        this.zkChunksMeta = zkChunksMeta;
    }

    @Autowired
    public void setFileTransportSender(FileTransportSender fileTransportSender) {
        this.fileTransportSender = fileTransportSender;
    }

    @Autowired
    public void fileTransportReceiver(FileTransportReceiver fileTransportReceiver) {
        this.fileTransportReceiver = fileTransportReceiver;
    }

    @RequestMapping(path = "/createChunk")
    public FileChunksMeta createChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                      @RequestParam(name = "fileSize") long fileSize,
                                      @RequestParam(name = "md5") String md5) throws BrokerException {
        log.info("createChunk, groupId:{} md5:{}", groupId, md5);

        String fileId = WeEventUtils.generateUuid();

        // create AMOP channel with FileTransportSender
        this.fileTransportSender.openChannel(fileId);

        // create FileChunksMeta
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        fileChunksMeta.setFileId(fileId);

        // update to Zookeeper
        this.zkChunksMeta.addChunks(fileId, fileChunksMeta);

        return fileChunksMeta;
    }

    @RequestMapping(path = "/uploadChunk")
    public FileChunksMeta uploadChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                      @RequestParam(name = "fileId") String fileId,
                                      @RequestParam(name = "chunkIdx") int chunkIdx,
                                      @RequestParam(name = "chunkData") byte[] chunkData) throws BrokerException {
        log.info("uploadChunk, groupId:{}. fileId:{}. chunkIdx:{}", groupId, fileId, chunkIdx);

        /*
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        String uploadDirPath = BrokerApplication.weEventConfig.getBaseFilePath() + File.separator + fileId;
        String tempFileName = fileName + "_tmp";
        File uploadFile = new File(uploadDirPath);
        File tmpFile = new File(uploadDirPath, tempFileName);
        if (!uploadFile.exists()) {
            uploadFile.mkdirs();
        }

        try {
            RandomAccessFile accessTmpFile = new RandomAccessFile(tmpFile, "rw");
            // write chunk data
            long offset = chunkSize * chunkIdx;
            accessTmpFile.write(chunkData);
            accessTmpFile.close();
        } catch (IOException e) {
            log.error("upload chunk failed, fileId:{}, chunkSize:{}", fileId, chunkIdx);
        }

        BitSet chunkBitSet = chunkStatusMap.get(fileId);
        if (chunkBitSet == null) {
            chunkBitSet = new BitSet();
        }
        chunkBitSet.set(chunkIdx);
        chunkStatusMap.put(fileId, chunkBitSet);

        fileChunksMeta.setUuid(fileId);
        fileChunksMeta.setChunkNum(chunkIdx);
        BitSet bs = new BitSet();
        bs.set(chunkIdx);
        fileChunksMeta.setChunkStatus(bs);

        try {
            writeChunkMeta(uploadDirPath, fileName, chunkIdx);
        } catch (IOException e) {
            log.error("write chunk meta error, e:{}", e);
        }
        return fileChunksMeta;
        */

        // send data to FileTransportSender

        // update bitmap in Zookeeper
        boolean finish = this.zkChunksMeta.setChunksBit(fileId, chunkIdx);
        // close AMOP channel if finish
        if (finish) {
            this.fileTransportReceiver.closeChannel(fileId);
            this.fileTransportSender.closeChannel(fileId);
        }

        return this.zkChunksMeta.getChunks(fileId);
    }

    @RequestMapping(path = "/downloadChunk")
    public byte[] downloadChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                @RequestParam(name = "fileId") String fileId,
                                @RequestParam(name = "chunkIdx") int chunkIdx) throws BrokerException {
        log.info("downloadChunk, groupId:{}. fileId:{}. chunkIdx:{}", groupId, fileId, chunkIdx);

        // get file data from FileTransportReceiver

        return null;
    }

    @RequestMapping(path = "/listChunk")
    public FileChunksMeta listChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                    @RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("listChunk, groupId:{}. fileId:{}", groupId, fileId);

        // get file chunks info from Zookeeper
        return this.zkChunksMeta.getChunks(fileId);
    }
}
