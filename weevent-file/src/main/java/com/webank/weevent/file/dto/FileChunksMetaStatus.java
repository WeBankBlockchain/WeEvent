package com.webank.weevent.file.dto;


import com.webank.weevent.client.BrokerException;
import com.webank.weevent.file.service.FileChunksMeta;
import com.webank.weevent.client.JsonHelper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * FileChunksMeta + plus information.
 *
 * @author matthewliu
 * @since 2020/03/12
 */
@Slf4j
@Getter
@Setter
public class FileChunksMetaStatus {
    private FileChunksMeta file;

    // cost time in second
    private String time;
    // sender ready chunk
    private int readyChunk;
    // processing
    private String process;
    // speed in Byte/s
    private String speed;

    private FileChunksMetaStatus() {
    }

    public FileChunksMetaStatus(FileChunksMeta file) {
        // deep clone
        try {
            byte[] json = JsonHelper.object2JsonBytes(file);
            this.file = JsonHelper.json2Object(json, FileChunksMeta.class);
        } catch (BrokerException e) {
            log.error("deep clone failed");
        }

        long time = (System.currentTimeMillis() / 1000) - this.file.getStartTime();
        this.time = String.format("%ss", time);
        this.readyChunk = this.file.getChunkStatus().cardinality();
        this.process = String.format("%.2f%%", (float) (this.readyChunk * 100) / this.file.getChunkNum());
        this.speed = String.format("%.2fB/s", ((double) this.readyChunk * this.file.getChunkSize()) / time);

        // do not show chunk detail
        this.file.cleanChunkStatus();
    }
}
