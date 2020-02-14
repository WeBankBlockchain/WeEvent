package com.webank.weevent.broker.fisco.file;


import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.FileChunksMeta;

/**
 * FileChunksMeta in Zookeeper.
 * Stored in expired ZkNode.
 *
 * @author matthewliu
 * @since 2020/02/14
 */
public class ZKChunksMeta {
    private final String zkPath;

    public ZKChunksMeta(String zkPath) {
        this.zkPath = zkPath;
    }

    public FileChunksMeta getChunks(String fileId) throws BrokerException {
        // get from zookeeper
        return new FileChunksMeta();
    }

    public void addChunks(String fileId, FileChunksMeta fileChunksMeta) throws BrokerException {
        // update to zookeeper
    }

    public void removeChunks(String fileId) throws BrokerException {
        // update to zookeeper
    }

    public boolean setChunksBit(String fileId, int bitIndex) throws BrokerException {
        FileChunksMeta fileChunksMeta = new FileChunksMeta();
        fileChunksMeta.getChunkStatus().set(bitIndex);

        // update to zookeeper

        // check if whole true
        return fileChunksMeta.getChunkStatus().cardinality() == fileChunksMeta.getChunkStatus().length();
    }
}
