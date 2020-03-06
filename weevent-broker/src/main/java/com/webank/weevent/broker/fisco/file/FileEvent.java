package com.webank.weevent.broker.fisco.file;


import com.webank.weevent.client.FileChunksMeta;

import lombok.Getter;
import lombok.Setter;

/**
 * Event for file transport.
 *
 * @author matthewliu
 * @since 2020/02/16
 */
@Getter
@Setter
public class FileEvent {
    public enum EventType {
        // sign well done transport event in WeEvent
        FileTransport(1),

        // sender -> receiver over AMOP channel
        // start to a file transport
        FileChannelStart(10),
        // get file chunk meta
        FileChannelStatus(11),
        // send file chunk data
        FileChannelData(12),
        // end to a file transport
        FileChannelEnd(13),
        ;

        private final int code;

        EventType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private EventType eventType;
    private String fileId;

    // available only if while eventType == FileChannelStart or FileTransport
    private FileChunksMeta fileChunksMeta = null;

    // available only if eventType == FIleChannelData
    private int chunkIndex = 0;
    // available while eventType == FIleChannelData
    private byte[] chunkData = null;

    public FileEvent(EventType eventType, String fileId) {
        this.eventType = eventType;
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return "FileEvent{" +
                "eventType=" + this.eventType +
                ", fileId=" + this.fileId +
                ", fileChunksMeta=" + this.fileChunksMeta +
                ", chunkIndex=" + this.chunkIndex +
                ", chunkData.length=" + (this.chunkData == null ? 0 : this.chunkData.length) +
                '}';
    }

    // this is for jackson Serializer/Deserializer
    private FileEvent() {
    }
}
