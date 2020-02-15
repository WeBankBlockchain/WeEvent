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
    private long fileSize;
    // file data's md5
    private String fileMd5;

    // uuid for file
    private String fileId;
    // chunk size
    private int chunkSize;
    // chunk num
    private int chunkNum;
    // all chunk's upload status(chunkIndex, true/false)
    private BitSet chunkStatus;
    // groupId
    private String groupId;
    // topic
    private String topic;
}
