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
        // sender -> receiver, start to transport file
        FileTransportStart(1),
        // sender -> receiver, end to transport file
        FileTransportEnd(2),

        // over AMOP channel
        // receiver -> sender, channel is already
        FileChannelAlready(10),
        // sender -> receiver, send file chunk data
        FileChannelData(11),
        // receiver -> sender, send file status
        FileChannelStatus(12),
        // exception on channel, cancel do
        FileChannelException(13);

        private final int code;

        EventType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private final EventType eventType;
    private FileChunksMeta fileChunksMeta;

    // have data while eventType == FIleChannelData
    private int chunkIndex = 0;
    private byte[] chunkData = null;

    public FileEvent(EventType eventType) {
        this.eventType = eventType;
    }
}
