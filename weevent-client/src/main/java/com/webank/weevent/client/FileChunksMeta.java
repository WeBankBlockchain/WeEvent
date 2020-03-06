package com.webank.weevent.client;


import java.io.IOException;
import java.util.BitSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;

/**
 * File chunk information.
 *
 * @author matthewliu
 * @since 2020/02/12
 */
@Getter
public class FileChunksMeta {
    // uuid for file
    private String fileId;
    // file name in biz
    private String fileName;
    // file size in byte
    private long fileSize;
    // file data's md5
    private String fileMd5;
    // topic
    private String topic;
    // groupId
    private String groupId;

    // file host, sender and receiver is different
    private String host;
    // chunk size
    private int chunkSize = 0;
    // chunk num
    private int chunkNum = 0;
    // all chunk's upload status(chunkIndex, true/false), chunkIndex begin with 0
    @JsonSerialize(using = BitSetJsonSerializer.class)
    @JsonDeserialize(using = BitSetJsonDeserializer.class)
    private BitSet chunkStatus;

    public FileChunksMeta(String fileId,
                          String fileName,
                          long fileSize,
                          String fileMd5,
                          String topic,
                          String groupId) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileMd5 = fileMd5;
        this.topic = topic;
        this.groupId = groupId;
    }

    public void initChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        this.chunkNum = (int) ((this.fileSize + this.chunkSize - 1) / this.chunkSize);
        this.chunkStatus = new BitSet(this.chunkNum);
    }

    public boolean checkChunkFull() {
        return this.chunkStatus.cardinality() == this.chunkNum;
    }

    public void setHost(String host) {
        this.host = host;
    }

    // this is for jackson Serializer/Deserializer
    private FileChunksMeta() {
    }

    static class BitSetJsonSerializer extends JsonSerializer<BitSet> {
        @Override
        public void serialize(BitSet value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeBinary(value.toByteArray());
        }
    }

    static class BitSetJsonDeserializer extends JsonDeserializer<BitSet> {
        @Override
        public BitSet deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
            return BitSet.valueOf(p.getBinaryValue());
        }
    }
}
