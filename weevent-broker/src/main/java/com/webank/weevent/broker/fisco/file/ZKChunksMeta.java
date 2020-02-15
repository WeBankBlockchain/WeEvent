package com.webank.weevent.broker.fisco.file;


import java.nio.charset.StandardCharsets;

import com.webank.weevent.broker.fisco.util.DataTypeUtils;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.FileChunksMeta;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.PathUtils;
import org.apache.zookeeper.CreateMode;

/**
 * FileChunksMeta in Zookeeper.
 * Stored in expired ZkNode.
 *
 * @author matthewliu
 * @since 2020/02/14
 */
@Slf4j
public class ZKChunksMeta {
    private final String zkPath;
    private CuratorFramework zkClient;

    public ZKChunksMeta(String zkPath, String connectString) throws BrokerException {
        try {
            PathUtils.validatePath(zkPath);
        } catch (IllegalArgumentException e) {
            log.error("invalid zookeeper path", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_INVALID_PATH);
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.zkClient = CuratorFrameworkFactory.newClient(
                connectString,
                10000,
                3000,
                retryPolicy);

        try {
            this.zkClient.start();
            this.zkClient.checkExists().creatingParentsIfNeeded().forPath(zkPath);
            log.info("ensure zookeeper root path, {}", zkPath);
        } catch (Exception e) {
            log.error("ensure zookeeper root path failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }

        this.zkPath = zkPath;
    }

    public FileChunksMeta getChunks(String fileId) throws BrokerException {
        String zkPath = this.genFilePath(fileId);

        if (!this.zkExist(zkPath)) {
            throw new BrokerException(ErrorCode.ZOOKEEPER_UNKNOWN_FILE);
        }

        // get from zookeeper
        return this.zkGet(zkPath);
    }

    public void addChunks(String fileId, FileChunksMeta fileChunksMeta) throws BrokerException {
        String zkPath = this.genFilePath(fileId);

        if (this.zkExist(zkPath)) {
            log.error("already exist file id in zookeeper, {}", fileId);
            throw new BrokerException(ErrorCode.ZOOKEEPER_EXIST_FILE);
        }

        // create in zookeeper
        this.zkSet(zkPath, fileChunksMeta);
    }

    public void removeChunks(String fileId) throws BrokerException {
        String zkPath = this.genFilePath(fileId);

        // remove in zookeeper
        this.zkRemove(zkPath);
    }

    public boolean setChunksBit(String fileId, int bitIndex) throws BrokerException {
        String zkPath = this.genFilePath(fileId);

        if (!this.zkExist(zkPath)) {
            log.error("not exist file id in zookeeper, {}", fileId);
            throw new BrokerException(ErrorCode.ZOOKEEPER_UNKNOWN_FILE);
        }

        FileChunksMeta fileChunksMeta = this.zkGet(zkPath);
        fileChunksMeta.getChunkStatus().set(bitIndex);

        // update to zookeeper
        this.zkSet(this.zkPath, fileChunksMeta);

        // check if whole true
        return fileChunksMeta.getChunkStatus().cardinality() == fileChunksMeta.getChunkStatus().length();
    }

    private boolean zkExist(String zkPath) throws BrokerException {
        try {
            return this.zkClient.checkExists().forPath(zkPath) != null;
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }
    }

    private FileChunksMeta zkGet(String zkPath) throws BrokerException {
        byte[] nodeData;
        try {
            nodeData = this.zkClient.getData().forPath(zkPath);
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }

        return DataTypeUtils.json2Object(new String(nodeData, StandardCharsets.UTF_8), FileChunksMeta.class);
    }

    private void zkSet(String zkPath, FileChunksMeta fileChunksMeta) throws BrokerException {
        try {

            String json = DataTypeUtils.object2Json(fileChunksMeta);
            this.zkClient.create().withMode(CreateMode.PERSISTENT).forPath(zkPath, json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }
    }

    private void zkRemove(String zkPath) throws BrokerException {
        try {
            this.zkClient.delete().forPath(zkPath);
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }
    }

    private String genFilePath(String fileId) {
        return this.zkPath + "/" + fileId;
    }
}
