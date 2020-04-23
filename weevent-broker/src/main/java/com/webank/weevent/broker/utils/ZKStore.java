package com.webank.weevent.broker.utils;


import java.util.Optional;

import com.webank.weevent.client.BrokerException;
import com.webank.weevent.client.ErrorCode;
import com.webank.weevent.client.JsonHelper;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.PathUtils;
import org.apache.zookeeper.CreateMode;

/**
 * Store Object T in zookeeper with key-value format.
 *
 * @author matthewliu
 * @since 2020/04/21
 */
@Slf4j
public class ZKStore<T> {
    private final String zkPath;
    private final CuratorFramework zkClient;

    public ZKStore(String zkPath, String connectString) throws BrokerException {
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

    public boolean exist(String key) throws BrokerException {
        String zkPath = this.genPath(key);

        return this.zkExist(zkPath);
    }


    public Optional<T> get(String key) throws BrokerException {
        String zkPath = this.genPath(key);

        // get from zookeeper
        if (this.zkExist(zkPath)) {
            return Optional.of(this.zkGet(zkPath));
        }

        return Optional.empty();
    }

    public void add(String key, T value) throws BrokerException {
        String zkPath = this.genPath(key);

        if (this.zkExist(zkPath)) {
            log.error("already exist key in zookeeper, {}", key);
            throw new BrokerException(ErrorCode.ZOOKEEPER_EXIST_KEY);
        }

        // create in zookeeper
        this.zkAdd(zkPath, value);
    }

    public void remove(String key) throws BrokerException {
        String zkPath = this.genPath(key);

        // remove in zookeeper
        this.zkRemove(zkPath);
    }

    public void update(String key, T value) throws BrokerException {
        String zkPath = this.genPath(key);

        if (!this.zkExist(zkPath)) {
            log.error("not exist key in zookeeper, {}", key);
            throw new BrokerException(ErrorCode.ZOOKEEPER_UNKNOWN_KEY);
        }

        // update to zookeeper
        this.zkUpdate(zkPath, value);
    }

    public void set(String key, T value) throws BrokerException {
        String zkPath = this.genPath(key);

        if (this.zkExist(zkPath)) {
            this.zkUpdate(zkPath, value);
        } else {
            this.zkAdd(zkPath, value);
        }
    }

    private boolean zkExist(String zkPath) throws BrokerException {
        try {
            return this.zkClient.checkExists().forPath(zkPath) != null;
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }
    }

    private void zkAdd(String zkPath, T value) throws BrokerException {
        log.info("zookeeper add path, {}", zkPath);

        try {
            byte[] json = JsonHelper.object2JsonBytes(value);
            this.zkClient.create().withMode(CreateMode.PERSISTENT).forPath(zkPath, json);
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }
    }

    private T zkGet(String zkPath) throws BrokerException {
        byte[] nodeData;
        try {
            nodeData = this.zkClient.getData().forPath(zkPath);
        } catch (Exception e) {
            log.error("access zookeeper failed", e);
            throw new BrokerException(ErrorCode.ZOOKEEPER_ERROR);
        }

        return JsonHelper.json2Object(nodeData, new TypeReference<T>() {
        });
    }

    private void zkUpdate(String zkPath, T value) throws BrokerException {
        log.info("zookeeper update path, {}", zkPath);

        try {
            byte[] json = JsonHelper.object2JsonBytes(value);
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

    private String genPath(String key) {
        return this.zkPath + "/" + key;
    }
}
