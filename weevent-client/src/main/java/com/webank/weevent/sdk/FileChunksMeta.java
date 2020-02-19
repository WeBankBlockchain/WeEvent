package com.webank.weevent.sdk;


import java.util.BitSet;

import lombok.Data;

/**
 * File chunk information.
 *
 * @author matthewliu
 * @since 2020/02/12
 */
@Data
public class FileChunksMeta {
    // file name in biz
    private String fileName;
    // file size in byte
    private long fileSize = 0;
    // file data's md5
    private String fileMd5;
    // topic
    private String topic;
    // groupId
    private String groupId;

    // uuid for file
    private String fileId;

    // chunk size
    private int chunkSize = 0;
    // chunk num
    private int chunkNum = 0;
    // all chunk's upload status(chunkIndex, true/false)
    private BitSet chunkStatus = new BitSet();

    public FileChunksMeta(String fileName, long fileSize, String fileMd5, String topic, String groupId) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileMd5 = fileMd5;
        this.topic = topic;
        this.groupId = groupId;
    }

    public FileChunksMeta(String fileId) {
        this.fileId = fileId;
    }

    private FileChunksMeta() {
    }

    public boolean checkChunkFull() {
        return chunkStatus.cardinality() == chunkStatus.length();
    }
}
