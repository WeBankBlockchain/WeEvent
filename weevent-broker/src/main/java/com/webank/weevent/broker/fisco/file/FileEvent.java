package com.webank.weevent.broker.fisco.file;


import com.webank.weevent.sdk.FileChunksMeta;

import lombok.Data;

/**
 * Event for file transport.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Data
public class FileEvent {
    public enum EventType {
        // over WeEvent
        // sender -> receiver, start to upload file
        FileTransportStart,
        // sender -> receiver, end to upload file
        FileTransportEnd,

        // over AMOP channel
        // sender -> receiver, send file data
        FIleChannelData,
        // receiver -> sender, send file status
        FIleChannelStatus,
        // channel ia already
        FIleChannelAlready,
        // channel to be close
        FIleChannelClose,
    }

    private EventType eventType;
    private FileChunksMeta fileChunksMeta;

    // have data while eventType == FIleChannelData
    private int chunkIndex = 0;
    private byte[] chunkData = null;

    public FileEvent(EventType eventType) {
        this.eventType = eventType;
    }
}
