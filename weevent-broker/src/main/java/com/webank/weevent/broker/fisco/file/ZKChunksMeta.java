package com.webank.weevent.broker.fisco.file;


import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.FileChunksMeta;
import com.webank.weevent.client.JsonHelper;

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
        log.info("try to access zookeeper, {}@{}", zkPath, connectString);

        try {
            PathUtils.validatePath(zkPath);
        } catch (IllegalArgumentException e) {
            log.error("invalid zookeeper path", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_INVALID_PATH);
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // default data is local address. skip it
        this.zkClient = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .sessionTimeoutMs(10000)
                .connectionTimeoutMs(3000)
                .retryPolicy(retryPolicy)
                .defaultData("".getBytes())
                .build();

        try {
            this.zkClient.start();
            // ensure path
            if (this.zkClient.checkExists().forPath(zkPath) == null) {
                this.zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPath);
            }

            log.info("ensure zookeeper root path for file, {}", zkPath);
        } catch (Exception e) {
            log.error("ensure zookeeper root path for file failed", e);
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
        this.zkAdd(zkPath, fileChunksMeta);
    }

    public void removeChunks(String fileId) throws BrokerException {
        String zkPath = this.genFilePath(fileId);

        // remove in zookeeper
        this.zkRemove(zkPath);
    }

    public boolean updateChunks(String fileId, FileChunksMeta fileChunksMeta) throws BrokerException {
        String zkPath = this.genFilePath(fileId);

        if (!this.zkExist(zkPath)) {
            log.error("not exist file id in zookeeper, {}", fileId);
            throw new BrokerException(ErrorCode.ZOOKEEPER_UNKNOWN_FILE);
        }

        // update to zookeeper
        this.zkUpdate(zkPath, fileChunksMeta);

        // check is full
        return fileChunksMeta.checkChunkFull();
    }

    private boolean zkExist(String zkPath) throws BrokerException {
        try {
            return this.zkClient.checkExists().forPath(zkPath) != null;
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }
    }

    private void zkAdd(String zkPath, FileChunksMeta fileChunksMeta) throws BrokerException {
        log.info("zookeeper add path, {}", zkPath);

        try {
            byte[] json = JsonHelper.object2JsonBytes(fileChunksMeta);
            this.zkClient.create().withMode(CreateMode.PERSISTENT).forPath(zkPath, json);
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

        return JsonHelper.json2Object(nodeData, FileChunksMeta.class);
    }

    private void zkUpdate(String zkPath, FileChunksMeta fileChunksMeta) throws BrokerException {
        log.info("zookeeper update path, {}", zkPath);

        try {
            byte[] json = JsonHelper.object2JsonBytes(fileChunksMeta);
            this.zkClient.setData().forPath(zkPath, json);
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }
    }

    // zkPath is a child node, DO NOT contains any child
    private void zkRemove(String zkPath) throws BrokerException {
        log.info("zookeeper remove path, {}", zkPath);

        try {
            this.zkClient.delete().guaranteed().forPath(zkPath);
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }
    }

    private String genFilePath(String fileId) {
        return this.zkPath + "/" + fileId;
    }
}
