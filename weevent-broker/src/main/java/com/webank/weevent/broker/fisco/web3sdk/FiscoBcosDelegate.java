package com.webank.weevent.broker.fisco.web3sdk;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webank.weevent.BrokerApplication;
import com.webank.weevent.broker.config.FiscoConfig;
import com.webank.weevent.broker.fisco.RedisService;
import com.webank.weevent.broker.fisco.constant.WeEventConstants;
import com.webank.weevent.broker.fisco.dto.ListPage;
import com.webank.weevent.broker.fisco.util.LRUCache;
import com.webank.weevent.protocol.rest.entity.GroupGeneral;
import com.webank.weevent.protocol.rest.entity.TbBlock;
import com.webank.weevent.protocol.rest.entity.TbNode;
import com.webank.weevent.protocol.rest.entity.TbTransHash;
import com.webank.weevent.sdk.BrokerException;
import com.webank.weevent.sdk.ErrorCode;
import com.webank.weevent.sdk.SendResult;
import com.webank.weevent.sdk.TopicInfo;
import com.webank.weevent.sdk.WeEvent;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Detect FISCO-BCOS version from configuration 'fisco.properties' and then proxy all the invoke to the target.
 * Parameter groupId in all interface:
 * a. default 1L in 1.3.x
 * b. default 1L in 2.x, meanings first group
 * There is 2 different caches for block data. One is local memory, another is redis.
 * All can be opened/closed by configuration. And is independent to each other.
 *
 * @author matthewliu
 * @since 2019/04/28
 */
@Slf4j
public class FiscoBcosDelegate {
    // access to version 1.x
    private FiscoBcos fiscoBcos;

    // access to version 2.x
    private Map<Long, FiscoBcos2> fiscoBcos2Map = new ConcurrentHashMap<>();

    // web3sdk timeout, ms
    public static Integer timeout = 10000;

    // thread pool used in web3sdk
    public static ThreadPoolTaskExecutor threadPool;

    // block data cached in redis
    private static RedisService redisService;

    // block data cached in local memory
    private static LRUCache<String, List<WeEvent>> blockCache;

    // groupId list
    private List<String> groupIdList = new ArrayList<>();

    /**
     * notify from web3sdk2.x when new block mined
     */
    public interface IBlockEventListener {
        /**
         * @param groupId group id
         * @param blockHeight new block height
         */
        void onEvent(Long groupId, Long blockHeight);
    }

    private void initRedisService() {
        if (redisService == null) {
            // load redis service if needed
            String redisServerIp = BrokerApplication.weEventConfig.getRedisServerIp();
            Integer redisServerPort = BrokerApplication.weEventConfig.getRedisServerPort();
            if (StringUtils.isNotBlank(redisServerIp) && redisServerPort > 0) {
                log.info("init redis service");

                redisService = BrokerApplication.applicationContext.getBean(RedisService.class);
            }
        }

        if (blockCache == null) {
            // skip local cache if lru.cache.capacity = 0
            Integer capacity = BrokerApplication.weEventConfig.getMaxCapacity();
            if (capacity > 0) {
                log.info("init local memory cache: " + capacity);

                blockCache = new LRUCache<>(capacity);
            }
        }
    }

    public static ThreadPoolTaskExecutor initThreadPool(FiscoConfig fiscoConfig) {
        // init thread pool
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setThreadNamePrefix("web3sdk_");
        pool.setCorePoolSize(fiscoConfig.getWeb3sdkCorePoolSize());
        pool.setMaxPoolSize(fiscoConfig.getWeb3sdkMaxPoolSize());
        // queue conflict with thread pool scale up, forbid it
        pool.setQueueCapacity(0);
        pool.setKeepAliveSeconds(fiscoConfig.getWeb3sdkKeepAliveSeconds());
        // abort policy
        pool.setRejectedExecutionHandler(null);
        pool.setDaemon(true);
        pool.initialize();

        log.info("init ThreadPoolTaskExecutor");
        return pool;
    }

    public void initProxy(FiscoConfig fiscoConfig) throws BrokerException {
        threadPool = initThreadPool(fiscoConfig);
        timeout = fiscoConfig.getWeb3sdkTimeout();

        if (StringUtils.isBlank(fiscoConfig.getVersion())) {
            log.error("the fisco version in fisco.properties is null");
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }
        if (StringUtils.isBlank(fiscoConfig.getNodes())) {
            log.error("the fisco nodes in fisco.properties is null");
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }

        if (fiscoConfig.getVersion().startsWith(WeEventConstants.FISCO_BCOS_1_X_VERSION_PREFIX)) {
            log.info("Notice: FISCO-BCOS's version is 1.x");

            // set web3sdk.Async thread pool
            new org.bcos.web3j.utils.Async(threadPool);

            FiscoBcos fiscoBcos = new FiscoBcos(fiscoConfig);
            fiscoBcos.init();

            this.fiscoBcos = fiscoBcos;
        } else if (fiscoConfig.getVersion().startsWith(WeEventConstants.FISCO_BCOS_2_X_VERSION_PREFIX)) {
            log.info("Notice: FISCO-BCOS's version is 2.x");

            // set web3sdk.Async thread pool
            new org.fisco.bcos.web3j.utils.Async(threadPool);

            // 1 is always exist
            Long defaultGId = Long.valueOf(WeEvent.DEFAULT_GROUP_ID);
            FiscoBcos2 defaultFiscoBcos2 = new FiscoBcos2(fiscoConfig);
            defaultFiscoBcos2.init(defaultGId);
            this.fiscoBcos2Map.put(defaultGId, defaultFiscoBcos2);
            // this call need default group has been initialized
            List<String> groups = this.listGroupId();

            // init all group in nodes except default one
            groups.remove(WeEvent.DEFAULT_GROUP_ID);
            for (String groupId : groups) {
                Long gid = Long.valueOf(groupId);
                FiscoBcos2 fiscoBcos2 = new FiscoBcos2(fiscoConfig);
                fiscoBcos2.init(gid);
                this.fiscoBcos2Map.put(gid, fiscoBcos2);
            }

            log.info("all group in nodes: {}", this.fiscoBcos2Map.keySet());
        } else {
            log.error("unknown FISCO-BCOS's version");
            throw new BrokerException(ErrorCode.WEB3SDK_INIT_ERROR);
        }

        initRedisService();
    }

    public boolean supportBlockEventNotify() {
        // 2.0 support notify
        return !this.fiscoBcos2Map.isEmpty();
    }

    /**
     * web3sdk will notify when new block mined in every group.
     *
     * @param listener listener
     */
    public void setListener(@NonNull IBlockEventListener listener) {
        log.info("set IBlockEventListener for every group for FISCO-BCOS 2.x");

        for (Map.Entry<Long, FiscoBcos2> entry : fiscoBcos2Map.entrySet()) {
            entry.getValue().setListener(listener);
        }
    }

    /**
     * list all group id
     *
     * @return list of groupId
     */
    public List<String> listGroupId() throws BrokerException {
        if (this.groupIdList.isEmpty()) {
            if (this.fiscoBcos != null) {
                this.groupIdList.add(WeEvent.DEFAULT_GROUP_ID);
            } else {
                // group 1 is always exist
                this.groupIdList = this.fiscoBcos2Map.get(Long.valueOf(WeEvent.DEFAULT_GROUP_ID)).listGroupId();
            }
        }
        return new ArrayList<>(this.groupIdList);
    }

    private void checkVersion(Long groupId) throws BrokerException {
        if (this.fiscoBcos != null) {
            if (groupId != Long.parseLong(WeEvent.DEFAULT_GROUP_ID)) {
                throw new BrokerException(ErrorCode.WEB3SDK_VERSION_NOT_SUPPORT);
            }
            return;
        }

        if (!this.fiscoBcos2Map.containsKey(groupId)) {
            throw new BrokerException(ErrorCode.WEB3SDK_VERSION_NOT_SUPPORT);
        }
    }

    public boolean createTopic(String topicName, Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.createTopic(topicName);
        } else {
            return this.fiscoBcos2Map.get(groupId).createTopic(topicName);
        }
    }

    public boolean isTopicExist(String topicName, Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.isTopicExist(topicName);
        } else {
            return this.fiscoBcos2Map.get(groupId).isTopicExist(topicName);
        }
    }

    public ListPage<String> listTopicName(Integer pageIndex, Integer pageSize, Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.listTopicName(pageIndex, pageSize);
        } else {
            return this.fiscoBcos2Map.get(groupId).listTopicName(pageIndex, pageSize);
        }
    }

    public TopicInfo getTopicInfo(String topicName, Long groupId, boolean skipCache) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.getTopicInfo(topicName);
        } else {
            return this.fiscoBcos2Map.get(groupId).getTopicInfo(topicName, skipCache);
        }
    }

    public WeEvent getEvent(String eventId, Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.getEvent(eventId);
        } else {
            return this.fiscoBcos2Map.get(groupId).getEvent(eventId);
        }
    }

    public SendResult publishEvent(String topicName, Long groupId, String eventContent, String extensions) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.publishEvent(topicName, eventContent, extensions);
        } else {
            return this.fiscoBcos2Map.get(groupId).publishEvent(topicName, eventContent, extensions);
        }
    }

    public Long getBlockHeight(Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            return this.fiscoBcos.getBlockHeight();
        } else {
            return this.fiscoBcos2Map.get(groupId).getBlockHeight();
        }
    }

    private String getRedisKey(Long blockNum, Long groupId) {
        if (this.fiscoBcos != null) {
            return Long.toString(blockNum);
        } else {
            return Long.toString(groupId) + blockNum;
        }
    }

    private List<WeEvent> getFromCache(String key) {
        try {
            if (blockCache != null && blockCache.containsKey(key)) {
                return blockCache.get(key);
            }
            if (redisService != null && redisService.isEventsExistInRedis(key)) {
                return redisService.readEventsFromRedis(key);
            }
        } catch (Exception e) {
            log.error("Exception happened while read events from redis server", e);
        }

        return null;
    }

    private void setCache(String key, List<WeEvent> events) {
        try {
            if (events != null) {
                if (blockCache != null) {
                    blockCache.putIfAbsent(key, events);
                }
                if (redisService != null) {
                    redisService.writeEventsToRedis(key, events);
                }
            }
        } catch (Exception e) {
            log.error("Exception happened while write events to redis server", e);
        }
    }

    /**
     * get data from block chain and it's cache
     *
     * @param blockNum block height
     * @param groupId group id
     * @return list of WeEvent
     * @throws BrokerException BrokerException
     */
    public List<WeEvent> loop(Long blockNum, Long groupId) throws BrokerException {
        checkVersion(groupId);

        List<WeEvent> events = new ArrayList<>();
        if (blockNum <= 0) {
            return events;
        }

        // try to get data from local cache and redis
        String key = getRedisKey(blockNum, groupId);
        events = getFromCache(key);
        // redis data may be dirty
        if (events != null) {
            return events;
        }

        // from block chain
        if (this.fiscoBcos != null) {
            events = this.fiscoBcos.loop(blockNum);
        } else {
            events = this.fiscoBcos2Map.get(groupId).loop(blockNum);
        }

        //write events list to redis server
        setCache(key, events);

        return events;
    }

    public GroupGeneral getGroupGeneral(Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            throw new BrokerException(ErrorCode.WEB3SDK_VERSION_NOT_SUPPORT);
        } else {
            return this.fiscoBcos2Map.get(groupId).getGroupGeneral();
        }
    }

    public ListPage<TbTransHash> queryTransList(Long groupId, String transHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            throw new BrokerException(ErrorCode.WEB3SDK_VERSION_NOT_SUPPORT);
        } else {
            return this.fiscoBcos2Map.get(groupId).queryTransList(transHash, blockNumber, pageIndex, pageSize);
        }
    }

    public ListPage<TbBlock> queryBlockList(Long groupId, String transHash, BigInteger blockNumber, Integer pageIndex, Integer pageSize) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            throw new BrokerException(ErrorCode.WEB3SDK_VERSION_NOT_SUPPORT);
        } else {
            return this.fiscoBcos2Map.get(groupId).queryBlockList(transHash, blockNumber, pageIndex, pageSize);
        }
    }

    public ListPage<TbNode> queryNodeList(Long groupId) throws BrokerException {
        checkVersion(groupId);

        if (this.fiscoBcos != null) {
            throw new BrokerException(ErrorCode.WEB3SDK_VERSION_NOT_SUPPORT);
        } else {
            return this.fiscoBcos2Map.get(groupId).queryNodeList();
        }
    }

}
