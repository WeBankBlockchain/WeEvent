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
    String fileName;
    // file size in byte
    long fileSize;
    // file data's md5
    String fileMd5;

    // unique id for file
    String uuid;
    // chunk size
    int chunkSize;
    // chunk num
    int chunkNum;
    // all chunk's upload status(chunkIndex, true/false)
    BitSet chunkStatus;
}
