package com.webank.weevent.protocol.rest;

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

    @Autowired
    public void setProducer(IProducer producer) {
        this.producer = producer;
    }

    @RequestMapping(path = "/createChunk")
    public FileChunksMeta createChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                      @RequestParam(name = "fileSize") long fileSize,
                                      @RequestParam(name = "md5") String md5) throws BrokerException {
        log.info("createChunk, groupId:{} md5:{}", groupId, md5);

        return this.producer.createChunk(groupId, fileSize, md5);
    }

    @RequestMapping(path = "/uploadChunk")
    public FileChunksMeta uploadChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                      @RequestParam(name = "fileId") String fileId,
                                      @RequestParam(name = "chunkIdx") int chunkIdx,
                                      @RequestParam(name = "chunkData") byte[] chunkData) throws BrokerException {
        log.info("uploadChunk, groupId:{}. fileId:{}. chunkIdx:{}", groupId, fileId, chunkIdx);

        return this.producer.uploadChunk(groupId, fileId, chunkIdx, chunkData);
    }

    @RequestMapping(path = "/downloadChunk")
    public byte[] downloadChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                @RequestParam(name = "fileId") String fileId,
                                @RequestParam(name = "chunkIdx") int chunkIdx) throws BrokerException {
        log.info("downloadChunk, groupId:{}. fileId:{}. chunkIdx:{}", groupId, fileId, chunkIdx);

        return this.producer.downloadChunk(groupId, fileId, chunkIdx);
    }

    @RequestMapping(path = "/listChunk")
    public FileChunksMeta listChunk(@RequestParam(name = "groupId", required = false) String groupId,
                                    @RequestParam(name = "fileId") String fileId) throws BrokerException {
        log.info("listChunk, groupId:{}. fileId:{}", groupId, fileId);

        return this.producer.listChunk(groupId, fileId);
    }
}
