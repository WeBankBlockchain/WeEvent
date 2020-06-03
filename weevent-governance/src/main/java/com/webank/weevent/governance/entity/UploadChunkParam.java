package com.webank.weevent.governance.entity;

import com.webank.weevent.file.inner.DiskFiles;
import com.webank.weevent.file.service.FileChunksMeta;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadChunkParam {

    private String fileId;

    private Integer brokerId;

    private int chunkNumber;

    private byte[] chunkData;

    private FileChunksMeta fileChunksMeta;

    private DiskFiles diskFiles;

    @Override
    public String toString() {
        return "UploadChunkParam{" +
                "fileId='" + fileId + '\'' +
                ", brokerId=" + brokerId +
                ", chunkData=" + chunkData.length +
                ", chunkIdx=" + chunkNumber +
                ", chunkTotal=" + fileChunksMeta.getChunkSize() +
                '}';
    }
}
